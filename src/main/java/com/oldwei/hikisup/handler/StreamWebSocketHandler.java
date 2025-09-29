package com.oldwei.hikisup.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.oldwei.hikisup.config.HikIsupProperties;
import com.oldwei.hikisup.domain.ChatMessage;
import com.oldwei.hikisup.domain.DeviceCache;
import com.oldwei.hikisup.sdk.service.IHCISUPCMS;
import com.oldwei.hikisup.sdk.service.IHikISUPStream;
import com.oldwei.hikisup.sdk.structure.*;
import com.oldwei.hikisup.util.GlobalCacheService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

@Slf4j
@Component
public class StreamWebSocketHandler implements WebSocketHandler {

    private final ConnectionManager connectionManager;
    private final ObjectMapper objectMapper;
    private final HikIsupProperties hikIsupProperties;
    private final IHikISUPStream hikISUPStream;
    private final IHCISUPCMS ihcisupcms;
    @Resource(name = "streamExecutor")
    private Executor streamExecutor;


    // 每个设备一个 latch，用于控制阻塞/停止
    private final Map<String, CountDownLatch> latchMap = new ConcurrentHashMap<>();

    public StreamWebSocketHandler(ConnectionManager connectionManager, HikIsupProperties hikIsupProperties, IHikISUPStream hikISUPStream, IHCISUPCMS ihcisupcms) {
        this.connectionManager = connectionManager;
        this.hikIsupProperties = hikIsupProperties;
        this.hikISUPStream = hikISUPStream;
        this.ihcisupcms = ihcisupcms;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private final Map<String, Sinks.Many<ChatMessage>> sessionSinks = new ConcurrentHashMap<>();
    private final Sinks.Many<ChatMessage> globalMessageSink = Sinks.many().multicast().onBackpressureBuffer();
    // 在类的成员变量中添加
    private final Map<String, WebSocketSession> deviceSessions = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String sessionId = session.getId();
        String username = "User-" + (sessionId.length() >= 8 ? sessionId.substring(0, 8) : sessionId);

        log.info("WebSocket 连接建立: {} 连接用户: {}", sessionId, username);

        connectionManager.addConnection(sessionId, username);

        Sinks.Many<ChatMessage> messageSink = Sinks.many().multicast().onBackpressureBuffer();
        sessionSinks.put(sessionId, messageSink);
        deviceSessions.put(username, session);

        ChatMessage joinMessage = ChatMessage.builder()
                .type(ChatMessage.MessageType.JOIN)
                .sender("System")
                .content(username + " 加入聊天")
                .timestamp(LocalDateTime.now())
                .build();
        globalMessageSink.tryEmitNext(joinMessage);

        Mono<Void> input = session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .doOnNext(messageText -> {
                    try {
                        log.info("接收到的数据 {}", messageText);
                        ChatMessage message = objectMapper.readValue(messageText, ChatMessage.class);
                        message.setTimestamp(LocalDateTime.now());
                        message.setType(ChatMessage.MessageType.CHAT);
                        if (message.getContent().equals("开始预览")) {
                            CompletableFuture.runAsync(() -> {
                                // 异步处理数据
                                DeviceCache stream = (DeviceCache) GlobalCacheService.getInstance().get(message.getSender());
                                deviceSessions.put(stream.getDeviceId(), session);
                                preview(stream.getLLoginID(), stream.getLChannel(), stream.getDeviceId(), "7650");
                            }, streamExecutor);

                        } else if (message.getContent().equals("停止预览")) {
                            DeviceCache stream = (DeviceCache) GlobalCacheService.getInstance().get(message.getSender());
                            stopPreview(stream.getDeviceId(), stream.getLLoginID());
                        } else {
                            message.setSender(username);
                        }

                        globalMessageSink.tryEmitNext(message);
                        log.debug("Received message from {}: {}", username, message.getContent());
                    } catch (Exception e) {
                        log.error("Error parsing message from {}: {}", username, messageText, e);
                    }
                })
                .doOnError(error -> log.error("Error in WebSocket receive for {}: {}", username, error.getMessage()))
                .doOnCancel(() -> handleDisconnection(sessionId, username))
                .doOnTerminate(() -> handleDisconnection(sessionId, username))
                .then();

        Flux<WebSocketMessage> output = Flux.merge(
                        messageSink.asFlux().map(msg -> {
                            try {
                                return objectMapper.writeValueAsString(msg);
                            } catch (Exception e) {
                                log.error("Error serializing message", e);
                                return "";
                            }
                        }),
                        globalMessageSink.asFlux()
                                .filter(msg -> {
                                    // 对于CHAT消息，只发送给其他用户（不发送给发送者）
                                    // 对于JOIN/LEAVE消息，发送给所有用户
                                    return msg.getType() != ChatMessage.MessageType.CHAT || !msg.getSender().equals(username);
                                })
                                .map(msg -> {
                                    try {
                                        return objectMapper.writeValueAsString(msg);
                                    } catch (Exception e) {
                                        log.error("Error serializing message", e);
                                        return "";
                                    }
                                })
                )
                .filter(json -> !json.isEmpty())
                .map(session::textMessage)
                .doOnError(error -> log.error("Error in WebSocket send for {}: {}", username, error.getMessage()));

        return Mono.zip(input, session.send(output)).then();
    }

    private void handleDisconnection(String sessionId, String username) {
        log.info("WebSocket connection closed: {} for user: {}", sessionId, username);

        connectionManager.removeConnection(sessionId);
        sessionSinks.remove(sessionId);

        ChatMessage leaveMessage = ChatMessage.builder()
                .type(ChatMessage.MessageType.LEAVE)
                .sender("System")
                .content(username + " left the chat")
                .timestamp(LocalDateTime.now())
                .build();

        globalMessageSink.tryEmitNext(leaveMessage);
    }

    public void preview(int lLoginID, int lChannel, String deviceId, String randomPort) {
        int lListenHandle = -1;
        int sessionID = -1;
        CountDownLatch latch = new CountDownLatch(1);
        latchMap.put(deviceId, latch);


        try {
            lListenHandle = startPlayBackListen(randomPort, deviceId);
            if (lListenHandle == -1) {
                log.error("启动预览监听失败");
                return;
            }

            sessionID = RealPlay(lLoginID, lChannel, randomPort);
            if (sessionID == -1) {
                log.error("启动实时流失败");
                return;
            }

            DeviceCache stream = (DeviceCache) GlobalCacheService.getInstance().get(deviceId);
            stream.setSessionId(sessionID);
            GlobalCacheService.getInstance().put(deviceId, stream);
            log.info("sessionID: {}, lListenHandle: {}", sessionID, lListenHandle);

            // 阻塞，直到 stopPreview() 调用 latch.countDown()
            latch.await();

        } catch (InterruptedException e) {
            log.error("线程被中断", e);
            Thread.currentThread().interrupt(); // 恢复中断状态
        } catch (Exception e) {
            log.error("处理流时发生异常", e);
        } finally {
            // 确保资源被正确清理
            if (sessionID != -1) {
                StopRealPlay(lLoginID, sessionID, lListenHandle, lListenHandle, hikISUPStream);
            }
            latchMap.remove(deviceId);
            log.info("保存流{}结束", deviceId);
        }
    }

    public void stopPreview(String deviceId, int lLoginID) {
        CountDownLatch latch = latchMap.get(deviceId);
        if (latch != null) {
            latch.countDown(); // 唤醒 preview
            log.info("停止预览: {}", deviceId);
        }
    }

    private int startPlayBackListen(String randomPort, String deviceId) {
        log.info("========================= 启动SMS =========================");
        NET_EHOME_LISTEN_PREVIEW_CFG netEhomeListenPreviewCfg = new NET_EHOME_LISTEN_PREVIEW_CFG();
        System.arraycopy(hikIsupProperties.getSmsServer().getListenIp().getBytes(), 0, netEhomeListenPreviewCfg.struIPAdress.szIP, 0, hikIsupProperties.getSmsServer().getListenIp().length());
        netEhomeListenPreviewCfg.struIPAdress.wPort = Short.parseShort(randomPort); //流媒体服务器监听端口

        netEhomeListenPreviewCfg.fnNewLinkCB = (lLinkHandle, pNewLinkCBMsg, pUserData) -> {
            //预览数据回调参数
            System.out.println("[lPreviewHandle 默认值 -1]预览数据回调参数:" + lLinkHandle);
            NET_EHOME_PREVIEW_DATA_CB_PARAM struDataCB = new NET_EHOME_PREVIEW_DATA_CB_PARAM();
            struDataCB.fnPreviewDataCB = (iPreviewHandle, pPreviewCBMsg, pud) -> {
                long offset = 0;
                ByteBuffer buffers = pPreviewCBMsg.pRecvdata.getByteBuffer(offset, pPreviewCBMsg.dwDataLen);
                byte[] bytes = new byte[pPreviewCBMsg.dwDataLen];
                buffers.rewind();
                buffers.get(bytes);
                // TODO 发送bytes到前端
                CompletableFuture.runAsync(() -> {
                    // 异步处理数据
                    // 发送视频数据到前端
                    log.info("预览数据回调, iPreviewHandle: {}, dwDataLen: {}", iPreviewHandle, pPreviewCBMsg.dwDataLen);
                    sendVideoDataToFrontend(deviceId, bytes);
                }, streamExecutor);
            };

            if (!this.hikISUPStream.NET_ESTREAM_SetPreviewDataCB(lLinkHandle, struDataCB)) {
                System.out.println("NET_ESTREAM_SetPreviewDataCB failed err:：" + this.hikISUPStream.NET_ESTREAM_GetLastError());
                return false;
            }
            return true;
        }; //预览连接请求回调函数
        netEhomeListenPreviewCfg.pUser = null;
        netEhomeListenPreviewCfg.byLinkMode = 0; //0- TCP方式，1- UDP方式
        netEhomeListenPreviewCfg.write();
        int lListenHandle = hikISUPStream.NET_ESTREAM_StartListenPreview(netEhomeListenPreviewCfg);
        log.info("lListenHandle: {}", lListenHandle);
        if (lListenHandle == -1) {
            hikISUPStream.NET_ESTREAM_Fini();
            log.error("流媒体预览监听启动失败, error code: {}", hikISUPStream.NET_ESTREAM_GetLastError());
        } else {
            String StreamListenInfo = new String(netEhomeListenPreviewCfg.struIPAdress.szIP).trim() + "_" + netEhomeListenPreviewCfg.struIPAdress.wPort;
            log.info("{}, 流媒体服务：流媒体预览监听启动成功", StreamListenInfo);
        }
        return lListenHandle;
    }

    /**
     * 开启预览
     *
     * @param lLoginID
     * @param lChannel
     * @return sessionID 会话id
     */
    public int RealPlay(int lLoginID, int lChannel, String randomPort) {
        int sessionID = -1; //预览sessionID
        NET_EHOME_PREVIEWINFO_IN_V11 struPreviewInV11 = new NET_EHOME_PREVIEWINFO_IN_V11();
        struPreviewInV11.iChannel = lChannel; //通道号
        struPreviewInV11.dwLinkMode = 0; //0- TCP方式，1- UDP方式
        struPreviewInV11.dwStreamType = 0; //码流类型：0- 主码流，1- 子码流, 2- 第三码流
        log.info("ip: {}, port: {}", hikIsupProperties.getSmsServer().getIp(), hikIsupProperties.getSmsServer().getPort());
        struPreviewInV11.struStreamSever.szIP = hikIsupProperties.getSmsServer().getIp().getBytes();//流媒体服务器IP地址,公网地址
        struPreviewInV11.struStreamSever.wPort = Short.parseShort(randomPort); //流媒体服务器端口，需要跟服务器启动监听端口一致
        struPreviewInV11.write();
        //预览请求
        NET_EHOME_PREVIEWINFO_OUT struPreviewOut = new NET_EHOME_PREVIEWINFO_OUT();
        boolean getRS = ihcisupcms.NET_ECMS_StartGetRealStreamV11(lLoginID, struPreviewInV11, struPreviewOut);
        log.info("NET_ECMS_StartGetRealStream 预览请求: {}", getRS);
        if (!getRS) {
            log.error("NET_ECMS_StartGetRealStream failed, error code: {}", ihcisupcms.NET_ECMS_GetLastError());
            return sessionID;
        } else {
            struPreviewOut.read();
            log.info("NET_ECMS_StartGetRealStream succeed, sessionID: {}", struPreviewOut.lSessionID);
            sessionID = struPreviewOut.lSessionID;
        }
        NET_EHOME_PUSHSTREAM_IN struPushInfoIn = new NET_EHOME_PUSHSTREAM_IN();
        struPushInfoIn.read();
        struPushInfoIn.dwSize = struPushInfoIn.size();
        struPushInfoIn.lSessionID = sessionID;
        struPushInfoIn.write();
        NET_EHOME_PUSHSTREAM_OUT struPushInfoOut = new NET_EHOME_PUSHSTREAM_OUT();
        struPushInfoOut.read();
        struPushInfoOut.dwSize = struPushInfoOut.size();
        struPushInfoOut.write();
        if (!ihcisupcms.NET_ECMS_StartPushRealStream(lLoginID, struPushInfoIn, struPushInfoOut)) {
            log.error("NET_ECMS_StartPushRealStream failed, error code: {}", ihcisupcms.NET_ECMS_GetLastError());
            return sessionID;
        } else {
            log.info("NET_ECMS_StartPushRealStream succeed, sessionID: {}", struPushInfoIn.lSessionID);
        }
        return sessionID;
    }

    /**
     * 停止预览,Stream服务停止实时流转发，CMS向设备发送停止预览请求
     */
    public void StopRealPlay(int lLoginID, int sessionID, int lPreviewHandle, int lListenHandle, IHikISUPStream hikISUPStream) {
        log.info("停止获取实时流");
        if (!ihcisupcms.NET_ECMS_StopGetRealStream(lLoginID, sessionID)) {
            log.error("NET_ECMS_StopGetRealStream failed,err = {}", ihcisupcms.NET_ECMS_GetLastError());
            return;
        }
        log.info("停止预览");
        if (!hikISUPStream.NET_ESTREAM_StopPreview(lPreviewHandle)) {
            log.error("NET_ESTREAM_StopPreview failed,err = {}", hikISUPStream.NET_ESTREAM_GetLastError());
            return;
        }
        log.info("停止监听预览");
        if (!hikISUPStream.NET_ESTREAM_StopListenPreview(lListenHandle)) {
            log.error("NET_ESTREAM_StopListenPreview failed,err = {}", ihcisupcms.NET_ECMS_GetLastError());
        }
    }

    // 新增方法：发送视频数据到前端
    private void sendVideoDataToFrontend(String deviceId, byte[] videoData) {
        WebSocketSession session = deviceSessions.get(deviceId);
        if (session != null && session.isOpen()) {
            try {
                // 方式1：发送二进制数据
                WebSocketMessage binaryMessage = session.binaryMessage(factory -> factory.wrap(videoData));
                session.send(Mono.just(binaryMessage)).subscribe();
            } catch (Exception e) {
                log.error("发送视频数据到前端失败: {}", e.getMessage());
                // 连接异常时移除会话
                deviceSessions.remove(deviceId);
            }
        }
    }

}