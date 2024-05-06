package com.oldwei.hikisup.util;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import static org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_H264;

public class JavaCVProcessThread extends Thread {
    FFmpegFrameGrabber grabber = null;
    FFmpegFrameRecorder recorder = null;
    private boolean isPinClosed = false; // 新增标志用于跟踪pin流状态
    PipedInputStream pin;
    PipedOutputStream pout;

    /**
     * 创建用于把字节数组转换为inputstream流的管道流
     * @throws IOException
     */
    public JavaCVProcessThread() throws IOException {
        pout = new PipedOutputStream();
        pin = new PipedInputStream(pout);
    }

    /**
     * 异步接收海康/大华/宇视设备sdk回调实时视频裸流数据
     * @param data
     * @param size
     */
    public void push(byte[] data, int size) {
        try {
            if (data.length == 0) {
                System.out.println("没了");
            }
            pout.write(data,0,size);
        } catch (IOException e) {
//            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            grabber = new FFmpegFrameGrabber(pin, 0);
            grabber.start();
            String liveAddress = "rtmp://192.168.2.57:15800/rtp/livehime";
            recorder = new FFmpegFrameRecorder(liveAddress, 1920, 1080);
                recorder.setVideoCodec(grabber.getVideoCodec());
            System.out.println(grabber.getVideoCodec());
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
//            byte[] buffer = new byte[1024]; // 用于读取的缓冲区
//            int bytesRead;
//            while (!isPinClosed) {
//                System.out.println("什么玩意");
//                try {
//                    bytesRead = pin.read(buffer); // 读取输入流
//                    if (bytesRead == -1) { // 如果读取到流末尾，设置流关闭状态并退出循环
//                        isPinClosed = true;
//                        System.out.println("没了结束");
//                        break;
//                    }
//                } catch (IOException e) {
//                    System.out.println("Error reading from pin: " + e.getMessage());
//                    isPinClosed = true; // 设置流关闭状态
//                }
//                recorder.record(grabber.grabFrame());
//            }
        } catch (IOException e) {
            System.out.println("通道关闭");
        } finally {
            try {
                grabber.close();
                recorder.close();
                pout.close();
                pin.close();
                System.out.println("关流成功");
            } catch (IOException e) {
                System.out.println("关流失败");
                e.printStackTrace();
            }
        }
    }
}
