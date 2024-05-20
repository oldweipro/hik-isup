package com.oldwei.hikisup.sdk.service.impl;

import com.oldwei.hikisup.sdk.SdkService.StreamService.HCNetSDK;
import com.oldwei.hikisup.sdk.service.PREVIEW_DATA_CB;
import com.oldwei.hikisup.sdk.structure.NET_EHOME_PREVIEW_CB_MSG;
import com.oldwei.hikisup.util.JavaCVProcessThread;
import com.sun.jna.Pointer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class FPREVIEW_DATA_CB_FILE implements PREVIEW_DATA_CB {
    private final Map<Integer, JavaCVProcessThread> threads = new HashMap<>();
    @Value("${hik.pushStreamUrl}")
    private String pushStreamUrl;
    JavaCVProcessThread t;

    //实时流回调函数
    public void invoke(int iPreviewHandle, NET_EHOME_PREVIEW_CB_MSG pPreviewCBMsg, Pointer pUserData) {
        try {
            if (Objects.isNull(t) || t.getShouldBeNull()) {
                t = new JavaCVProcessThread(pushStreamUrl + "cz");
                t.start();
            }
        } catch (IOException e) {
            log.info("SDK流媒体数据操作失败");
        }
        if (pPreviewCBMsg.byDataType == HCNetSDK.NET_DVR_STREAMDATA) {
            t.push(pPreviewCBMsg.pRecvdata.getByteArray(0, pPreviewCBMsg.dwDataLen), pPreviewCBMsg.dwDataLen);
        }
    }

//    //实时流回调函数/
//    public void invoke(int iPreviewHandle, NET_EHOME_PREVIEW_CB_MSG pPreviewCBMsg, Pointer pUserData) {
//        try {
//            if (Objects.nonNull(threads.get(iPreviewHandle))) {
//                JavaCVProcessThread t = threads.get(iPreviewHandle);
//                if (t.getShouldBeNull()) {
//                    threads.put(iPreviewHandle, null);
//                } else {
//                    t.push(pPreviewCBMsg.pRecvdata.getByteArray(0, pPreviewCBMsg.dwDataLen), pPreviewCBMsg.dwDataLen);
//                }
//            } else {
//                JavaCVProcessThread t = new JavaCVProcessThread(pushStreamUrl + "cz");
//                log.info("初始化推流对象");
//                threads.put(iPreviewHandle, t);
//                t.start();
//                t.push(pPreviewCBMsg.pRecvdata.getByteArray(0, pPreviewCBMsg.dwDataLen), pPreviewCBMsg.dwDataLen);
//            }
//        } catch (IOException e) {
//            log.info("SDK流媒体数据操作失败");
//        }
//    }
}
