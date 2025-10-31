package com.oldwei.isup.handler;

import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.javacv.FFmpegFrameGrabber;

public interface StreamOutputStrategy {
    void init(FFmpegFrameGrabber grabber, AVFormatContext ifmtCtx) throws Exception;

    void handlePacket(AVPacket packet) throws Exception;

    void close();
}
