package com.oldwei.isup.handler.strategy;

import com.oldwei.isup.handler.FlvCache;
import com.oldwei.isup.handler.StreamOutputStrategy;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;

import java.io.ByteArrayOutputStream;
import java.util.function.Consumer;

/**
 * WebSocket输出
 */
@Slf4j
public class WebSocketOutputStrategy implements StreamOutputStrategy {
    private final Consumer<byte[]> frameConsumer;
    private FFmpegFrameRecorder recorder;
    private final ByteArrayOutputStream outputStreamPush = new ByteArrayOutputStream(4096 * 5);
    private final String playKey;

    public WebSocketOutputStrategy(String playKey, Consumer<byte[]> frameConsumer) {
        this.frameConsumer = frameConsumer;
        this.playKey = playKey;
    }

    @Override
    public void init(FFmpegFrameGrabber grabber, AVFormatContext ifmtCtx) throws Exception {
        log.info("初始化 WebSocket 推流器");

        recorder = new FFmpegFrameRecorder(outputStreamPush, grabber.getImageWidth(),
                grabber.getImageHeight(), grabber.getAudioChannels());

        recorder.setFormat("flv");

        // ⚙️ 关键配置：零延迟 + 不重新编码 + 立刻刷新输出
        recorder.setVideoOption("tune", "zerolatency");
        recorder.setVideoOption("preset", "ultrafast");
        recorder.setVideoCodec(grabber.getVideoCodec());
        recorder.setAudioCodec(grabber.getAudioCodec());
        recorder.setOption("flush_packets", "1");
        recorder.setOption("fflags", "nobuffer");
        recorder.setOption("avioflags", "direct");
        recorder.setOption("max_delay", "0");
        recorder.setOption("rtbufsize", "0");

        recorder.start(ifmtCtx);
        log.info("WebSocket 推流器启动完成");
    }

    @Override
    public void handlePacket(AVPacket packet) throws Exception {
        recorder.recordPacket(packet);

        byte[] flvData = outputStreamPush.toByteArray();
        if (flvData.length > 0 && frameConsumer != null) {
            outputStreamPush.reset();

            if (FlvCache.getFlvHeader(playKey) == null) {
                FlvCache.cacheFlvHeader(playKey, flvData);
            }

            // 🔍 精确判断关键帧
            if (isKeyFrame(packet)) {
                FlvCache.cacheKeyFrame(playKey, flvData);
            }

            frameConsumer.accept(flvData);
        }

        // ✅ 强制立即 flush 输出
        recorder.flush();
    }

    @Override
    public void close() {
        try {
            if (recorder != null) {
                recorder.stop();
                recorder.release();
            }
            outputStreamPush.close();
        } catch (Exception e) {
            log.error("关闭 WebSocket 推流器出错: {}", e.getMessage());
        }
    }

    private boolean isKeyFrame(AVPacket pkt) {
        return (pkt.flags() & avcodec.AV_PKT_FLAG_KEY) != 0;
    }
}
