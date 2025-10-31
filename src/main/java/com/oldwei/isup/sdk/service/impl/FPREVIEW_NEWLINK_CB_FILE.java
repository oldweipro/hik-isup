package com.oldwei.isup.sdk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oldwei.isup.model.Device;
import com.oldwei.isup.sdk.service.IHikISUPStream;
import com.oldwei.isup.sdk.service.PREVIEW_NEWLINK_CB;
import com.oldwei.isup.sdk.structure.NET_EHOME_NEWLINK_CB_MSG;
import com.oldwei.isup.sdk.structure.NET_EHOME_PREVIEW_DATA_CB_PARAM;
import com.oldwei.isup.service.IDeviceService;
import com.sun.jna.Pointer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 实时预览数据回调（预览数据存储到文件）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FPREVIEW_NEWLINK_CB_FILE implements PREVIEW_NEWLINK_CB {

    private final IHikISUPStream hikISUPStream;
    private final PreviewStreamHandler fpreviewDataCbFile;
    private final IDeviceService deviceService;

    public boolean invoke(int lPreviewHandle, NET_EHOME_NEWLINK_CB_MSG pNewLinkCBMsg, Pointer pUserData) {
        //预览数据回调参数
        log.info("预览数据回调参数 lPreviewHandle: {}", lPreviewHandle);

        int iSessionID = pNewLinkCBMsg.iSessionID;
        Optional<Device> oneOpt = deviceService.getOneOpt(new LambdaQueryWrapper<Device>().eq(Device::getPreviewSessionId, iSessionID));
        if (oneOpt.isPresent()) {
            Device device = oneOpt.get();
            device.setPreviewHandle(lPreviewHandle);
            deviceService.updateById(device);
            log.info("pNewLinkCBMsg.iSessionID是和预览的时候的sessionID一致的 这个应该是全局唯一 通过sessionID可以确定是哪个摄像头 iSessionID: {}", iSessionID);
            NET_EHOME_PREVIEW_DATA_CB_PARAM struDataCB = new NET_EHOME_PREVIEW_DATA_CB_PARAM();
            struDataCB.fnPreviewDataCB = fpreviewDataCbFile;

            if (!this.hikISUPStream.NET_ESTREAM_SetPreviewDataCB(lPreviewHandle, struDataCB)) {
                log.info("NET_ESTREAM_SetPreviewDataCB failed err: {}", this.hikISUPStream.NET_ESTREAM_GetLastError());
                return false;
            }
            return true;
        }
        return false;
    }
}
