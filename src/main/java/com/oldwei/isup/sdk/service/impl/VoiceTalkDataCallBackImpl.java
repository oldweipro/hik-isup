package com.oldwei.isup.sdk.service.impl;

import com.oldwei.isup.sdk.service.VOICETALK_DATA_CB;
import com.oldwei.isup.sdk.structure.NET_EHOME_VOICETALK_DATA_CB_INFO;
import com.oldwei.isup.util.CommonMethod;
import com.oldwei.isup.util.OsSelect;
import com.sun.jna.Pointer;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

@Service("voiceTalkDataCallBack")
public class VoiceTalkDataCallBackImpl implements VOICETALK_DATA_CB {
    private File fileG7 = null;

    private FileOutputStream outputStreamG7 = null;

    public VoiceTalkDataCallBackImpl() {
        // 保存回调函数的音频数据
        if (OsSelect.isWindows()) {
            fileG7 = new File(CommonMethod.getResFileAbsPath("resources\\audioFile\\DeviceToPlat.g7"));
        }
        if (OsSelect.isLinux()) {
            fileG7 = new File(CommonMethod.getResFileAbsPath("/resources/audioFile/DeviceToPlat.g7"));
        }
        try {
            if (!fileG7.exists()) {
                fileG7.createNewFile();
            }
            outputStreamG7 = new FileOutputStream(fileG7);
        } catch (IOException e) {
            // TODO 自行处理异常
            e.printStackTrace();
        }
    }

    public boolean invoke(int lHandle, NET_EHOME_VOICETALK_DATA_CB_INFO pNewLinkCBInfo, Pointer pUserData) {
        //回调函数保存设备返回的语音数据
        //将设备发送过来的语音数据写入文件
//        System.out.println("设备音频发送.....");
        int VoiceHandle = -1;
        VoiceHandle = lHandle;
        long offset = 0;
        ByteBuffer buffers = pNewLinkCBInfo.pData.getByteBuffer(offset, pNewLinkCBInfo.dwDataLen);
        byte[] bytes = new byte[pNewLinkCBInfo.dwDataLen];
        buffers.rewind();
        buffers.get(bytes);
        try {
            outputStreamG7.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

}
