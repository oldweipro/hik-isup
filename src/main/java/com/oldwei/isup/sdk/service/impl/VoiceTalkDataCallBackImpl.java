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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service("voiceTalkDataCallBack")
public class VoiceTalkDataCallBackImpl implements VOICETALK_DATA_CB {
    private File fileG7 = null;

    private FileOutputStream outputStreamG7 = null;

    public VoiceTalkDataCallBackImpl() {
        // 保存回调函数的音频数据
        if (OsSelect.isWindows()) {
            String resFileAbsPath = CommonMethod.getResFileAbsPath("container\\resources\\audioFile\\DeviceToPlat.g7");
            // 如果resFileAbsPath不存在则创建
            Path filePath = Paths.get(resFileAbsPath);

            // 检查文件是否存在
            if (!Files.exists(filePath)) {
                try {
                    // 创建父目录（如果不存在）
                    Path parentDir = filePath.getParent();
                    if (parentDir != null && !Files.exists(parentDir)) {
                        Files.createDirectories(parentDir);
                    }
                    // 创建空文件
                    Files.createFile(filePath);
                    System.out.println("文件已创建: " + filePath);
                } catch (IOException e) {
                    System.err.println("创建文件时发生错误: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("文件已存在: " + filePath);
            }
            fileG7 = new File(resFileAbsPath);
        }
        if (OsSelect.isLinux()) {
            String resFileAbsPath = CommonMethod.getResFileAbsPath("container/resources/audioFile/DeviceToPlat.g7");
            // 如果resFileAbsPath不存在则创建
            Path filePath = Paths.get(resFileAbsPath);

            // 检查文件是否存在
            if (!Files.exists(filePath)) {
                try {
                    // 创建父目录（如果不存在）
                    Path parentDir = filePath.getParent();
                    if (parentDir != null && !Files.exists(parentDir)) {
                        Files.createDirectories(parentDir);
                    }
                    // 创建空文件
                    Files.createFile(filePath);
                    System.out.println("文件已创建: " + filePath);
                } catch (IOException e) {
                    System.err.println("创建文件时发生错误: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("文件已存在: " + filePath);
            }
            fileG7 = new File(resFileAbsPath);
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
