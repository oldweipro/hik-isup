package com.oldwei.isup.sdk.service.impl;

import com.oldwei.isup.sdk.StreamManager;
import com.oldwei.isup.sdk.service.PLAYBACK_DATA_CB;
import com.oldwei.isup.sdk.structure.NET_EHOME_PLAYBACK_DATA_CB_INFO;
import com.sun.jna.Pointer;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

@Service("playbackDataCallback")
public class PlaybackDataCallback implements PLAYBACK_DATA_CB {

    int iCount = 0;
    private File playbackFile = new File(System.getProperty("user.dir") + "\\outputFiles\\playbackVideo.mp4");
    private FileOutputStream playbackFileOutput;

    //实时流回调函数
    public boolean invoke(int iPlayBackLinkHandle, NET_EHOME_PLAYBACK_DATA_CB_INFO pDataCBInfo, Pointer pUserData) {
        if (iCount == 500) {//降低打印频率
            System.out.println("PLAYBACK_DATA_CB callback , dwDataLen:" + pDataCBInfo.dwDataLen + ",dwType:" + pDataCBInfo.dwType);
            iCount = 0;
        }
        iCount++;
        //播放库SDK解码显示在win窗口上，
//        switch (pDataCBInfo.dwType) {
//            case 1: //系统头
//                // 初始化回放标记
//                stopPlayBackFlag = false;
//
//                boolean b_port = playCtrl.PlayM4_GetPort(m_lPort);
//                if (!b_port) //获取播放库未使用的通道号
//                {
//                    break;
//                }
//                if (pDataCBInfo.dwDataLen > 0) {
//                    if (!playCtrl.PlayM4_SetOverlayMode(m_lPort.getValue(), false, 0)) {
//                        break;
//                    }
//
//                    if (!playCtrl.PlayM4_SetStreamOpenMode(m_lPort.getValue(), PlayCtrl.STREAME_FILE))  //设置文件流播放模式
//                    {
//                        break;
//                    }
//
//                    if (!playCtrl.PlayM4_OpenStream(m_lPort.getValue(), pDataCBInfo.pData, pDataCBInfo.dwDataLen, 2 * 1024 * 1024)) //打开流接口
//                    {
//                        break;
//                    }
//                    W32API.HWND hwnd = new W32API.HWND(Native.getComponentPointer(PlaybackVideoUI.panelPlay));
//                    if (!playCtrl.PlayM4_Play(m_lPort.getValue(), hwnd)) //播放开始
//                    {
//                        break;
//                    }
//                }
//            case 2:   //码流数据
//                if ((pDataCBInfo.dwDataLen > 0) && (m_lPort.getValue() != -1)) {
//                    // 推流过快可能导致推流失败，这里处理重试逻辑
//                    int tryInputTime = 5;
//                    for (int i = 0; i < tryInputTime; i++) {
//                        boolean bRet = playCtrl.PlayM4_InputData(m_lPort.getValue(), pDataCBInfo.pData, pDataCBInfo.dwDataLen);
//                        if (!bRet) {
//                            if (i >= tryInputTime - 1) {
//                                System.out.println("PlayM4_InputData,failed err:" + playCtrl.PlayM4_GetLastError(m_lPort.getValue()));
//                            }
//                            try {
//                                Thread.sleep(200);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        } else {
//                            break;
//                        }
//                    }
//                }
//                break;
//            case 3:  // 视频流结束标记
//                System.err.println("收到回放结束信令！");
//                stopPlayBackFlag = true;
//                break;
//        }

        // 回放推流开始信令
        if (pDataCBInfo.dwType == 1) {
            // 初始化回放标记
            StreamManager.stopPlayBackFlag = false;
        }

        // 回放推流结束信令
        if (pDataCBInfo.dwType == 3) {
            System.err.println("收到回放结束信令！");
            StreamManager.stopPlayBackFlag = true;
        }
        //仅保存回放的视频流文件，不需要保存的话，可注释掉这部分代码
        try {
            playbackFileOutput = new FileOutputStream(playbackFile, true);
            long offset = 0;
            ByteBuffer buffers = pDataCBInfo.pData.getByteBuffer(offset, pDataCBInfo.dwDataLen);
            byte[] bytes = new byte[pDataCBInfo.dwDataLen];
            buffers.rewind();
            buffers.get(bytes);
            playbackFileOutput.write(bytes);
            playbackFileOutput.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }
}
