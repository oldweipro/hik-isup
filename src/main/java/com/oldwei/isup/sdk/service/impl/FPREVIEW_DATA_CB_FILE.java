package com.oldwei.isup.sdk.service.impl;

import com.oldwei.isup.sdk.service.PREVIEW_DATA_CB;
import com.oldwei.isup.sdk.structure.NET_EHOME_PREVIEW_CB_MSG;
import com.sun.jna.Pointer;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 预览数据回调函数（预览数据）
 */
@Slf4j
public class FPREVIEW_DATA_CB_FILE implements PREVIEW_DATA_CB {
    private final File previewFile = new File(System.getProperty("user.dir") + "\\output\\previewVideo.mp4");

    // 实时流回调函数
    public void invoke(int iPreviewHandle, NET_EHOME_PREVIEW_CB_MSG pPreviewCBMsg, Pointer pUserData) {
        //仅需取码流数据，获取码流数据保存成文件
        try {
//            log.info("预览数据回调, iPreviewHandle: {}, dwDataLen: {}", iPreviewHandle, pPreviewCBMsg.dwDataLen);
            FileOutputStream previewFileOutput = new FileOutputStream(previewFile, true);
            long offset = 0;
            ByteBuffer buffers = pPreviewCBMsg.pRecvdata.getByteBuffer(offset, pPreviewCBMsg.dwDataLen);
            byte[] bytes = new byte[pPreviewCBMsg.dwDataLen];
            buffers.rewind();
            buffers.get(bytes);
            previewFileOutput.write(bytes);
            previewFileOutput.close();
        } catch (IOException e) {
            // TODO 这里需要自行处理文件读取异常逻辑
            e.printStackTrace();
        }
    }
}
