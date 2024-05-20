package com.oldwei.hikisup.sdk.service.impl;

import com.oldwei.hikisup.sdk.service.IHikISUPStream;
import com.oldwei.hikisup.sdk.service.PREVIEW_NEWLINK_CB;
import com.oldwei.hikisup.sdk.structure.NET_EHOME_NEWLINK_CB_MSG;
import com.oldwei.hikisup.sdk.structure.NET_EHOME_PREVIEW_DATA_CB_PARAM;
import com.sun.jna.Pointer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 实时预览数据回调（预览数据存储到文件）
 */
@Component
@RequiredArgsConstructor
public class FPREVIEW_NEWLINK_CB_FILE implements PREVIEW_NEWLINK_CB {

    private final IHikISUPStream hikISUPStream;
    private final FPREVIEW_DATA_CB_FILE fpreviewDataCbFile;

    public boolean invoke(int lLinkHandle, NET_EHOME_NEWLINK_CB_MSG pNewLinkCBMsg, Pointer pUserData) {
        //预览数据回调参数
        System.out.println("[lPreviewHandle 默认值 -1]预览数据回调参数:" + lLinkHandle);
        NET_EHOME_PREVIEW_DATA_CB_PARAM struDataCB = new NET_EHOME_PREVIEW_DATA_CB_PARAM();
        struDataCB.fnPreviewDataCB = fpreviewDataCbFile;

        if (!this.hikISUPStream.NET_ESTREAM_SetPreviewDataCB(lLinkHandle, struDataCB)) {
            System.out.println("NET_ESTREAM_SetPreviewDataCB failed err:：" + this.hikISUPStream.NET_ESTREAM_GetLastError());
            return false;
        }
        return true;
    }
}
