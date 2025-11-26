package com.oldwei.isup.sdk;

import com.aizuda.zlm4j.structure.MK_RTP_SERVER;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StreamManager {
    public static Map<String, MK_RTP_SERVER> deviceRTP = new ConcurrentHashMap<>();
    public static Map<String, MK_RTP_SERVER> deviceRTPPlayback = new ConcurrentHashMap<>();
    public static Map<Integer, Integer> userIDandSessionMap = new HashMap<>();

    public static Map<Integer, Integer> previewHandSAndSessionIDandMap = new HashMap<>();
    public static Map<Integer, Integer> sessionIDAndPreviewHandleMap = new HashMap<>();
    public static Map<Integer, Boolean> loginchannelIdAndstopflag = new HashMap<>();
    // 存储 sessionID 对应的 RTP 端口，用于回调函数获取对应的端口
    public static Map<Integer, Integer> sessionIDAndRtpPortMap = new HashMap<>();
    //    public static Map<Integer, Boolean> loginIDAndPreviewStatusMap = new HashMap<>();


    public static Map<Integer, Integer> playbackUserIDandSessionMap = new HashMap<>();
    public static Map<Integer, Integer> playbackPreviewHandSAndSessionIDandMap = new HashMap<>();
    public static Map<Integer, Integer> playbackSessionIDAndPreviewHandleMap = new HashMap<>();
    public static Map<Integer, Boolean> playbackLoginchannelIdAndstopflag = new HashMap<>();
    // 存储 sessionID 对应的 RTP 端口，用于回调函数获取对应的端口
    public static Map<Integer, Integer> playbackSessionIDAndRtpPortMap = new HashMap<>();

    public static int lVoiceLinkHandle = -1;
}
