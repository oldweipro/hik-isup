package com.oldwei.isup.util;

import com.oldwei.isup.sdk.StreamManager;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.CompletableFuture;

/**
 * 跳过了解析帧，降低cpu利用率
 */
@Slf4j
public class StreamHandler {
    private PipedOutputStream outputStream;
    private PipedInputStream inputStream;
    private FFmpegFrameGrabber grabber;
    private FFmpegFrameRecorder recorder;
    private volatile boolean running;

    public Thread thread;
    private int count;
    public String pushAddress;
    public Integer streamType; // 1:预览 2:回放
    public Integer loginchannelId;

    private CompletableFuture<String> completableFutureString;

    public StreamHandler(String address, CompletableFuture<String> completableFuture, Integer streamType, Integer loginchannelId) {
        try {
            completableFutureString = completableFuture;
            pushAddress = address;
            outputStream = new PipedOutputStream();
            inputStream = new PipedInputStream(outputStream, 4096 * 5);
            running = true;
            this.streamType = streamType;
            this.loginchannelId = loginchannelId;
            startProcessing();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize piped streams", e);
        }
    }


    public void processStream(byte[] data) {
        // 判断byte[] data是否为空
        if (data == null || data.length == 0) {
            // data 为空
            log.info("收到空数据包，忽略处理");
        }
        if (!running || outputStream == null) {
            log.debug("推流已停止，忽略数据包");
            return;
        }
        try {
            outputStream.write(data);
        } catch (IOException e) {
            if ("Pipe closed".equals(e.getMessage())) {
                log.warn("这里Pipe 已关闭（可能是grabber线程已退出），outputStream是否为null: {}，inputStream是否为null: {}, data数据是否为孔：{}, grabber是否为null:{}", outputStream == null, inputStream == null, data.length == 0, grabber == null);
                running = false;
            } else {
                log.error("写入Pipe异常: {}", e.getMessage(), e);
            }
        }
    }

    private void startProcessing() {
        thread = new Thread(() -> {
            try {
//           打印FFmpeg日志可以帮助确定输入流的音视频编码格式帧率等信息,需要时可以取消注释
//            avutil.av_log_set_level(avutil.AV_LOG_INFO);
//                FFmpegLogCallback.set();
                grabber = new FFmpegFrameGrabber(inputStream, 0);
                grabber.setOption("rtsp_transport", "tcp"); // 设置RTSP传输协议为TCP
//            grabber.setVideoCodec(avcodec.AV_CODEC_ID_H264); // 设置视频编解码器为H.264
//            grabber.setAudioCodec(avcodec.AV_CODEC_ID_AAC); // 设置音频编解码器为ACC
                grabber.setFormat("mpeg"); // 设置格式为MPEG
                grabber.start();

                // 获取输入格式上下文
                AVFormatContext ifmt_ctx = grabber.getFormatContext();

                log.info("视频宽度: {}", grabber.getImageWidth());
                log.info("视频高度: {}", grabber.getImageHeight());
                log.info("音频通道: {}", grabber.getAudioChannels());

                recorder = new FFmpegFrameRecorder(pushAddress, grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels());
                recorder.setInterleaved(true);  // 设置音视频交织方式
                recorder.setVideoOption("crf", "23"); //画质参数
                recorder.setFormat("flv");  // 设置推流格式为 FLV
//                recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);  // 设置音频编码器为 AAC
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);  // 设置视频编码器为 H.264

                recorder.setSampleRate(grabber.getSampleRate());  // 设置音频采样率
                recorder.setFrameRate(grabber.getFrameRate()); //设置视频帧率
                recorder.setVideoBitrate(3000000);  // 设置视频比特率为 3 Mbps（根据需要调整）
//                recorder.setVideoQuality(0);  // 设置视频质量参数（0为最高质量）
//                recorder.setAudioQuality(0);  // 设置音频质量参数（0为最高质量）
                recorder.setGopSize((int) (grabber.getFrameRate() * 2));
                recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
                recorder.setVideoOption("tune", "zerolatency"); // 降低编码延迟
                recorder.setVideoOption("preset", "superfast"); // 提升编码速度

                recorder.start(ifmt_ctx);  // 启动推流器
                Frame frame;

                count = 0;

                long t1 = System.currentTimeMillis();
                AVPacket packet;
                while (running && (packet = grabber.grabPacket()) != null) {
                    count++;
                    recorder.recordPacket(packet);
                    completableFutureString.complete("true");//运行到这说明推流成功了
//                    if (count % 100 == 0) {
//                        // 处理每帧
//                        log.info("packet推流帧====>{}", count);
//                    }
                }
                log.info("所以这里是推流结束了====>，总帧数: {}, 用时: {} ms", count, (System.currentTimeMillis() - t1));
                log.info("那么running的状态是：{}, grabPacket是不是为空呢: {}", running, grabber.grabPacket() == null);
            } catch (Exception e) {
                completableFutureString.complete("false");//运行到这说明推流异常,需要反馈到前端
                log.error("推流线程异常: {}", e.getMessage());
            } finally {
                try {
                    if (grabber != null) {
                        grabber.stop();
                        grabber.release();
                    }
                    if (recorder != null) {
                        recorder.stop();
                        recorder.release();
                    }
                } catch (Exception e) {
                    log.warn("销毁捕流器和推流器异常:: {}", e.getMessage());
                } finally {
                    try {
                        log.warn("inputStream 和 outputStream 关闭");
                        inputStream.close();
                        outputStream.close();
                    } catch (IOException e) {
                        log.error(e.getMessage());
                    }
                }
                // 定时任务会去刷这个状态来决定是否关闭推流相关
                if (streamType != null && streamType == 1) {
                    StreamManager.loginchannelIdAndstopflag.put(loginchannelId, true);
                } else if (streamType != null && streamType == 2) {
                    StreamManager.playbackLoginchannelIdAndstopflag.put(loginchannelId, true);
                }
            }
        });
        thread.start();
    }

    public void stopProcessing() {
        running = false;
        if (thread != null) {
            thread.interrupt();
        }
        log.info("已关闭javacv视频处理线程");
    }
}
