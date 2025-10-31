package com.oldwei.isup.handler;

import com.oldwei.isup.handler.strategy.RtmpOutputStrategy;
import com.oldwei.isup.handler.strategy.WebSocketOutputStrategy;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Slf4j
public class StreamHandler {
    private final PipedOutputStream outputStream = new PipedOutputStream();
    public PipedInputStream inputStream;
    private final List<StreamOutputStrategy> strategies = new ArrayList<>();
    private FFmpegFrameGrabber grabber;
    private volatile boolean running = false;
    public CompletableFuture<String> completableFutureOne;
    private Thread thread;
    private int count;
    private boolean enableWebSocket;
    private String rtmpUrl;
    private String hlsDir;
    private Consumer<byte[]> frameConsumer;
    private String playKey;

    /**
     * 初始化推流
     *
     * @param rtmpUrl              rtmp地址
     * @param hlsDir               hls地址
     * @param enableWebSocket      是否启用websocket
     * @param completableFutureOne 推流完成回调
     * @param frameConsumer        视频帧回调
     */
    public StreamHandler(
            String playKey,
            String rtmpUrl,
            String hlsDir,
            boolean enableWebSocket,
            CompletableFuture<String> completableFutureOne,
            Consumer<byte[]> frameConsumer
    ) {
        try {
            this.playKey = playKey;
            this.enableWebSocket = enableWebSocket;
            this.rtmpUrl = rtmpUrl;
            this.hlsDir = hlsDir;
            this.frameConsumer = frameConsumer;
            this.completableFutureOne = completableFutureOne;
            inputStream = new PipedInputStream(outputStream);
            running = true;
            // 启动推流主循环
            pushLoop();
        } catch (Exception e) {
            log.error("HandleStreamV2 初始化异常：{}", e.getMessage(), e);
            close();
        }
    }

    /**
     * 推流主循环
     */
    private void pushLoop() {
        thread = new Thread(() -> {
            try {
                log.info("初始化 FFmpegFrameGrabber");
                grabber = new FFmpegFrameGrabber(inputStream, 0);
                log.info("Starting FFmpegFrameGrabber...");
                grabber.setOption("probesize", "1024");       // 默认5MB，调小可加快探测
                grabber.start();
                log.info("FFmpegFrameGrabber started");

                AVFormatContext ifmtCtx = grabber.getFormatContext();

                // 根据配置添加推流策略
                if (enableWebSocket) {
                    strategies.add(new WebSocketOutputStrategy(playKey, frameConsumer));
                }
                if (rtmpUrl != null && !rtmpUrl.isEmpty()) {
                    strategies.add(new RtmpOutputStrategy(rtmpUrl));
                }
                log.info("==========Initialized {} output strategies.", strategies.size());
                // 初始化所有推流策略
                for (StreamOutputStrategy strategy : strategies) {
                    strategy.init(grabber, ifmtCtx);
                }

                AVPacket pkt;
                count = 0;
                while (running) {
                    pkt = grabber.grabPacket();
                    if (pkt == null) {
                        log.warn("No data packet received, waiting...");
                        Thread.sleep(100); // 等待 100 毫秒，防止 CPU 过高
                        continue;
                    }
                    if (pkt.size() == 0) {
                        log.warn("Empty packet received, skipping...");
                        Thread.sleep(100); // 等待 100 毫秒，防止 CPU 过高
                        continue;
                    }

                    for (StreamOutputStrategy strategy : strategies) {
                        try {
                            strategy.handlePacket(pkt);
                        } catch (Exception ex) {
                            log.warn("推流处理异常：{}", ex.getMessage());
                        }
                    }
                    count++;

                    completableFutureOne.complete("true");//运行到这说明推流成功了
                    avcodec.av_packet_unref(pkt);
                }
            } catch (Exception e) {
                log.error("推流主循环异常：{}", e.getMessage(), e);
            } finally {
                close();
            }
        });
        thread.setName("javacv-video-push-thread");
        thread.start();
    }

    public void close() {
        log.info("准备关闭 StreamHandler...");
        running = false;

        // 1️⃣ 通知 grabber 停止读取
        stopProcessing();

        // 2️⃣ 等待推流线程完全结束
        if (thread != null && thread.isAlive()) {
            try {
                thread.join(500); // 最多等0.5秒
            } catch (InterruptedException ignored) {
            }
        }

        // 3️⃣ 再安全释放 grabber
        try {
            for (StreamOutputStrategy strategy : strategies) {
                strategy.close();
            }
            if (grabber != null) {
                try {
                    grabber.stop();
                } catch (Exception ignored) {
                }
                try {
                    grabber.release();
                } catch (Exception ignored) {
                }
            }
            log.info("FFmpeg grabber 已安全释放");
        } catch (Exception e) {
            log.error("关闭 HandleStreamV2 出错：{}", e.getMessage());
        }

        // 4️⃣ 最后再关IO流
        try {
            outputStream.close();
        } catch (Exception ignored) {
        }
        try {
            inputStream.close();
        } catch (Exception ignored) {
        }

        log.info("StreamHandler 已完全关闭。");
    }

    public void processStream(byte[] data) {
        try {
//            log.info("=======Sending stream data of size: {}", data.length);
            outputStream.write(data, 0, data.length);
        } catch (Exception e) {
            log.error("Send stream error ,This service has been discontinued.{}", e.getMessage());
        }
    }

    public void stopProcessing() {
        running = false;
        if (thread != null) {
            thread.interrupt();
        }
        log.info("已关闭javacv视频处理线程");
    }
}