package com.oldwei.isup.sdk.service.impl;

import com.oldwei.isup.sdk.service.IHikNet;
import com.oldwei.isup.sdk.service.VOICETALK_DATA_CB;
import com.oldwei.isup.sdk.structure.BYTE_ARRAY;
import com.oldwei.isup.sdk.structure.NET_DVR_AUDIODEC_PROCESS_PARAM;
import com.oldwei.isup.sdk.structure.NET_EHOME_VOICETALK_DATA_CB_INFO;
import com.oldwei.isup.util.CommonMethod;
import com.sun.jna.Pointer;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

@Service("voiceTalkDataCallBack")
public class VoiceTalkDataCallBackImpl implements VOICETALK_DATA_CB {
    private File fileG7 = null;
    private File filePcm = null;

    private FileOutputStream outputStreamG7 = null;
    private FileOutputStream outputStreamPcm = null;
    private final IHikNet hikNet;

    public VoiceTalkDataCallBackImpl(IHikNet hikNet) {
        this.hikNet = hikNet;
        // 保存回调函数的音频数据
        fileG7 = new File(CommonMethod.getResFileAbsPath("audioFile/DeviceToPlat.g7"));
        try {
            if (!fileG7.exists()) {
                fileG7.createNewFile();
            }
            outputStreamG7 = new FileOutputStream(fileG7);
        } catch (IOException e) {
            // TODO 自行处理异常
            e.printStackTrace();
        }

        // 保存回调函数的音频数据（解码后的pcm数据，播放和确认时长）
        filePcm = new File(CommonMethod.getResFileAbsPath("audioFile/DeviceToPlat.pcm"));
        try {
            if (!filePcm.exists()) {
                filePcm.createNewFile();
            }
            outputStreamPcm = new FileOutputStream(filePcm);
        } catch (IOException e) {
            // TODO 自行处理异常
            e.printStackTrace();
        }
    }

    public boolean invoke(int lHandle, NET_EHOME_VOICETALK_DATA_CB_INFO pNewLinkCBInfo, Pointer pUserData) {
        //回调函数保存设备返回的语音数据
        //将设备发送过来的语音数据写入文件
        System.out.println("设备音频发送.....");
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

        //解码为pcm（播放和确认时长）
        Pointer pDecHandle = hikNet.NET_DVR_InitG711Decoder();
        // 回调中一次可能返回多帧数据，需要逐帧解码为pcm
        for (int i = 0; i < bytes.length; i += 160) {
            BYTE_ARRAY byteArray = new BYTE_ARRAY(160);
            // 检查是否到达源数组末尾，避免数组越界
            int actualReadSize = Math.min(160, bytes.length - i);
            System.arraycopy(bytes, i, byteArray.byValue, 0, actualReadSize);
            byteArray.write();
            NET_DVR_AUDIODEC_PROCESS_PARAM struAudioParam = new NET_DVR_AUDIODEC_PROCESS_PARAM();
            struAudioParam.in_buf = byteArray.getPointer();
            struAudioParam.in_data_size = byteArray.size();

            BYTE_ARRAY ptrVoiceData = new BYTE_ARRAY(320);
            ptrVoiceData.write();
            struAudioParam.out_buf = ptrVoiceData.getPointer();
            struAudioParam.out_frame_size = 320;
            struAudioParam.g711_type = 0; //G711编码类型：0- U law，1- A law
            struAudioParam.write();
            if (!hikNet.NET_DVR_DecodeG711Frame(pDecHandle, struAudioParam)) {
                System.out.println("NET_DVR_DecodeG711Frame failed, error code:" + hikNet.NET_DVR_GetLastError());
            }
            struAudioParam.read();
            //将解码之后PCM音频数据写入文件
            long offsetPcm = 0;
            ByteBuffer buffersPcm = struAudioParam.out_buf.getByteBuffer(offsetPcm, struAudioParam.out_frame_size);
            byte[] bytesPcm = new byte[struAudioParam.out_frame_size];
            buffersPcm.rewind();
            buffersPcm.get(bytesPcm);
            try {
                outputStreamPcm.write(bytesPcm);  //这里实现的是将设备发送的pcm音频数据写入文件，（前面的代码实现的就是将g711解码成pcm音频）
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

}
