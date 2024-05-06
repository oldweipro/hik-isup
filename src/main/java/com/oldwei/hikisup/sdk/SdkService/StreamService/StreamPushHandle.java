package com.oldwei.hikisup.sdk.SdkService.StreamService;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FFmpegLogCallback;
import org.bytedeco.javacv.Frame;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * 推流处理
 *
 * @author lidaofu
 * @since 2023/11/10
 **/
@Slf4j
public class StreamPushHandle {

    private FFmpegFrameGrabber grabber = null;
    private FFmpegFrameRecorder recorder = null;
    private final PipedOutputStream outputStream;
    private final PipedInputStream inputStream;
    private final String pushAddress;
    private AVPacket avPacket = null;
    private Frame frame = null;
    private double frameRate = 25.0;



    public StreamPushHandle() {
        this.pushAddress = "rtmp://192.168.2.3:1935/livehime";
        this.outputStream = new PipedOutputStream();
        this.inputStream = new PipedInputStream(1024);
        try {
            //建立管道连接
            this.inputStream.connect(this.outputStream);
        } catch (IOException e) {
            System.out.println("创建输入管道失败");
        }
    }


    /**
     * 异步接收海康/大华/宇视设备sdk回调实时视频裸流数据
     */
    public void write(byte[] data,int dwBufSize) {
        try {
            this.outputStream.write(data, 0, dwBufSize);
        } catch (IOException e) {
            System.out.println("写入数据失败");
        }
    }



    /**
     * 推流
     */
    public void push() {
        try {
            FFmpegLogCallback.setLevel(avutil.AV_LOG_ERROR);
            grabber = new FFmpegFrameGrabber(this.inputStream, 0);
            //有些码率什么可以自己设置、不过没有必要
            grabber.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            // 设置读取的最大数据，单位字节 为了加快首播速度
            grabber.setOption("probesize", "8192");
            // 设置分析的最长时间，单位微秒 为了加快首播速度
            grabber.setOption("analyzeduration", "1000000");
            // 5秒超时 单位微秒
            grabber.setOption("stimeout", "5000000");
            // 5秒超时 单位微秒
            grabber.setOption("rw_timeout", "5000000");
            // 设置缓存大小，提高画质、减少卡顿花屏
            grabber.setOption("buffer_size", "1024000");
            grabber.start();
            // 部分监控设备流信息里携带的帧率为9000，如出现此问题，会导致dts、pts时间戳计算失败，播放器无法播放，故出现错误的帧率时，默认为25帧
            if (grabber.getFrameRate() > 0 && grabber.getFrameRate() < 100) {
                frameRate = grabber.getFrameRate();
            }
            recorder = new FFmpegFrameRecorder(this.pushAddress, grabber.getImageWidth(), grabber.getImageHeight());
            recorder.setFormat("flv");
            recorder.setInterleaved(true);
            recorder.setVideoOption("preset", "ultrafast");
            recorder.setVideoOption("tune", "zerolatency");
            recorder.setVideoOption("crf", "25");
            recorder.setSampleRate(grabber.getSampleRate());
            recorder.setFrameRate(grabber.getFrameRate());
            recorder.setVideoBitrate(grabber.getVideoBitrate());
            int videoIndex=0;
            AVFormatContext context = grabber.getFormatContext();
            for (int i = 0; i < context.nb_streams(); i++) {
                if (context.streams(i).codecpar().codec_type()==avutil.AVMEDIA_TYPE_VIDEO){
                    videoIndex=i;
                }
            }
            //需要等待的时间
            long waitTime = (long) (1000 / frameRate);
            //h264只需要转封装
            if (grabber.getVideoCodec() == avcodec.AV_CODEC_ID_H264) {
                recorder.start(context);
                //初次执行时间
                long exStartTime = System.currentTimeMillis();
                while ((avPacket = grabber.grabPacket()) != null) {
                    recorder.recordPacket(avPacket);
                }
            } else {
                if (grabber.getAudioChannels() > 0) {
                    recorder.setAudioChannels(grabber.getAudioChannels());
                    recorder.setAudioBitrate(grabber.getAudioBitrate());
                    recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
                }
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                //使用libx264加速解码 注意需要使用gpl版的 不然还是默认是思科的h264编解码器
                recorder.setVideoCodecName("libx264");
                recorder.start();
                //初次执行时间
                long exStartTime = System.currentTimeMillis();
                while ((frame = grabber.grab()) != null) {
                    recorder.record(frame);
                }
            }
        } catch (Exception e) {
            log.warn("【FFMPEG】推送SDK流失败 推流地址：{}", pushAddress);
        } finally {
            //回调SDK关流
//            streamClose.closeStream(handleId);
            try {
                if (recorder != null) {
                    recorder.close();
                }
                if (grabber != null) {
                    grabber.close();
                }
            } catch (Exception e) {
                System.out.println("关闭取流器失败");
            }
        }
    }


}


