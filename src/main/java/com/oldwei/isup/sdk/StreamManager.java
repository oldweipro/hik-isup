package com.oldwei.isup.sdk;

import com.oldwei.isup.handler.StreamHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StreamManager {
    public static Map<Integer, StreamHandler> concurrentMap = new ConcurrentHashMap<>();
}
