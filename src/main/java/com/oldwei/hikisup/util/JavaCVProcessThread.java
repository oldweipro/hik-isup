package com.oldwei.hikisup.util;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class JavaCVProcessThread extends Thread {
    private FFmpegFrameGrabber grabber = null;
    private FFmpegFrameRecorder recorder = null;
    private PipedInputStream pin;
    private PipedOutputStream pout;
    private final String pushStreamUrl;
    private boolean shouldBeNull = false;
    public boolean getShouldBeNull() {
        return this.shouldBeNull;
    }

    /**
     * 创建用于把字节数组转换为inputstream流的管道流
     *
     * @throws IOException
     */
    public JavaCVProcessThread(String pushStreamUrl) throws IOException {
        pout = new PipedOutputStream();
        pin = new PipedInputStream(pout);
        System.out.println("推送地址：" + pushStreamUrl);
        this.pushStreamUrl = pushStreamUrl;
    }

    /**
     * 异步接收海康/大华/宇视设备sdk回调实时视频裸流数据
     *
     * @param data
     * @param size
     */
    public void push(byte[] data, int size) {
        try {
            pout.write(data, 0, size);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            grabber = new FFmpegFrameGrabber(pin, 0);
            grabber.start();
            recorder = new FFmpegFrameRecorder(pushStreamUrl, 1920, 1080);
            System.out.println("视频编码：" + grabber.getVideoCodec());
            recorder.setVideoCodec(grabber.getVideoCodec());
            // 一般都是非flv编码格式，如mpeg，推流的情况下使用flv，所以这里写死为flv
            recorder.setFormat("flv");
            if (grabber.getAudioChannels() != 0) {
                System.out.println("音频信息：" + grabber.getAudioCodec());
                recorder.setAudioCodec(grabber.getAudioCodec());
            }
            // set 0 禁用音频
            recorder.setAudioChannels(grabber.getAudioChannels());
            recorder.start();
            Frame frame;
            while ((frame = grabber.grabFrame()) != null) {
                recorder.record(frame);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                grabber.close();
                recorder.close();
                pout.close();
                pin.close();
                pout = null;
                pin = null;
                shouldBeNull = true;
                System.out.println("关流成功");
            } catch (IOException e) {
                System.out.println("关流失败");
                e.printStackTrace();
            }
        }
        shouldBeNull = true;
        System.out.println("线程run结束");
    }
}
