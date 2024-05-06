package com.oldwei.hikisup.controller;

import com.oldwei.hikisup.sdk.SdkService.StreamService.StreamDemo;
import lombok.RequiredArgsConstructor;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/demo")
public class DemoController {

    private final StreamDemo streamDemo;

    @GetMapping("/rtmp")
    public void rtmp() {
        FFmpegFrameGrabber grabber = null;
        FFmpegFrameRecorder recorder = null;
        String filePath = "C:\\Users\\klf\\IdeaProjects\\hik-isup\\output.mp4";
        String rtmpUrl = "rtmp://192.168.2.57:1935/rtp/livehime";
        try {
            grabber = new FFmpegFrameGrabber(filePath);
            grabber.start();

            recorder = new FFmpegFrameRecorder(rtmpUrl, grabber.getImageWidth(), grabber.getImageHeight());
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            recorder.setFormat("flv");
            recorder.start();
            Frame frame;
            while ((frame = grabber.grabFrame()) != null) {
                // 推送视频帧到 RTMP 服务器
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
        }
    }
}

