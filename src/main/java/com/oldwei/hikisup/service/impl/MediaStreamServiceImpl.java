package com.oldwei.hikisup.service.impl;

import com.oldwei.hikisup.domain.DeviceCache;
import com.oldwei.hikisup.sdk.SdkService.StreamService.StreamDemo;
import com.oldwei.hikisup.sdk.SdkService.StreamService.StreamThread;
import com.oldwei.hikisup.service.IMediaStreamService;
import com.oldwei.hikisup.util.FileUtil;
import com.oldwei.hikisup.util.GlobalCacheService;
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
    private final StreamDemo streamDemo;

    @Override
    @Async("taskExecutor")
    public void openStreamCV(int lLoginID, int lChannel, String deviceId, String liveAddress) {
        streamDemo.startRealPlayListen_File("out.mp4");
        streamDemo.RealPlay(lLoginID, lChannel);
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
            streamDemo.startRealPlayListen_File("out.mp4");
            // FIXME 注意这里的IChannel，不同设备类型可能不太一样
            streamDemo.RealPlay(lLoginID, lChannel);
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
        streamDemo.StopRealPlay(lLoginID);
    }

    @Override
    public void saveStream(int lLoginID, int lChannel, String deviceId) {
        streamDemo.startRealPlayListen_File(deviceId + ".mp4");
        streamDemo.RealPlay(lLoginID, lChannel);
        // 这里只预览20s, 方便demo示例代码的效果演示
        try {
            Thread.sleep(300 * 1000);
        } catch (InterruptedException e) {
            log.error("睡眠失败");
        }

        streamDemo.StopRealPlay(0);
    }
}
