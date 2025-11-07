package com.oldwei.isup.handler.strategy;

import com.oldwei.isup.handler.StreamOutputStrategy;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.ffmpeg.avformat.AVStream;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;

/**
 * rtmp推流 - 改进版
 */
@Slf4j
public class RtmpOutputStrategy implements StreamOutputStrategy {
    private final String rtmpUrl;
    private FFmpegFrameRecorder recorder;
    private long startTime = 0;
    private AVStream[] inputStreams;
    private boolean initialized = false;

    public RtmpOutputStrategy(String rtmpUrl) {
        this.rtmpUrl = rtmpUrl;
    }

    @Override
    public void init(FFmpegFrameGrabber grabber, AVFormatContext ifmtCtx) throws Exception {
        try {
            // 验证输入参数
            if (grabber.getImageWidth() <= 0 || grabber.getImageHeight() <= 0) {
                throw new IllegalStateException("Invalid video dimensions");
            }

            recorder = new FFmpegFrameRecorder(rtmpUrl,
                    grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels());

            initCommon(grabber, recorder, "flv");

            // 添加重试机制
            int retryCount = 3;
            Exception lastException = null;

            for (int i = 0; i < retryCount; i++) {
                try {
                    recorder.start(ifmtCtx);
                    initialized = true;
                    log.info("RTMP recorder started successfully on attempt {}", i + 1);

                    // 保存输入流信息用于时间戳转换
                    if (ifmtCtx != null && ifmtCtx.nb_streams() > 0) {
                        inputStreams = new AVStream[ifmtCtx.nb_streams()];
                        for (int j = 0; j < ifmtCtx.nb_streams(); j++) {
                            inputStreams[j] = ifmtCtx.streams(j);
                        }
                    }
                    return;
                } catch (Exception e) {
                    lastException = e;
                    log.warn("Failed to start RTMP recorder on attempt {}: {}", i + 1, e.getMessage());
                    if (i < retryCount - 1) {
                        Thread.sleep(1000); // 等待1秒后重试
                    }
                }
            }

            throw new Exception("Failed to start RTMP recorder after " + retryCount + " attempts", lastException);

        } catch (Exception e) {
            log.error("RTMP初始化失败: {}", e.getMessage(), e);
            close();
            throw e;
        }
    }

    @Override
    public void handlePacket(AVPacket packet) throws Exception {
        if (!initialized || recorder == null) {
            log.warn("Recorder not initialized, skipping packet");
            return;
        }

        if (packet == null || packet.size() <= 0) {
            return;
        }

        try {
            // 设置起始时间
            if (startTime == 0) {
                startTime = System.currentTimeMillis();
            }

            // 记录数据包（FFmpegFrameRecorder会处理时间戳）
            recorder.recordPacket(packet);

        } catch (Exception e) {
            // 检查是否是连接错误
            String errorMsg = e.getMessage();
            if (errorMsg != null && (errorMsg.contains("-10054") ||
                    errorMsg.contains("Connection reset") ||
                    errorMsg.contains("Broken pipe"))) {
                log.error("RTMP连接断开: {}", errorMsg);
                initialized = false;
                throw new Exception("RTMP connection lost: " + errorMsg, e);
            } else {
                log.warn("处理数据包时出错: {}", errorMsg);
                throw e;
            }
        }
    }

    @Override
    public void close() {
        initialized = false;
        try {
            if (recorder != null) {
                recorder.stop();
                recorder.release();
                log.info("RTMP recorder closed successfully");
            }
        } catch (Exception e) {
            log.error("关闭RTMP推流器出错: {}", e.getMessage());
        }
    }

    private void initCommon(FFmpegFrameGrabber grabber, FFmpegFrameRecorder recorder, String format) {
        recorder.setInterleaved(true);
        recorder.setFormat(format);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);

        // 音频设置
//        if (grabber.getAudioChannels() > 0) {
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        recorder.setSampleRate(grabber.getSampleRate() > 0 ? grabber.getSampleRate() : 44100);
        recorder.setAudioBitrate(128000);
//        }

        recorder.setAudioChannels(0);
        recorder.setAudioCodecName("none"); // 防止FFmpeg自动选择音频

        // 视频设置
        double frameRate = grabber.getFrameRate();
        if (frameRate <= 0) {
            frameRate = 25.0; // 默认帧率
        }
        recorder.setFrameRate(frameRate);
        recorder.setVideoBitrate(2000000); // 降低比特率以减少网络压力
        recorder.setGopSize((int) frameRate * 2);
        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);

        // RTMP优化选项
        recorder.setVideoOption("tune", "zerolatency");
        recorder.setVideoOption("preset", "ultrafast"); // 使用ultrafast减少编码延迟
        recorder.setVideoOption("profile", "baseline");

        // 网络选项
        recorder.setOption("rtmp_buffer", "100");
        recorder.setOption("buffer_size", "1024000");

        log.info("Recorder配置: {}x{} @ {} fps, 音频通道: {}, 音频id: {}, 视频id: {}",
                recorder.getImageWidth(), recorder.getImageHeight(),
                frameRate, recorder.getAudioChannels(), recorder.getAudioCodec(), recorder.getVideoCodec());
    }
}