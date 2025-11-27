package com.oldwei.isup.sdk;

import com.aizuda.zlm4j.structure.MK_RTP_SERVER;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StreamManager {
    public static Map<String, MK_RTP_SERVER> deviceRTP = new ConcurrentHashMap<>();
    public static Map<Integer, Integer> userIDandSessionMap = new HashMap<>();
    public static Map<Integer, Integer> previewHandSAndSessionIDandMap = new HashMap<>();
    public static Map<Integer, Integer> sessionIDAndPreviewHandleMap = new HashMap<>();
    // 存储 sessionID 对应的 RTP 端口，用于回调函数获取对应的端口
    public static Map<Integer, Integer> sessionIDAndRtpPortMap = new HashMap<>();


    public static Map<String, MK_RTP_SERVER> playbackDeviceRTP = new ConcurrentHashMap<>();
    public static Map<Integer, Integer> playbackUserIDandSessionMap = new HashMap<>();
    public static Map<Integer, Integer> playbackPreviewHandSAndSessionIDandMap = new HashMap<>();
    public static Map<Integer, Integer> playbackSessionIDAndPreviewHandleMap = new HashMap<>();
    // 存储 sessionID 对应的 RTP 端口，用于回调函数获取对应的端口
    public static Map<Integer, Integer> playbackSessionIDAndRtpPortMap = new HashMap<>();

    // TODO 多设备支持，暂时只支持一个设备的语音对讲
    public static int lVoiceLinkHandle = -1;
}
