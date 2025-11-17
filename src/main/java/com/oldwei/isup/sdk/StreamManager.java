package com.oldwei.isup.sdk;

import com.oldwei.isup.handler.StreamHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StreamManager {
    public static Map<Integer, StreamHandler> concurrentMap = new ConcurrentHashMap<>();
    public static Map<Integer, Integer> userIDandSessionMap = new HashMap<>();

    public static Map<Integer, Integer> previewHandSAndSessionIDandMap = new HashMap<>();
    public static Map<Integer, Integer> sessionIDAndPreviewHandleMap = new HashMap<>();
    public static Map<Integer, Boolean> loginchannelIdAndstopflag = new HashMap<>();
    //    public static Map<Integer, Boolean> loginIDAndPreviewStatusMap = new HashMap<>();


    public static Map<Integer, StreamHandler> playbackConcurrentMap = new ConcurrentHashMap<>();
    public static Map<Integer, Integer> playbackUserIDandSessionMap = new HashMap<>();
    public static Map<Integer, Integer> playbackPreviewHandSAndSessionIDandMap = new HashMap<>();
    public static Map<Integer, Integer> playbackSessionIDAndPreviewHandleMap = new HashMap<>();
    public static Map<Integer, Boolean> playbackLoginchannelIdAndstopflag = new HashMap<>();

    public static int lVoiceLinkHandle = -1;
}
