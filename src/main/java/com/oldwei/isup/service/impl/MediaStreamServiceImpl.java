package com.oldwei.isup.service.impl;

import com.oldwei.isup.config.HikIsupProperties;
import com.oldwei.isup.handler.StreamHandler;
import com.oldwei.isup.model.Device;
import com.oldwei.isup.sdk.StreamManager;
import com.oldwei.isup.sdk.service.HCISUPCMS;
import com.oldwei.isup.sdk.service.IHikISUPStream;
import com.oldwei.isup.sdk.structure.NET_EHOME_PREVIEWINFO_IN_V11;
import com.oldwei.isup.sdk.structure.NET_EHOME_PREVIEWINFO_OUT;
import com.oldwei.isup.sdk.structure.NET_EHOME_PUSHSTREAM_IN;
import com.oldwei.isup.sdk.structure.NET_EHOME_PUSHSTREAM_OUT;
import com.oldwei.isup.service.IDeviceService;
import com.oldwei.isup.service.IMediaStreamService;
import com.oldwei.isup.websocket.WebSocketManager;
import com.oldwei.isup.websocket.WebSocketServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

@Slf4j
@Service("mediaStreamService")
@RequiredArgsConstructor
public class MediaStreamServiceImpl implements IMediaStreamService {

    private final HikIsupProperties hikIsupProperties;
    private final IHikISUPStream hikISUPStream;
    private final HCISUPCMS hcisupcms;
    private final IDeviceService deviceService;

    // 每个设备一个 latch，用于控制阻塞/停止
    private final Map<String, CountDownLatch> latchMap = new ConcurrentHashMap<>();

    @Async("streamExecutor")
    @Override
    public void preview(Device device) {
        CountDownLatch latch = new CountDownLatch(1);
        latchMap.put(device.getDeviceId(), latch);
        try {
            // 启动或获取 WebSocket 服务
            int wsPort = 9002;
            WebSocketServer wsServer = WebSocketManager.getOrCreateServer(wsPort);
            // 创建异步控制器
            CompletableFuture<String> completableFuture = new CompletableFuture<>();
            // 定义视频帧消费逻辑（推送给 WebSocket 客户端）
            // 将 frame 推送给所有连接的客户端，或按 playKey 选择推送
            Consumer<byte[]> frameConsumer = frame -> {
                wsServer.sendToPlayKey(device.getDeviceId(), frame);
            };
            int sessionID = RealPlay(device, completableFuture, frameConsumer);
            if (sessionID == -1) {
                log.error("启动实时流失败");
                return;
            }
            device.setPreviewSessionId(sessionID);
            deviceService.updateById(device);
            log.info("sessionID: {}", device.getPreviewSessionId());

            // 阻塞，直到 stopPreview() 调用 latch.countDown()
            latch.await();
            log.info("结束阻塞，准备停止预览");
        } catch (InterruptedException e) {
            log.error("线程被中断", e);
            Thread.currentThread().interrupt(); // 恢复中断状态
        } catch (Exception e) {
            log.error("处理流时发生异常", e);
        } finally {
            // 确保资源被正确清理
            if (device.getPreviewSessionId() != -1) {
                //停止预览,Stream服务停止实时流转发，CMS向设备发送停止预览请求
                log.info("停止获取实时流");
                if (!hcisupcms.NET_ECMS_StopGetRealStream(device.getLoginId(), device.getPreviewSessionId())) {
                    log.error("NET_ECMS_StopGetRealStream failed,err = {}", hcisupcms.NET_ECMS_GetLastError());
                }
                log.info("停止预览");
                if (!hikISUPStream.NET_ESTREAM_StopPreview(device.getPreviewHandle())) {
                    log.error("NET_ESTREAM_StopPreview failed,err = {}", hikISUPStream.NET_ESTREAM_GetLastError());
                }
//                log.info("停止监听预览");
//                if (!hikISUPStream.NET_ESTREAM_StopListenPreview(device.getPreviewListenHandle())) {
//                    log.error("NET_ESTREAM_StopListenPreview failed,err = {}", hcisupcms.NET_ECMS_GetLastError());
//                }
                // 销毁streamHandler对象
                StreamHandler streamHandler = StreamManager.concurrentMap.get(device.getPreviewSessionId());
                if (streamHandler != null) {
                    streamHandler.close();
                    StreamManager.concurrentMap.remove(device.getPreviewSessionId());
                }
                device.setIsPush(-1);
                deviceService.updateById(device);
            }
            latchMap.remove(device.getDeviceId());
            log.info("保存流{}结束", device.getDeviceId());
        }
        log.info("预览线程结束: {}", device.getDeviceId());
    }

    public void stopPreview(Device device) {
        CountDownLatch latch = latchMap.get(device.getDeviceId());
        if (latch != null) {
            latch.countDown(); // 唤醒 preview
            log.info("结束预览实例: {}", device.getDeviceId());
        }
    }

    /**
     * 开启预览
     *
     * @return sessionID 会话id
     */
    public int RealPlay(Device device, CompletableFuture<String> completableFuture, Consumer<byte[]> frameConsumer) {
        NET_EHOME_PREVIEWINFO_IN_V11 struPreviewInV11 = new NET_EHOME_PREVIEWINFO_IN_V11();
        struPreviewInV11.iChannel = device.getChannel(); //通道号
        struPreviewInV11.dwLinkMode = 0; //0- TCP方式，1- UDP方式
        struPreviewInV11.dwStreamType = 0; //码流类型：0- 主码流，1- 子码流, 2- 第三码流
        log.info("ip: {}, port: {}", hikIsupProperties.getSmsServer().getIp(), hikIsupProperties.getSmsServer().getPort());
        struPreviewInV11.struStreamSever.szIP = hikIsupProperties.getSmsServer().getIp().getBytes();//流媒体服务器IP地址,公网地址
        struPreviewInV11.struStreamSever.wPort = Short.parseShort(hikIsupProperties.getSmsServer().getPort()); //流媒体服务器端口，需要跟服务器启动监听端口一致
        struPreviewInV11.write();
        //预览请求
        NET_EHOME_PREVIEWINFO_OUT struPreviewOut = new NET_EHOME_PREVIEWINFO_OUT();
        boolean getRS = hcisupcms.NET_ECMS_StartGetRealStreamV11(device.getLoginId(), struPreviewInV11, struPreviewOut);
        log.info("NET_ECMS_StartGetRealStream 预览请求: {}", getRS);
        if (!hcisupcms.NET_ECMS_StartGetRealStreamV11(device.getLoginId(), struPreviewInV11, struPreviewOut)) {
            log.error("NET_ECMS_StartGetRealStream failed, error code: {}", hcisupcms.NET_ECMS_GetLastError());
            return -1;
        } else {
            struPreviewOut.read();
            log.info("NET_ECMS_StartGetRealStream succeed, sessionID: {}", struPreviewOut.lSessionID);
            NET_EHOME_PUSHSTREAM_IN struPushInfoIn = new NET_EHOME_PUSHSTREAM_IN();
            struPushInfoIn.read();
            struPushInfoIn.dwSize = struPushInfoIn.size();
            struPushInfoIn.lSessionID = struPreviewOut.lSessionID;
            struPushInfoIn.write();
            NET_EHOME_PUSHSTREAM_OUT struPushInfoOut = new NET_EHOME_PUSHSTREAM_OUT();
            struPushInfoOut.read();
            struPushInfoOut.dwSize = struPushInfoOut.size();
            struPushInfoOut.write();
            if (StreamManager.concurrentMap.get(struPushInfoIn.lSessionID) == null) {
                // "rtmp://localhost:1935/live/ipc"
                StreamManager.concurrentMap.put(struPushInfoIn.lSessionID,
                        new StreamHandler(device.getDeviceId(), null, null, true, completableFuture, frameConsumer));
                log.info("加入concurrentMap deviceId: {}", device.getDeviceId());
            }
            if (!hcisupcms.NET_ECMS_StartPushRealStream(device.getLoginId(), struPushInfoIn, struPushInfoOut)) {
                log.error("NET_ECMS_StartPushRealStream failed, error code: {}", hcisupcms.NET_ECMS_GetLastError());
            } else {
                log.info("NET_ECMS_StartPushRealStream succeed, sessionID: {}", struPushInfoIn.lSessionID);
            }
            return struPushInfoIn.lSessionID;
        }
    }
}
