package com.oldwei.isup.handler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FlvCache {
    private static final Map<String, byte[]> flvHeaders = new ConcurrentHashMap<>();
    private static final Map<String, byte[]> keyFrames = new ConcurrentHashMap<>();

    public static void cacheFlvHeader(String playKey, byte[] header) {
        flvHeaders.put(playKey, header);
    }

    public static void cacheKeyFrame(String playKey, byte[] keyFrame) {
        keyFrames.put(playKey, keyFrame);
    }

    public static byte[] getFlvHeader(String playKey) {
        return flvHeaders.get(playKey);
    }

    public static byte[] getKeyFrame(String playKey) {
        return keyFrames.get(playKey);
    }
}


