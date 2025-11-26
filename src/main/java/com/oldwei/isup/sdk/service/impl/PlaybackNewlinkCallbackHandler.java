package com.oldwei.isup.sdk.service.impl;

import com.oldwei.isup.sdk.StreamManager;
import com.oldwei.isup.sdk.service.IHikISUPStream;
import com.oldwei.isup.sdk.service.PLAYBACK_NEWLINK_CB;
import com.oldwei.isup.sdk.structure.NET_EHOME_PLAYBACK_DATA_CB_PARAM;
import com.oldwei.isup.sdk.structure.NET_EHOME_PLAYBACK_NEWLINK_CB_INFO;
import com.sun.jna.Pointer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service("playbackNewlinkCallbackHandler")
@RequiredArgsConstructor
public class PlaybackNewlinkCallbackHandler implements PLAYBACK_NEWLINK_CB {

    private final IHikISUPStream hikISUPStream;

    // 存储每个预览句柄对应的回调处理器实例
    private final Map<Integer, PlaybackDataCallback> handlerMap = new ConcurrentHashMap<>();

    public boolean invoke(int lPlayBackLinkHandle, NET_EHOME_PLAYBACK_NEWLINK_CB_INFO pNewLinkCBInfo, Pointer pUserData) {
        pNewLinkCBInfo.read();
        log.info("PLAYBACK_NEWLINK_CB callback, szDeviceID: {},lSessionID: {},dwChannelNo: {}", new String(pNewLinkCBInfo.szDeviceID).trim(), pNewLinkCBInfo.lSessionID, pNewLinkCBInfo.dwChannelNo);
        StreamManager.playbackPreviewHandSAndSessionIDandMap.put(lPlayBackLinkHandle, pNewLinkCBInfo.lSessionID);
        StreamManager.playbackSessionIDAndPreviewHandleMap.put(pNewLinkCBInfo.lSessionID, lPlayBackLinkHandle);

        // 为每个预览会话创建独立的回调处理器实例
        PlaybackDataCallback playbackDataCallback = handlerMap.computeIfAbsent(lPlayBackLinkHandle, handle -> {
            log.info("创建新的PreviewStreamHandler实例，句柄: {}", handle);
            return new PlaybackDataCallback();
        });

        NET_EHOME_PLAYBACK_DATA_CB_PARAM struCBParam = new NET_EHOME_PLAYBACK_DATA_CB_PARAM();
        //预览数据回调参数
        struCBParam.fnPlayBackDataCB = playbackDataCallback;
        struCBParam.byStreamFormat = 0;
        struCBParam.write();
        if (!hikISUPStream.NET_ESTREAM_SetPlayBackDataCB(lPlayBackLinkHandle, struCBParam)) {
            System.out.println("NET_ESTREAM_SetPlayBackDataCB failed");
        }
        return true;
    }
}
