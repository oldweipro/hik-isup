package com.oldwei.hikisup.sdk.service.impl;

import com.oldwei.hikisup.sdk.service.IPushStreamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

@Slf4j
@Service
public class PushStreamServiceImpl implements IPushStreamService {

    private PipedInputStream pin;
    private PipedOutputStream pout;
    private FFmpegFrameGrabber grabber;
    private FFmpegFrameRecorder recorder;
    private String pushStreamUrl;

    @Async
    @Override
    public void pushMediaStream(String pushStreamUrl, byte[] data, int size) {
        this.pushStreamUrl = pushStreamUrl;
        try {
            initializePipes();
            writeDataToPipe(data, size);
            startStreaming();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeResources();
        }
    }
    private void initializePipes() throws IOException {
        pout = new PipedOutputStream();
        pin = new PipedInputStream(pout);
    }

    private void writeDataToPipe(byte[] data, int size) throws IOException {
        pout.write(data, 0, size);
    }

    private void startStreaming() throws IOException {
        grabber = new FFmpegFrameGrabber(pin, 0);
        grabber.start();
        recorder = new FFmpegFrameRecorder(pushStreamUrl, 1920, 1080);
        recorder.setVideoCodec(grabber.getVideoCodec());
        recorder.setFormat("flv");
        if (grabber.getAudioChannels() != 0) {
            recorder.setAudioCodec(grabber.getAudioCodec());
        }
        recorder.setAudioChannels(grabber.getAudioChannels());
        recorder.start();
        Frame frame;
        while ((frame = grabber.grabFrame()) != null) {
            recorder.record(frame);
        }
    }

    private void closeResources() {
        try {
            if (grabber != null) grabber.close();
            if (recorder != null) recorder.close();
            if (pout != null) pout.close();
            if (pin != null) pin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
