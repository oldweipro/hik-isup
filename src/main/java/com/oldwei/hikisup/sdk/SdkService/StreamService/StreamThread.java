package com.oldwei.hikisup.sdk.SdkService.StreamService;

import com.oldwei.hikisup.domain.DeviceCache;
import com.oldwei.hikisup.util.GlobalCacheService;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;

import static org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_H264;

public class StreamThread implements Runnable {
    private PipedInputStream inputStream;
        private final String liveAddress;

        public StreamThread(PipedInputStream inputStream, String liveAddress) {
            this.inputStream = inputStream;
            this.liveAddress = liveAddress;
        }

        @Override
        public void run() {
            FFmpegFrameGrabber grabber = null;
            FFmpegFrameRecorder recorder = null;
            try {
                grabber = new FFmpegFrameGrabber(inputStream, 0);
                grabber.start();
                // 创建FFmpegFrameRecorder对象
                recorder = new FFmpegFrameRecorder(liveAddress, 1920, 1080);
//                recorder.setVideoCodec(grabber.getVideoCodec());
                System.out.println(grabber.getVideoCodec());
                recorder.setVideoCodec(AV_CODEC_ID_H264);
//                recorder.setFormat(grabber.getFormat());
                System.out.println(grabber.getFormat());
                recorder.setFormat("flv");
//                System.out.println(grabber.getAudioChannels());
                // set 0 禁用音频
//                recorder.setAudioChannels(0);
//                recorder.setAudioCodec(grabber.getAudioCodec());
                recorder.start();
                Frame frame;
                while ((frame = grabber.grabFrame()) != null) {
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
        public static void rtmp(PipedInputStream inputStream, String liveAddress) {
            FFmpegFrameGrabber grabber = null;
            FFmpegFrameRecorder recorder = null;
            try {
                grabber = new FFmpegFrameGrabber(inputStream, 200);
                grabber.start();
                // 创建FFmpegFrameRecorder对象
                recorder = new FFmpegFrameRecorder(liveAddress, 1920, 1080);
//                recorder.setVideoCodec(grabber.getVideoCodec());
                System.out.println(grabber.getVideoCodec());
                recorder.setVideoCodec(AV_CODEC_ID_H264);
//                recorder.setFormat(grabber.getFormat());
                System.out.println(grabber.getFormat());
                recorder.setFormat("flv");
//                System.out.println(grabber.getAudioChannels());
                // set 0 禁用音频
//                recorder.setAudioChannels(0);
//                recorder.setAudioCodec(grabber.getAudioCodec());
                recorder.start();
                Frame frame;
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
            }
        }
        public static void saveMp4(PipedInputStream inputStream) {
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream("output.mp4", true);
                byte[] buffer = new byte[1024]; // 使用一个较大的缓冲区
                while (true) {
                    // 从管道读取数据
                    int bytesRead = inputStream.read(buffer);
                    if (bytesRead == -1) {
                        break; // 到达流的末尾，退出循环
                    }
                    // 写入到文件
                    fileOutputStream.write(buffer, 0, bytesRead);
                    fileOutputStream.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }