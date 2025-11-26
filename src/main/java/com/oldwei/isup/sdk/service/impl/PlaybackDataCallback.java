package com.oldwei.isup.sdk.service.impl;

import com.oldwei.isup.sdk.StreamManager;
import com.oldwei.isup.sdk.service.PLAYBACK_DATA_CB;
import com.oldwei.isup.sdk.structure.NET_EHOME_PLAYBACK_DATA_CB_INFO;
import com.oldwei.isup.util.StreamHandler;
import com.sun.jna.Pointer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Slf4j
@Service("playbackDataCallback")
public class PlaybackDataCallback implements PLAYBACK_DATA_CB {

    int iCount = 0;

    //实时流回调函数
    public boolean invoke(int iPlayBackLinkHandle, NET_EHOME_PLAYBACK_DATA_CB_INFO pDataCBInfo, Pointer pUserData) {
        Integer sessionID = StreamManager.playbackPreviewHandSAndSessionIDandMap.get(iPlayBackLinkHandle);
        //播放库SDK解码显示在win窗口上，
        switch (pDataCBInfo.dwType) {
            case 1:
                // 初始化回放标记
                StreamManager.playbackUserIDandSessionMap.get(sessionID);
//                StreamManager.playbackSessionIDAndStopPlaybackFlagMap.put(sessionID, false);
                break;
            case 2:
                iCount++;
                //码流数据
                long offset = 0;
                ByteBuffer buffers = pDataCBInfo.pData.getByteBuffer(offset, pDataCBInfo.dwDataLen);
                byte[] dataStream = new byte[pDataCBInfo.dwDataLen];
                buffers.rewind();
                buffers.get(dataStream);

                if (pDataCBInfo.dwDataLen > 0) {
                    StreamHandler streamHandler = StreamManager.playbackConcurrentMap.get(sessionID);
                    if (streamHandler != null) {
                        streamHandler.processStream(dataStream);
                    } else {
                        log.warn("streamHandler未初始化");
                    }
                } else {
                    log.info("回放数据回调, dwDataLen: {}, 码流数据为空, sessionID: {}", pDataCBInfo.dwDataLen, sessionID);
                }
                break;
            case 3:  // 视频流结束标记
                System.err.println("收到回放结束信令！");
//                StreamManager.playbackSessionIDAndStopPlaybackFlagMap.put(sessionID, true);
                break;
        }
        return true;
    }
}
