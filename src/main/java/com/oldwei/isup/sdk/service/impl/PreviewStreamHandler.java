package com.oldwei.isup.sdk.service.impl;

import com.oldwei.isup.handler.StreamHandler;
import com.oldwei.isup.sdk.StreamManager;
import com.oldwei.isup.sdk.service.PREVIEW_DATA_CB;
import com.oldwei.isup.sdk.structure.NET_EHOME_PREVIEW_CB_MSG;
import com.sun.jna.Pointer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PreviewStreamHandler implements PREVIEW_DATA_CB {

    @Override
    public void invoke(int iPreviewHandle, NET_EHOME_PREVIEW_CB_MSG pPreviewCBMsg, Pointer pUserData) {
        byte[] dataStream = pPreviewCBMsg.pRecvdata.getByteArray(0, pPreviewCBMsg.dwDataLen);
        if (dataStream != null && dataStream.length > 0) {
            Integer sessionID = StreamManager.previewHandSAndSessionIDandMap.get(iPreviewHandle);
            StreamHandler streamHandler = StreamManager.concurrentMap.get(sessionID);
            if (streamHandler != null) {
                // 如果报错，应该关闭预览
                streamHandler.processStream(dataStream);
            }
        }
    }
}
