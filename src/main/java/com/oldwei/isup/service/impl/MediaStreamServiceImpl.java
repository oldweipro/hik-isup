package com.oldwei.isup.service.impl;

import com.aizuda.zlm4j.core.ZLMApi;
import com.aizuda.zlm4j.structure.MK_RTP_SERVER;
import com.oldwei.isup.config.HikIsupProperties;
import com.oldwei.isup.config.HikStreamProperties;
import com.oldwei.isup.model.Device;
import com.oldwei.isup.sdk.StreamManager;
import com.oldwei.isup.sdk.service.HCISUPCMS;
import com.oldwei.isup.sdk.service.IHikISUPStream;
import com.oldwei.isup.sdk.structure.*;
import com.oldwei.isup.service.DeviceCacheService;
import com.oldwei.isup.service.IMediaStreamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

@Slf4j
@Service("mediaStreamService")
@RequiredArgsConstructor
public class MediaStreamServiceImpl implements IMediaStreamService {

    private final HikIsupProperties hikIsupProperties;
    private final IHikISUPStream hikISUPStream;
    private final HCISUPCMS hcisupcms;
    private final DeviceCacheService deviceCacheService;
    private final HikStreamProperties hikStreamProperties;
    private final ZLMApi zlmApi;
    // 每个设备一个 latch，用于控制阻塞/停止
    private final Map<String, CountDownLatch> latchMap = new ConcurrentHashMap<>();

    // RTP端口管理：起始端口
    private static final int RTP_PORT_START = 30002;
    private static final int RTP_PORT_END = 30100;
    // 存储已分配的端口
    private final Map<Integer, Boolean> allocatedPorts = new ConcurrentHashMap<>();
    // 存储 sessionID 对应的 RTP 端口
    private final Map<Integer, Integer> sessionRtpPortMap = new ConcurrentHashMap<>();

    @Override
    @Async("streamExecutor")
    public void preview(Device device) {
        CountDownLatch latch = new CountDownLatch(1);
        latchMap.put(device.getDeviceId(), latch);
        try {
            // 创建异步控制器
            RealPlay(device);
            // 阻塞，直到 stopPreview() 调用 latch.countDown()
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            latchMap.remove(device.getDeviceId());
        }
    }

    public void stopPreview(Device device) {
        Integer loginId = device.getLoginId();
        Integer sessionId = StreamManager.userIDandSessionMap.get(device.getLoginId() * 100 + device.getChannel());
        Integer previewHandleId = StreamManager.sessionIDAndPreviewHandleMap.get(sessionId);
        // 确保资源被正确清理
        log.info("停止预览：PreviewHandle: {}", previewHandleId);
        if (previewHandleId != null) {
            if (!hikISUPStream.NET_ESTREAM_StopPreview(previewHandleId)) {
                log.error("NET_ESTREAM_StopPreview failed,err = {}", hikISUPStream.NET_ESTREAM_GetLastError());
            }
            StreamManager.previewHandSAndSessionIDandMap.remove(previewHandleId);
        }
        //停止预览,Stream服务停止实时流转发，CMS向设备发送停止预览请求
        log.info("停止获取实时流");
        if (sessionId != null) {
            if (!hcisupcms.NET_ECMS_StopGetRealStream(loginId, sessionId)) {
                log.error("NET_ECMS_StopGetRealStream failed,err = {}", hcisupcms.NET_ECMS_GetLastError());
            }
            // 释放RTP端口
            Integer rtpPort = sessionRtpPortMap.remove(sessionId);
            if (rtpPort != null) {
                releaseRtpPort(rtpPort);
                StreamManager.sessionIDAndRtpPortMap.remove(sessionId);
            }

            StreamManager.sessionIDAndPreviewHandleMap.remove(sessionId);
        }
        StreamManager.userIDandSessionMap.remove(loginId);
        if (!StreamManager.previewHandSAndSessionIDandMap.containsKey(previewHandleId)
                && !StreamManager.userIDandSessionMap.containsKey(loginId)
                && !StreamManager.sessionIDAndPreviewHandleMap.containsKey(sessionId)) {
            log.info("会话: {} 相关资源已被清空", sessionId);
        }
        MK_RTP_SERVER mkRtpServer = StreamManager.deviceRTP.get(device.getDeviceId());
        zlmApi.mk_rtp_server_release(mkRtpServer);
        log.info("CMS已发送停止预览请求");
        CountDownLatch latch = latchMap.get(device.getDeviceId());
        if (latch != null) {
            latch.countDown(); // 唤醒 preview
            log.info("结束预览实例: {}", device.getDeviceId());
        }
    }

    /**
     * 分配一个可用的RTP端口
     *
     * @return 可用端口号，如果无可用端口返回 -1
     */
    private synchronized int allocateRtpPort() {
        for (int port = RTP_PORT_START; port <= RTP_PORT_END; port++) {
            if (!allocatedPorts.getOrDefault(port, false)) {
                allocatedPorts.put(port, true);
                log.info("分配RTP端口: {}", port);
                return port;
            }
        }
        log.error("无可用RTP端口，当前范围: {}-{}", RTP_PORT_START, RTP_PORT_END);
        return -1;
    }

    /**
     * 释放RTP端口
     *
     * @param port 要释放的端口号
     */
    private synchronized void releaseRtpPort(int port) {
        if (allocatedPorts.remove(port) != null) {
            log.info("释放RTP端口: {}", port);
        }
    }

    /**
     * 开启预览
     *
     * @return sessionID 会话id
     */
    public void RealPlay(Device device) {
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
                // 动态分配RTP端口
                int rtpPort = allocateRtpPort();
                if (rtpPort == -1) {
                    log.error("无法分配RTP端口，预览失败");
                    return;
                }

                MK_RTP_SERVER mkRtpServer = zlmApi.mk_rtp_server_create2((short) rtpPort, 1, "__defaultVhost__", "live", device.getDeviceId());
                StreamManager.deviceRTP.put(device.getDeviceId(), mkRtpServer);

                // 保存 sessionID 与 RTP 端口的映射关系，供回调函数使用
                StreamManager.sessionIDAndRtpPortMap.put(struPushInfoIn.lSessionID, rtpPort);
                sessionRtpPortMap.put(struPushInfoIn.lSessionID, rtpPort);

                log.info("NET_ECMS_StartPushRealStream succeed, sessionID: {}, 分配RTP端口: {}", struPushInfoIn.lSessionID, rtpPort);
            }
        }
    }

    @Override
    public void playbackByTime(String deviceId, Integer loginId, Integer channelId, String startTime, String endTime) {
        NET_EHOME_PLAYBACK_INFO_IN m_struPlayBackInfoIn = new NET_EHOME_PLAYBACK_INFO_IN();
        m_struPlayBackInfoIn.read();
        m_struPlayBackInfoIn.dwSize = m_struPlayBackInfoIn.size();
        m_struPlayBackInfoIn.dwChannel = channelId; //通道号
        m_struPlayBackInfoIn.byPlayBackMode = 1;//0- 按文件名回放，1- 按时间回放
        m_struPlayBackInfoIn.unionPlayBackMode.setType(NET_EHOME_PLAYBACKBYTIME.class);
        // 解析时间字符串（支持 "2025-11-12 11:03:00" 或 "2025-11-12T11:03:00"）
        DateTimeFormatter formatter;
        if (startTime.contains("T")) {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        } else {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        }

        LocalDateTime start = LocalDateTime.parse(startTime, formatter);
        LocalDateTime end = LocalDateTime.parse(endTime, formatter);
        // FIXME 这里的时间参数需要根据实际设备上存在的时间段进行设置, 否则可能可能提示：3505 - 该时间段内无录像。
        // 填充开始时间
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStartTime.wYear = (short) start.getYear();
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStartTime.byMonth = (byte) start.getMonthValue();
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStartTime.byDay = (byte) start.getDayOfMonth();
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStartTime.byHour = (byte) start.getHour();
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStartTime.byMinute = (byte) start.getMinute();
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStartTime.bySecond = (byte) start.getSecond();

        // 填充结束时间
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStopTime.wYear = (short) end.getYear();
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStopTime.byMonth = (byte) end.getMonthValue();
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStopTime.byDay = (byte) end.getDayOfMonth();
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStopTime.byHour = (byte) end.getHour();
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStopTime.byMinute = (byte) end.getMinute();
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStopTime.bySecond = (byte) end.getSecond();

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

        NET_EHOME_PUSHPLAYBACK_OUT m_struPushPlayBackOut = new NET_EHOME_PUSHPLAYBACK_OUT();
        m_struPushPlayBackOut.read();
        m_struPushPlayBackOut.dwSize = m_struPushPlayBackOut.size();
        m_struPushPlayBackOut.write();

        if (!hcisupcms.NET_ECMS_StartPushPlayBack(loginId, m_struPushPlayBackIn, m_struPushPlayBackOut)) {
            System.out.println("NET_ECMS_StartPushPlayBack failed, error code:" + hcisupcms.NET_ECMS_GetLastError());
        } else {
            System.out.println("NET_ECMS_StartPushPlayBack succeed, sessionID:" + m_struPushPlayBackIn.lSessionID + ",lUserID:" + loginId);
            // 创建异步控制器
            int loginchannelId = loginId * 100 + channelId;
            StreamManager.playbackLoginchannelIdAndstopflag.put(loginchannelId, false);
            StreamManager.playbackUserIDandSessionMap.put(loginchannelId, m_struPushPlayBackIn.lSessionID);
//                StreamManager.playbackConcurrentMap.put(m_struPushPlayBackIn.lSessionID, new StreamHandler(rtmpUrl, completableFuture, 2, loginchannelId));

            // 动态分配RTP端口
            int rtpPort = allocateRtpPort();
            if (rtpPort == -1) {
                log.error("无法分配RTP端口，预览失败");
                return;
            }
            MK_RTP_SERVER mkRtpServer = zlmApi.mk_rtp_server_create2((short) rtpPort, 1, "__defaultVhost__", "playback", deviceId);
            StreamManager.deviceRTPPlayback.put(deviceId, mkRtpServer);
        }
    }

    @Override
    public void stopPlayBackByTime(Integer loginId) {
        Integer lSessionID = StreamManager.playbackUserIDandSessionMap.get(loginId);
        if (lSessionID != null) {
            if (!hcisupcms.NET_ECMS_StopPlayBack(loginId, lSessionID)) {
                System.out.println("NET_ECMS_StopPlayBack failed,err = " + hcisupcms.NET_ECMS_GetLastError());
                return;
            }
            System.out.println("CMS发送回放停止请求");
            if (!hikISUPStream.NET_ESTREAM_StopPlayBack(StreamManager.playbackSessionIDAndPreviewHandleMap.get(lSessionID))) {
                System.out.println("NET_ESTREAM_StopPlayBack failed,err = " + hikISUPStream.NET_ESTREAM_GetLastError());
                return;
            }
            System.out.println("停止回放Stream服务的实时流转发");

            Integer lPreviewHandle = StreamManager.playbackSessionIDAndPreviewHandleMap.get(lSessionID);
            StreamManager.playbackSessionIDAndPreviewHandleMap.remove(lSessionID);
            StreamManager.playbackPreviewHandSAndSessionIDandMap.remove(lPreviewHandle);
            StreamManager.playbackUserIDandSessionMap.remove(loginId);
            if (!StreamManager.playbackPreviewHandSAndSessionIDandMap.containsKey(lPreviewHandle)
                    && !StreamManager.playbackUserIDandSessionMap.containsKey(loginId)
                    && !StreamManager.playbackSessionIDAndPreviewHandleMap.containsKey(lSessionID)) {
                log.info("会话:{} 相关资源已被清空", lSessionID);
            }
        }
    }

    @Override
    public void voiceTrans(Integer loginId, String fileFullPath) {
        log.info("voiceTrans fileFullPath:{}", fileFullPath);
        //获取设备通道对讲信息，包括编码格式，起始对讲通道号等，跟nvr对讲前先获取
        NET_EHOME_DEVICE_INFO res = getDeviceInfo(loginId);
        //采集本地20s音频保存为pcm
//        makeVoice(20);
        /**
         * ISUP5.0语音转发模块(需要设备在线, 需要确定设备是否支持此功能, 需要实现前面初始化语音流媒体服务的代码)
         */
        int dwVoiceChan = res.byStartDTalkChan + 3;
        byte dwAudioEncType = (byte) res.dwAudioEncType;

        int voiceTalkSessionId = StartVoiceTrans(loginId, dwVoiceChan, dwAudioEncType, fileFullPath);

        StopVoiceTrans(loginId, voiceTalkSessionId, StreamManager.lVoiceLinkHandle);
    }

    private NET_EHOME_DEVICE_INFO getDeviceInfo(Integer loginId) {
        boolean bRet;

        NET_EHOME_DEVICE_INFO ehomeDeviceInfo = new NET_EHOME_DEVICE_INFO();
        ehomeDeviceInfo.read();
        ehomeDeviceInfo.dwSize = ehomeDeviceInfo.size();
        ehomeDeviceInfo.write();

        NET_EHOME_CONFIG strEhomeCfd = new NET_EHOME_CONFIG();
        strEhomeCfd.pCondBuf = null;
        strEhomeCfd.dwCondSize = 0;
        strEhomeCfd.pOutBuf = ehomeDeviceInfo.getPointer();
        strEhomeCfd.dwOutSize = ehomeDeviceInfo.size();
        strEhomeCfd.pInBuf = null;
        strEhomeCfd.dwInSize = 0;
        strEhomeCfd.write();


        bRet = hcisupcms.NET_ECMS_GetDevConfig(loginId, 1, strEhomeCfd.getPointer(), strEhomeCfd.size());
        if (!bRet) {
            int dwErr = hcisupcms.NET_ECMS_GetLastError();
            System.out.println("获取报警输入参数失败，Error:" + dwErr);
        } else {
            //  读取返回的数据
            ehomeDeviceInfo.read();
            System.out.println("语音对讲的音频格式:" + ehomeDeviceInfo.dwAudioEncType);
            System.out.println("起始数字对讲通道号:" + ehomeDeviceInfo.byStartDTalkChan);
        }
        return ehomeDeviceInfo;
    }

    /**
     * 开启语音转发
     * 说明：byEncodingType为获取设备通道对讲信息中返回的编码格式，对应结构体NET_EHOME_DEVICE_INFO.dwAudioEncType;  // 语音对讲的音频格式：0-G.722，1-G.711U，2-G.711A，3-G.726，4-AAC，5-MP2L2。
     * NET_EHOME_DEVICE_INFO.dwAudioEncType为1表示g711u，对应NET_EHOME_TALK_ENCODING_TYPE中为2表示g711u
     * <p>
     * typedef enum tagNET_EHOME_TALK_ENCODING_TYPE{
     * ENUM_ENCODING_START = 0,
     * ENUM_ENCODING_G722_1, = 1
     * ENUM_ENCODING_G711_MU, = 2
     * ENUM_ENCODING_G711_A, = 3
     * ENUM_ENCODING_G723, = 4
     * ENUM_ENCODING_MP1L2, = 5
     * ENUM_ENCODING_MP2L2, = 6
     * ENUM_ENCODING_G726, = 7
     * ENUM_ENCODING_AAC, = 8
     * ENUM_ENCODING_RAW = 100
     * }NET_EHOME_TALK_ENCODING_TYPE;
     */
    private int StartVoiceTrans(Integer loginId, int dwVoiceChan, byte byEncodingType, String filePath) {
        int voiceTalkSessionId = -1;
//        byEncodingType = (byte) (byEncodingType + 1); //这里是将NET_EHOME_DEVICE_INFO.dwAudioEncType跟byEncodingType值对齐，区别见结构体NET_EHOME_DEVICE_INFO和NET_EHOME_TALK_ENCODING_TYPE的定义
        // 语音对讲开启请求的输入参数
        NET_EHOME_VOICE_TALK_IN net_ehome_voice_talk_in = new NET_EHOME_VOICE_TALK_IN();
        net_ehome_voice_talk_in.struStreamSever.szIP = hikIsupProperties.getVoiceSmsServer().getIp().getBytes();
        net_ehome_voice_talk_in.struStreamSever.wPort = Short.parseShort(hikIsupProperties.getVoiceSmsServer().getPort());
        net_ehome_voice_talk_in.dwVoiceChan = dwVoiceChan; //语音通道号,NVR设备起始通道号为3，dwVoiceChan传6对应nvr的通道4
        net_ehome_voice_talk_in.byEncodingType[0] = byEncodingType;  //跟NVR通道对讲必须带上此参数，ENUM_ENCODING_G722_1 = 1；ENUM_ENCODING_G711_MU = 2 ；ENUM_ENCODING_G711_A = 3
//        net_ehome_voice_talk_in.byAudioSamplingRate = 5;
        net_ehome_voice_talk_in.write();
        // 语音对讲开启请求的输出参数
        NET_EHOME_VOICE_TALK_OUT net_ehome_voice_talk_out = new NET_EHOME_VOICE_TALK_OUT();
        // 将语音对讲开启请求从CMS 发送给设备发送SMS 的地址和端口号给设备，设备自动为CMS 分配一个会话ID。
        if (!hcisupcms.NET_ECMS_StartVoiceWithStmServer(loginId, net_ehome_voice_talk_in, net_ehome_voice_talk_out)) {
            System.out.println("NET_ECMS_StartVoiceWithStmServer failed, error code:" + hcisupcms.NET_ECMS_GetLastError());
            return voiceTalkSessionId;
        } else {
            net_ehome_voice_talk_out.read();
            System.out.println("NET_ECMS_StartVoiceWithStmServer suss sessionID=" + net_ehome_voice_talk_out.lSessionID);
        }

        // 语音传输请求的输入参数
        NET_EHOME_PUSHVOICE_IN struPushVoiceIn = new NET_EHOME_PUSHVOICE_IN();
        struPushVoiceIn.dwSize = struPushVoiceIn.size();
        struPushVoiceIn.lSessionID = net_ehome_voice_talk_out.lSessionID;
        voiceTalkSessionId = net_ehome_voice_talk_out.lSessionID;
        struPushVoiceIn.write();
        // 语音传输请求的输出参数
        NET_EHOME_PUSHVOICE_OUT struPushVoiceOut = new NET_EHOME_PUSHVOICE_OUT();
        struPushVoiceOut.dwSize = struPushVoiceOut.size();
        struPushVoiceOut.write();
        // 将语音传输请求从CMS 发送给设备。设备自动连接SMS 并开始发送音频数据给SMS
        if (!hcisupcms.NET_ECMS_StartPushVoiceStream(loginId, struPushVoiceIn, struPushVoiceOut)) {
            System.out.println("NET_ECMS_StartPushVoiceStream failed, error code:" + hcisupcms.NET_ECMS_GetLastError());
            return voiceTalkSessionId;
        }
        System.out.println("NET_ECMS_StartPushVoiceStream success!\n");

        //发送音频数据
        FileInputStream voiceInputStream = null;
        int dataLength = 0;
        try {
            //创建从文件读取数据的FileInputStream流
            voiceInputStream = new FileInputStream(filePath);
            //返回文件的总字节数
            dataLength = voiceInputStream.available();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (dataLength < 0) {
            System.out.println("input file dataSize < 0");
            throw new RuntimeException("输入的文件");
//            return false;
        }

        BYTE_ARRAY ptrVoiceByte = new BYTE_ARRAY(dataLength);
        try {
            voiceInputStream.read(ptrVoiceByte.byValue);
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        ptrVoiceByte.write();
        int iSendData = 160;//G722编码每20毫秒发送80字节，G711编码每20毫秒发送160字节
        for (int i = 0; i < dataLength / iSendData; i++) {
            BYTE_ARRAY ptrG711Send = new BYTE_ARRAY(iSendData);
            System.arraycopy(ptrVoiceByte.byValue, i * iSendData, ptrG711Send.byValue, 0, iSendData);
            ptrG711Send.write();
            NET_EHOME_VOICETALK_DATA struVoicTalkData = new NET_EHOME_VOICETALK_DATA();
            struVoicTalkData.pData = ptrG711Send.getPointer();
            struVoicTalkData.dwDataLen = iSendData;
            struVoicTalkData.write();
            // 将音频数据发送给设备
            if (hikISUPStream.NET_ESTREAM_SendVoiceTalkData(StreamManager.lVoiceLinkHandle, struVoicTalkData) <= -1) {
                System.out.println("NET_ESTREAM_SendVoiceTalkData failed, error code:" + hikISUPStream.NET_ESTREAM_GetLastError());
            }

            //需要实时速率发送数据
            try {
                Thread.sleep(19);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return voiceTalkSessionId;
    }

    /**
     * 停止语音对讲
     */
    private void StopVoiceTrans(int loginId, int voiceTalkSessionId, int lVoiceLinkHandle) {
        //SMS 停止语音对讲
        if (lVoiceLinkHandle >= 0) {
            if (!hikISUPStream.NET_ESTREAM_StopVoiceTalk(lVoiceLinkHandle)) {
                System.out.println("NET_ESTREAM_StopVoiceTalk failed, error code:" + hikISUPStream.NET_ESTREAM_GetLastError());
                return;
            }
        }
        //释放语音对讲请求资源
        if (!hcisupcms.NET_ECMS_StopVoiceTalkWithStmServer(loginId, voiceTalkSessionId)) {
            System.out.println("NET_ECMS_StopVoiceTalkWithStmServer failed, error code:" + hcisupcms.NET_ECMS_GetLastError());
            return;
        }
        log.info("NET_ESTREAM_StopVoiceTalk success!");
    }
}
