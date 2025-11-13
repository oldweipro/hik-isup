package com.oldwei.isup.service.impl;

import com.oldwei.isup.config.HikIsupProperties;
import com.oldwei.isup.config.HikStreamProperties;
import com.oldwei.isup.handler.StreamHandler;
import com.oldwei.isup.model.Device;
import com.oldwei.isup.sdk.StreamManager;
import com.oldwei.isup.sdk.service.HCISUPCMS;
import com.oldwei.isup.sdk.service.IHikISUPStream;
import com.oldwei.isup.sdk.structure.*;
import com.oldwei.isup.service.IDeviceService;
import com.oldwei.isup.service.IMediaStreamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service("mediaStreamService")
@RequiredArgsConstructor
public class MediaStreamServiceImpl implements IMediaStreamService {

    private final HikIsupProperties hikIsupProperties;
    private final IHikISUPStream hikISUPStream;
    private final HCISUPCMS hcisupcms;
    private final IDeviceService deviceService;
    private final HikStreamProperties hikStreamProperties;

    @Override
    public void preview(Device device) {
        try {
            // 创建异步控制器
            CompletableFuture<String> completableFuture = new CompletableFuture<>();
            RealPlay(device, completableFuture);
            String result = completableFuture.get();
            log.info("异步结果是: {}", result);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void stopPreview(Device device) {
        Integer loginId = device.getLoginId();
        Integer sessionId = StreamManager.userIDandSessionMap.get(loginId);
        Integer previewHandleId = StreamManager.sessionIDAndPreviewHandleMap.get(sessionId);
        // 确保资源被正确清理
        log.info("停止预览：PreviewHandle: {}", previewHandleId);
        if (!hikISUPStream.NET_ESTREAM_StopPreview(previewHandleId)) {
            log.error("NET_ESTREAM_StopPreview failed,err = {}", hikISUPStream.NET_ESTREAM_GetLastError());
        }
        //停止预览,Stream服务停止实时流转发，CMS向设备发送停止预览请求
        log.info("停止获取实时流");
        if (!hcisupcms.NET_ECMS_StopGetRealStream(loginId, sessionId)) {
            log.error("NET_ECMS_StopGetRealStream failed,err = {}", hcisupcms.NET_ECMS_GetLastError());
        }
        // 销毁streamHandler对象
        StreamHandler streamHandler = StreamManager.concurrentMap.get(sessionId);
        if (streamHandler != null) {
            streamHandler.stopProcessing();
            StreamManager.concurrentMap.remove(sessionId);
        }
        StreamManager.sessionIDAndPreviewHandleMap.remove(sessionId);
        device.setIsPush(-1);
        deviceService.updateById(device);
        if (!StreamManager.concurrentMap.containsKey(sessionId)
                && !StreamManager.previewHandSAndSessionIDandMap.containsKey(previewHandleId)
                && !StreamManager.userIDandSessionMap.containsKey(loginId)
                && !StreamManager.sessionIDAndPreviewHandleMap.containsKey(sessionId)) {
            log.info("会话: {} 相关资源已被清空", sessionId);
        }
        log.info("CMS已发送停止预览请求");
    }

    /**
     * 开启预览
     *
     * @return sessionID 会话id
     */
    public void RealPlay(Device device, CompletableFuture<String> completableFuture) {
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
        } else {
            struPreviewOut.read();
            log.info("NET_ECMS_StartGetRealStream succeed, sessionID: {}", struPreviewOut.lSessionID);
            device.setPreviewSessionId(struPreviewOut.lSessionID);
            deviceService.updateById(device);
            log.info("sessionID: {}", device.getPreviewSessionId());
            NET_EHOME_PUSHSTREAM_IN struPushInfoIn = new NET_EHOME_PUSHSTREAM_IN();
            struPushInfoIn.read();
            struPushInfoIn.dwSize = struPushInfoIn.size();
            struPushInfoIn.lSessionID = struPreviewOut.lSessionID;
            struPushInfoIn.write();
            NET_EHOME_PUSHSTREAM_OUT struPushInfoOut = new NET_EHOME_PUSHSTREAM_OUT();
            struPushInfoOut.read();
            struPushInfoOut.dwSize = struPushInfoOut.size();
            struPushInfoOut.write();
            if (!hcisupcms.NET_ECMS_StartPushRealStream(device.getLoginId(), struPushInfoIn, struPushInfoOut)) {
                log.error("NET_ECMS_StartPushRealStream failed, error code: {}", hcisupcms.NET_ECMS_GetLastError());
            } else {
                log.info("NET_ECMS_StartPushRealStream succeed, sessionID: {}", struPushInfoIn.lSessionID);
                if (StreamManager.concurrentMap.get(struPushInfoIn.lSessionID) == null) {
                    StreamManager.userIDandSessionMap.put(device.getLoginId(), struPushInfoIn.lSessionID);
                }
                if (StreamManager.concurrentMap.get(struPushInfoIn.lSessionID) == null) {
                    // "rtmp://localhost:1935/live/ipc"
                    String rtmpUrl = "rtmp://" + hikStreamProperties.getRtmp().getListenIp() + ":" + hikStreamProperties.getRtmp().getPort() + "/live/ipc_" + device.getDeviceId();
                    log.info("rtmp推流地址: {}", rtmpUrl);
                    StreamManager.concurrentMap.put(struPushInfoIn.lSessionID, new StreamHandler(rtmpUrl, completableFuture));
                    log.info("加入concurrentMap deviceId: {}", device.getDeviceId());
                }
            }
        }
    }

    @Override
    public void playbackByTime(Integer loginId, Integer channelId, String startTime, String endTime) {
        NET_EHOME_PLAYBACK_INFO_IN m_struPlayBackInfoIn = new NET_EHOME_PLAYBACK_INFO_IN();
        m_struPlayBackInfoIn.read();
        m_struPlayBackInfoIn.dwSize = m_struPlayBackInfoIn.size();
        m_struPlayBackInfoIn.dwChannel = channelId; //通道号
        m_struPlayBackInfoIn.byPlayBackMode = 1;//0- 按文件名回放，1- 按时间回放
        m_struPlayBackInfoIn.unionPlayBackMode.setType(NET_EHOME_PLAYBACKBYTIME.class);
        // FIXME 这里的时间参数需要根据实际设备上存在的时间段进行设置, 否则可能可能提示：3505 - 该时间段内无录像。
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStartTime.wYear = 2025;
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStartTime.byMonth = 11;
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStartTime.byDay = 8;
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStartTime.byHour = 11;
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStartTime.byMinute = 3;
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStartTime.bySecond = 0;

        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStopTime.wYear = 2025;
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStopTime.byMonth = 11;
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStopTime.byDay = 9;
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStopTime.byHour = 11;
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStopTime.byMinute = 3;
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStopTime.bySecond = 30;

        System.arraycopy(hikIsupProperties.getSmsBackServer().getIp().getBytes(), 0, m_struPlayBackInfoIn.struStreamSever.szIP,
                0, hikIsupProperties.getSmsBackServer().getIp().length());
        m_struPlayBackInfoIn.struStreamSever.wPort = Short.parseShort(hikIsupProperties.getSmsBackServer().getPort());
        m_struPlayBackInfoIn.write();
        NET_EHOME_PLAYBACK_INFO_OUT m_struPlayBackInfoOut = new NET_EHOME_PLAYBACK_INFO_OUT();
        m_struPlayBackInfoOut.write();
        if (!hcisupcms.NET_ECMS_StartPlayBack(loginId, m_struPlayBackInfoIn, m_struPlayBackInfoOut)) {
            System.out.println("NET_ECMS_StartPlayBack failed, error code:" + hcisupcms.NET_ECMS_GetLastError());
            return;
        } else {
            m_struPlayBackInfoOut.read();
            System.out.println("NET_ECMS_StartPlayBack succeed, lSessionID:" + m_struPlayBackInfoOut.lSessionID);
        }

        NET_EHOME_PUSHPLAYBACK_IN m_struPushPlayBackIn = new NET_EHOME_PUSHPLAYBACK_IN();
        m_struPushPlayBackIn.read();
        m_struPushPlayBackIn.dwSize = m_struPushPlayBackIn.size();
        m_struPushPlayBackIn.lSessionID = m_struPlayBackInfoOut.lSessionID;
        m_struPushPlayBackIn.write();

        // TODO sessionID需要保存起来，停止回放时使用
        StreamManager.backSessionID = m_struPushPlayBackIn.lSessionID;

        NET_EHOME_PUSHPLAYBACK_OUT m_struPushPlayBackOut = new NET_EHOME_PUSHPLAYBACK_OUT();
        m_struPushPlayBackOut.read();
        m_struPushPlayBackOut.dwSize = m_struPushPlayBackOut.size();
        m_struPushPlayBackOut.write();

        if (!hcisupcms.NET_ECMS_StartPushPlayBack(loginId, m_struPushPlayBackIn, m_struPushPlayBackOut)) {
            System.out.println("NET_ECMS_StartPushPlayBack failed, error code:" + hcisupcms.NET_ECMS_GetLastError());
            return;
        } else {
            System.out.println("NET_ECMS_StartPushPlayBack succeed, sessionID:" + m_struPushPlayBackIn.lSessionID + ",lUserID:" + loginId);
        }
    }

    @Override
    public void stopPlayBackByTime(Integer loginId) {
        if (!hcisupcms.NET_ECMS_StopPlayBack(loginId, StreamManager.backSessionID)) {
            System.out.println("NET_ECMS_StopPlayBack failed,err = " + hcisupcms.NET_ECMS_GetLastError());
            return;
        }
        System.out.println("CMS发送回放停止请求");
        if (!hikISUPStream.NET_ESTREAM_StopPlayBack(StreamManager.m_lPlayBackLinkHandle)) {
            System.out.println("NET_ESTREAM_StopPlayBack failed,err = " + hikISUPStream.NET_ESTREAM_GetLastError());
            return;
        }
        System.out.println("停止回放Stream服务的实时流转发");
    }
}
