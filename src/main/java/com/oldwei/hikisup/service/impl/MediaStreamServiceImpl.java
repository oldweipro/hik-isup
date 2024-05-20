package com.oldwei.hikisup.service.impl;

import com.oldwei.hikisup.domain.DeviceCache;
import com.oldwei.hikisup.sdk.SdkService.CmsService.HCISUPCMS;
import com.oldwei.hikisup.sdk.SdkService.StreamService.StreamThread;
import com.oldwei.hikisup.sdk.service.IHCISUPCMS;
import com.oldwei.hikisup.sdk.service.IHikISUPStream;
import com.oldwei.hikisup.sdk.structure.*;
import com.oldwei.hikisup.service.IMediaStreamService;
import com.oldwei.hikisup.util.FileUtil;
import com.oldwei.hikisup.util.GlobalCacheService;
import com.oldwei.hikisup.util.GlobalKeyValueStore;
import com.oldwei.hikisup.util.PropertiesUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaStreamServiceImpl implements IMediaStreamService {
//    private final StreamDemo streamDemo;

    @Override
    @Async("taskExecutor")
    public void openStreamCV(int lLoginID, int lChannel, String deviceId, String liveAddress) {
//        streamDemo.startRealPlayListen_File("out.mp4");
//        streamDemo.RealPlay(lLoginID, lChannel);
        FFmpegFrameGrabber grabber = null;
        FFmpegFrameRecorder recorder = null;
        try {
            grabber = new FFmpegFrameGrabber("out.mp4");
            grabber.start();

            // 创建FFmpegFrameRecorder对象
            recorder = new FFmpegFrameRecorder(liveAddress, grabber.getImageWidth(), grabber.getImageHeight());
            recorder.setVideoCodec(grabber.getVideoCodec());
//            recorder.setFormat(grabber.getFormat());
            recorder.setFormat("flv");
            // set 0 禁用音频
            recorder.setAudioChannels(grabber.getAudioChannels());
            recorder.setAudioCodec(grabber.getAudioCodec());
            recorder.start();
            Frame frame;
            int c = 0;
            while ((frame = grabber.grab()) != null) {
                recorder.record(frame);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (grabber != null) {
                try {
                    grabber.stop();
                    grabber.release();
                } catch (org.bytedeco.javacv.FrameGrabber.Exception e) {
                    e.printStackTrace();
                }
            }
            if (recorder != null) {
                try {
                    recorder.stop();
                    recorder.release();
                } catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
                    e.printStackTrace();
                }
            }
            FileUtil.deleteFile("out.mp4");
        }
        System.out.println("结束预览");
//        streamDemo.StopRealPlay(lLoginID);
    }

    @Override
    @Async("taskExecutor")
    public void openStream(int lLoginID, int lChannel, String deviceId) {
        try {
            PipedOutputStream outputStream = new PipedOutputStream();
            PipedInputStream inputStream = new PipedInputStream(outputStream);
            // 创建并启动读取线程
            String liveAddress = "rtmp://192.168.2.57:15800/rtp/" + deviceId;
            Thread readerThread = new Thread(new StreamThread(inputStream, liveAddress));
            readerThread.start();
//            streamDemo.startRealPlayListen_File("out.mp4");
            // FIXME 注意这里的IChannel，不同设备类型可能不太一样
//            streamDemo.RealPlay(lLoginID, lChannel);
//            while (true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            System.out.println("ooooooooooooooo结束推流oooooooooooooooooo" + deviceId);
            DeviceCache deviceCache = (DeviceCache) GlobalCacheService.getInstance().get(deviceId);
            deviceCache.setIsPushed(0);
            GlobalCacheService.getInstance().put(deviceId, deviceCache);
//            streamDemo.StopRealPlay(lLoginID);
        }
    }

    @Override
    public void deleteStreamCV(int lLoginID) {
//        streamDemo.StopRealPlay(lLoginID);
    }

    private final PropertiesUtil propertiesUtil;
    private final IHikISUPStream hikISUPStream;
    private final IHCISUPCMS ihcisupcms;
//    static int StreamHandle = -1;

    @Async
    @Override
    public void saveStream(int lLoginID, int lChannel, String deviceId) {
        int sessionID = RealPlay(lLoginID, lChannel);
        // 这里只预览20s, 方便demo示例代码的效果演示
//        try {
//            Thread.sleep(1800 * 1000);
//        } catch (InterruptedException e) {
//            log.error("睡眠失败");
//        }
//        log.info("listenPreviewHandle: {}", listenPreviewHandle);
//        StopRealPlay(lLoginID, sessionID, 0, 0, hikISUPStream);
        log.info("结束");
    }

    /**
     * 开启预览
     *
     * @param lLoginID
     * @param lChannel
     * @return sessionID 会话id
     */
    public int RealPlay(int lLoginID, int lChannel) {
        int sessionID = -1; //预览sessionID
        HCISUPCMS.NET_EHOME_PREVIEWINFO_IN_V11 struPreviewInV11 = new HCISUPCMS.NET_EHOME_PREVIEWINFO_IN_V11();
        struPreviewInV11.iChannel = lChannel; //通道号
        struPreviewInV11.dwLinkMode = 0; //0- TCP方式，1- UDP方式
        struPreviewInV11.dwStreamType = 0; //码流类型：0- 主码流，1- 子码流, 2- 第三码流
        struPreviewInV11.struStreamSever.szIP = propertiesUtil.readValue("SmsServerIP").getBytes();//流媒体服务器IP地址,公网地址
        struPreviewInV11.struStreamSever.wPort = Short.parseShort(propertiesUtil.readValue("SmsServerPort")); //流媒体服务器端口，需要跟服务器启动监听端口一致
        struPreviewInV11.write();
        //预览请求
        NET_EHOME_PREVIEWINFO_OUT struPreviewOut = new NET_EHOME_PREVIEWINFO_OUT();
        boolean getRS = ihcisupcms.NET_ECMS_StartGetRealStreamV11(lLoginID, struPreviewInV11, struPreviewOut);
        //Thread.sleep(10000);
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
        HCISUPCMS.NET_EHOME_PUSHSTREAM_OUT struPushInfoOut = new HCISUPCMS.NET_EHOME_PUSHSTREAM_OUT();
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
//        if (!hikISUPStream.NET_ESTREAM_StopPreview(lPreviewHandle)) {
//            log.error("NET_ESTREAM_StopPreview failed,err = {}", hikISUPStream.NET_ESTREAM_GetLastError());
//            return;
//        }
        log.info("停止Stream的实时流转发");
        if (!ihcisupcms.NET_ECMS_StopGetRealStream(lLoginID, sessionID)) {
            log.error("NET_ECMS_StopGetRealStream failed,err = {}", ihcisupcms.NET_ECMS_GetLastError());
            return;
        }
//        log.info("CMS发送预览停止请求");
//        if (!hikISUPStream.NET_ESTREAM_StopListenPreview(lListenHandle)) {
//            log.error("NET_ECMS_StopGetRealStream failed,err = {}", ihcisupcms.NET_ECMS_GetLastError());
//            return;
//        }
        log.info("CMS发送预览停止请求");
    }
}
