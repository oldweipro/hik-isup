package com.oldwei.isup.sdk.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.oldwei.isup.handler.StreamHandler;
import com.oldwei.isup.mapper.DeviceMapper;
import com.oldwei.isup.model.Device;
import com.oldwei.isup.sdk.StreamManager;
import com.oldwei.isup.sdk.service.PREVIEW_DATA_CB;
import com.oldwei.isup.sdk.service.constant.EHOME_REGISTER_TYPE;
import com.oldwei.isup.sdk.structure.NET_EHOME_PREVIEW_CB_MSG;
import com.sun.jna.Pointer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PreviewStreamHandler implements PREVIEW_DATA_CB {
    private final DeviceMapper deviceMapper;

    @Override
    public void invoke(int iPreviewHandle, NET_EHOME_PREVIEW_CB_MSG pPreviewCBMsg, Pointer pUserData) {
        Optional<Device> oneOpt = new LambdaQueryChainWrapper<>(deviceMapper).eq(Device::getPreviewHandle, iPreviewHandle).oneOpt();
        if (oneOpt.isEmpty()) {
            log.warn("通过预览句柄:{} 未找到对应的设备信息", iPreviewHandle);
            return;
        }
        Device one = oneOpt.get();
        switch (pPreviewCBMsg.byDataType) {
            case EHOME_REGISTER_TYPE.NET_DVR_SYSHEAD: {
                //系统头
                log.info("预览数据回调, iPreviewHandle: {}, dwDataLen: {}, 系统头", iPreviewHandle, pPreviewCBMsg.dwDataLen);
                break;
            }
            case EHOME_REGISTER_TYPE.NET_DVR_STREAMDATA: {
                //码流数据
                break;
            }
        }
        byte[] dataStream = pPreviewCBMsg.pRecvdata.getByteArray(0, pPreviewCBMsg.dwDataLen);
        if (dataStream != null) {
            Integer sessionID = one.getPreviewSessionId();
            StreamHandler streamHandler = StreamManager.concurrentMap.get(sessionID);
            streamHandler.processStream(dataStream);
        }
    }
}
