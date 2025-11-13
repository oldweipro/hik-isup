package com.oldwei.isup.sdk.service.impl;

import com.oldwei.isup.handler.StreamHandler;
import com.oldwei.isup.sdk.StreamManager;
import com.oldwei.isup.sdk.service.PREVIEW_DATA_CB;
import com.oldwei.isup.sdk.service.constant.EHOME_REGISTER_TYPE;
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
        switch (pPreviewCBMsg.byDataType) {
            case EHOME_REGISTER_TYPE.NET_DVR_SYSHEAD: {
                //系统头
                log.info("预览数据回调, iPreviewHandle: {}, dwDataLen: {}, 系统头", iPreviewHandle, pPreviewCBMsg.dwDataLen);
                break;
            }
            case EHOME_REGISTER_TYPE.NET_DVR_STREAMDATA: {
                //码流数据
                byte[] dataStream = pPreviewCBMsg.pRecvdata.getByteArray(0, pPreviewCBMsg.dwDataLen);
                if (dataStream != null && dataStream.length > 0) {
                    Integer sessionID = StreamManager.previewHandSAndSessionIDandMap.get(iPreviewHandle);
                    StreamHandler streamHandler = StreamManager.concurrentMap.get(sessionID);
                    if (streamHandler != null) {
//                        log.info("预览数据回调, iPreviewHandle: {}, dwDataLen: {}, 码流数据, sessionID: {}, streamHandler: {}", iPreviewHandle, dataStream.length, sessionID, streamHandler.hashCode());
                        // 如果报错，应该关闭预览
                        streamHandler.processStream(dataStream);
                    }
                }
                break;
            }
        }
    }
}
