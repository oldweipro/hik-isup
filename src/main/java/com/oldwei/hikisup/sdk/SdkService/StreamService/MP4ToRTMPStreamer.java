package com.oldwei.hikisup.sdk.SdkService.StreamService;

import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;

public class MP4ToRTMPStreamer {

    public static void main(String[] args) {
        // 输入文件路径
        String inputFile = "C:\\Users\\klf\\Documents\\bin\\previewVideo.mp4";
        // RTMP服务器地址
        String rtmpServer = "rtmp://192.168.2.3:1935/livehime";

        while (true) {
            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile);
            try {
                grabber.setPixelFormat(avutil.AV_PIX_FMT_YUV420P); // 设置像素格式
                grabber.start();
                FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(rtmpServer, grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels());
                recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P); // 设置像素格式
                recorder.setInterleaved(true);
                recorder.setFormat("flv");
                recorder.setVideoCodecName("libx264");
                recorder.setAudioCodecName("aac");
                recorder.start();

                Frame frame;
                while ((frame = grabber.grabFrame()) != null) {
                    recorder.record(frame);
                }

                recorder.stop();
                grabber.stop();
            } catch (FrameGrabber.Exception | FrameRecorder.Exception e) {
                e.printStackTrace();
            }
        }
    }
}