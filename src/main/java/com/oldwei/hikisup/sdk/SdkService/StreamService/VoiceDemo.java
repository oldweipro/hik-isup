package com.oldwei.hikisup.sdk.SdkService.StreamService;

import com.oldwei.hikisup.util.CommonMethod;
import com.oldwei.hikisup.util.PropertiesUtil;
import com.oldwei.hikisup.util.OsSelect;
import com.oldwei.hikisup.sdk.DemoApp.IsupTest;
import com.oldwei.hikisup.sdk.SdkService.CmsService.CmsDemo;
import com.oldwei.hikisup.sdk.SdkService.CmsService.HCISUPCMS;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class VoiceDemo {

    public static HCISUPStream hCEhomeVoice = null;
    static HCNetSDK hCNetSDK = null;
    public static int VoiceHandle = -1;   //语音监听句柄
    public static int lVoiceLinkHandle = -1; //语音连接句柄
    public static int VoicelServHandle = -1; //语音流媒体监听句柄
    static File fileEncode = null;

    static FileOutputStream outputStreamG711 = null;

    static VOICETALK_NEWLINK_CB VOICETALK_newlink_cb;//语音转发连接回调函数实现
    static VOICE_DATA_CB VOICE_data_cb; //语音数据回调函数

    static int voiceTalkSessionId = -1;

    static String configPath = "./config.properties";
    PropertiesUtil propertiesUtil;

    {
        try {
            propertiesUtil = new PropertiesUtil(configPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void voice_Init() {
        if (hCEhomeVoice == null) {
            if (!CreateSDKInstance()) {
                System.out.println("Load Stream SDK fail");
                return;
            }

        }

        if (OsSelect.isWindows()) {
            //设置libcrypto.so所在路径
            HCISUPCMS.BYTE_ARRAY ptrByteArrayCrypto = new HCISUPCMS.BYTE_ARRAY(256);
            String strPathCrypto = System.getProperty("user.dir") + "\\sdk\\windows\\libeay32.dll"; //Linux版本是libcrypto.so库文件的路径
            System.arraycopy(strPathCrypto.getBytes(), 0, ptrByteArrayCrypto.byValue, 0, strPathCrypto.length());
            ptrByteArrayCrypto.write();
            if (!hCEhomeVoice.NET_ESTREAM_SetSDKInitCfg(0, ptrByteArrayCrypto.getPointer())) {
                System.out.println("NET_ESTREAM_SetSDKInitCfg 0 failed, error:" + hCEhomeVoice.NET_ESTREAM_GetLastError());
            } else {
                System.out.println("NET_ESTREAM_SetSDKInitCfg 0 succeed");
            }
            //设置libssl.so所在路径
            HCISUPCMS.BYTE_ARRAY ptrByteArraySsl = new HCISUPCMS.BYTE_ARRAY(256);
            String strPathSsl = System.getProperty("user.dir") + "\\sdk\\windows\\ssleay32.dll";    //Linux版本是libssl.so库文件的路径
            System.arraycopy(strPathSsl.getBytes(), 0, ptrByteArraySsl.byValue, 0, strPathSsl.length());
            ptrByteArraySsl.write();
            if (!hCEhomeVoice.NET_ESTREAM_SetSDKInitCfg(1, ptrByteArraySsl.getPointer())) {
                System.out.println("NET_ESTREAM_SetSDKInitCfg 1 failed, error:" + hCEhomeVoice.NET_ESTREAM_GetLastError());
            } else {
                System.out.println("NET_ESTREAM_SetSDKInitCfg 1 succeed");
            }
            //语音流媒体初始化
            hCEhomeVoice.NET_ESTREAM_Init();
            //设置HCAapSDKCom组件库文件夹所在路径
            HCISUPCMS.BYTE_ARRAY ptrByteArrayCom = new HCISUPCMS.BYTE_ARRAY(256);
            String strPathCom = System.getProperty("user.dir") + "\\sdk\\windows\\HCAapSDKCom";      //只支持绝对路径，建议使用英文路径
            System.arraycopy(strPathCom.getBytes(), 0, ptrByteArrayCom.byValue, 0, strPathCom.length());
            ptrByteArrayCom.write();
            if (!hCEhomeVoice.NET_ESTREAM_SetSDKLocalCfg(5, ptrByteArrayCom.getPointer())) {
                System.out.println("NET_ESTREAM_SetSDKLocalCfg 5 failed, error:" + hCEhomeVoice.NET_ESTREAM_GetLastError());
            } else {
                System.out.println("NET_ESTREAM_SetSDKLocalCfg 5 succeed");
            }
            hCEhomeVoice.NET_ESTREAM_SetLogToFile(3, "./EHomeSDKLog", false);
        } else if (OsSelect.isLinux()) {
            //设置libcrypto.so所在路径
            HCISUPCMS.BYTE_ARRAY ptrByteArrayCrypto = new HCISUPCMS.BYTE_ARRAY(256);
            String strPathCrypto = System.getProperty("user.dir") + "/sdk/linux/libcrypto.so"; //Linux版本是libcrypto.so库文件的路径
            System.arraycopy(strPathCrypto.getBytes(), 0, ptrByteArrayCrypto.byValue, 0, strPathCrypto.length());
            ptrByteArrayCrypto.write();
            if (!hCEhomeVoice.NET_ESTREAM_SetSDKInitCfg(0, ptrByteArrayCrypto.getPointer())) {
                System.out.println("NET_ESTREAM_SetSDKInitCfg 0 failed, error:" + hCEhomeVoice.NET_ESTREAM_GetLastError());
            } else {
                System.out.println("NET_ESTREAM_SetSDKInitCfg 0 succeed");
            }
            //设置libssl.so所在路径
            HCISUPCMS.BYTE_ARRAY ptrByteArraySsl = new HCISUPCMS.BYTE_ARRAY(256);
            String strPathSsl = System.getProperty("user.dir") + "/sdk/linux/libssl.so";    //Linux版本是libssl.so库文件的路径
            System.arraycopy(strPathSsl.getBytes(), 0, ptrByteArraySsl.byValue, 0, strPathSsl.length());
            ptrByteArraySsl.write();
            if (!hCEhomeVoice.NET_ESTREAM_SetSDKInitCfg(1, ptrByteArraySsl.getPointer())) {
                System.out.println("NET_ESTREAM_SetSDKInitCfg 1 failed, error:" + hCEhomeVoice.NET_ESTREAM_GetLastError());
            } else {
                System.out.println("NET_ESTREAM_SetSDKInitCfg 1 succeed");
            }
            hCEhomeVoice.NET_ESTREAM_Init();

            //设置HCAapSDKCom组件库文件夹所在路径
            HCISUPCMS.BYTE_ARRAY ptrByteArrayCom = new HCISUPCMS.BYTE_ARRAY(256);
            String strPathCom = System.getProperty("user.dir") + "/sdk/linux/HCAapSDKCom/";      //只支持绝对路径，建议使用英文路径
            System.arraycopy(strPathCom.getBytes(), 0, ptrByteArrayCom.byValue, 0, strPathCom.length());
            ptrByteArrayCom.write();
            if (!hCEhomeVoice.NET_ESTREAM_SetSDKLocalCfg(5, ptrByteArrayCom.getPointer())) {
                System.out.println("NET_ESTREAM_SetSDKLocalCfg 5 failed, error:" + hCEhomeVoice.NET_ESTREAM_GetLastError());
            } else {
                System.out.println("NET_ESTREAM_SetSDKLocalCfg 5 succeed");
            }

            hCEhomeVoice.NET_ESTREAM_SetLogToFile(3, "..\\EHomeSDKLog", false);
        }
    }

    /**
     * 开启语音流媒体服务监听
     */
    public void startVoiceServeListen() {
        if (VOICETALK_newlink_cb == null) {
            VOICETALK_newlink_cb = new VOICETALK_NEWLINK_CB();
        }
        HCISUPStream.NET_EHOME_LISTEN_VOICETALK_CFG net_ehome_listen_voicetalk_cfg = new HCISUPStream.NET_EHOME_LISTEN_VOICETALK_CFG();
        net_ehome_listen_voicetalk_cfg.struIPAdress.szIP = propertiesUtil.readValue("VoiceSmsServerListenIP").getBytes();
        net_ehome_listen_voicetalk_cfg.struIPAdress.wPort = Short.parseShort(propertiesUtil.readValue("VoiceSmsServerListenPort"));
        net_ehome_listen_voicetalk_cfg.fnNewLinkCB = VOICETALK_newlink_cb;
        net_ehome_listen_voicetalk_cfg.byLinkMode = 0;
        net_ehome_listen_voicetalk_cfg.write();
        VoicelServHandle = hCEhomeVoice.NET_ESTREAM_StartListenVoiceTalk(net_ehome_listen_voicetalk_cfg);
        if (VoicelServHandle == -1) {
            System.out.println("NET_ESTREAM_StartListenPreview failed, error code:" + hCEhomeVoice.NET_ESTREAM_GetLastError());
            hCEhomeVoice.NET_ESTREAM_Fini();
            return;
        } else {
            String VoiceStreamListenInfo = new String(net_ehome_listen_voicetalk_cfg.struIPAdress.szIP).trim() + "_" + net_ehome_listen_voicetalk_cfg.struIPAdress.wPort;
            System.out.println("语音流媒体服务：" + VoiceStreamListenInfo + ",NET_ESTREAM_StartListenVoiceTalk succeed");
        }
    }

    /**
     * 开启语音转发
     */
    public void StartVoiceTrans(int lLoginID) {
        // 语音对讲开启请求的输入参数
        HCISUPCMS.NET_EHOME_VOICE_TALK_IN net_ehome_voice_talk_in = new HCISUPCMS.NET_EHOME_VOICE_TALK_IN();
        net_ehome_voice_talk_in.struStreamSever.szIP = propertiesUtil.readValue("VoiceSmsServerIP").getBytes();
        net_ehome_voice_talk_in.struStreamSever.wPort = Short.parseShort(propertiesUtil.readValue("VoiceSmsServerPort"));
        net_ehome_voice_talk_in.dwVoiceChan = 1; //语音通道号
        net_ehome_voice_talk_in.write();
        // 语音对讲开启请求的输出参数
        HCISUPCMS.NET_EHOME_VOICE_TALK_OUT net_ehome_voice_talk_out = new HCISUPCMS.NET_EHOME_VOICE_TALK_OUT();
        // 将语音对讲开启请求从CMS 发送给设备发送SMS 的地址和端口号给设备，设备自动为CMS 分配一个会话ID。
        if (!CmsDemo.hCEhomeCMS.NET_ECMS_StartVoiceWithStmServer(lLoginID, net_ehome_voice_talk_in, net_ehome_voice_talk_out)) {
            System.out.println("NET_ECMS_StartVoiceWithStmServer failed, error code:" + CmsDemo.hCEhomeCMS.NET_ECMS_GetLastError());
            return;
        } else {
            net_ehome_voice_talk_out.read();
            System.out.println("NET_ECMS_StartVoiceWithStmServer suss sessionID=" + net_ehome_voice_talk_out.lSessionID);
        }

        // 语音传输请求的输入参数
        HCISUPCMS.NET_EHOME_PUSHVOICE_IN struPushVoiceIn = new HCISUPCMS.NET_EHOME_PUSHVOICE_IN();
        struPushVoiceIn.dwSize = struPushVoiceIn.size();
        struPushVoiceIn.lSessionID = net_ehome_voice_talk_out.lSessionID;
        voiceTalkSessionId = net_ehome_voice_talk_out.lSessionID;
        // 语音传输请求的输出参数
        HCISUPCMS.NET_EHOME_PUSHVOICE_OUT struPushVoiceOut = new HCISUPCMS.NET_EHOME_PUSHVOICE_OUT();
        struPushVoiceOut.dwSize = struPushVoiceOut.size();
        // 将语音传输请求从CMS 发送给设备。设备自动连接SMS 并开始发送音频数据给SMS
        if (!CmsDemo.hCEhomeCMS.NET_ECMS_StartPushVoiceStream(lLoginID, struPushVoiceIn, struPushVoiceOut)) {
            System.out.println("NET_ECMS_StartPushVoiceStream failed, error code:" + CmsDemo.hCEhomeCMS.NET_ECMS_GetLastError());
            return;
        }
        System.out.println("NET_ECMS_StartPushVoiceStream success!\n");

        //发送音频数据
        FileInputStream voiceInputStream = null;
        int dataLength = 0;
        try {
            //创建从文件读取数据的FileInputStream流
            voiceInputStream = new FileInputStream(CommonMethod.getResFileAbsPath("audioFile/twoWayTalk.g7"));
            //返回文件的总字节数
            dataLength = voiceInputStream.available();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (dataLength < 0) {
            System.out.println("input file dataSize < 0");
            throw new RuntimeException("输入的文件");
//            return false;
        }

        HCNetSDK.BYTE_ARRAY ptrVoiceByte = new HCNetSDK.BYTE_ARRAY(dataLength);
        try {
            voiceInputStream.read(ptrVoiceByte.byValue);
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        ptrVoiceByte.write();

        int iEncodeSize = 0;
        HCNetSDK.NET_DVR_AUDIOENC_INFO enc_info = new HCNetSDK.NET_DVR_AUDIOENC_INFO();
        enc_info.write();
        Pointer encoder = hCNetSDK.NET_DVR_InitG711Encoder(enc_info);
        while ((dataLength - iEncodeSize) > 640) {
            HCNetSDK.BYTE_ARRAY ptrPcmData = new HCNetSDK.BYTE_ARRAY(640);
            System.arraycopy(ptrVoiceByte.byValue, iEncodeSize, ptrPcmData.byValue, 0, 640);
            ptrPcmData.write();

            HCNetSDK.BYTE_ARRAY ptrG711Data = new HCNetSDK.BYTE_ARRAY(320);
            ptrG711Data.write();

            HCNetSDK.NET_DVR_AUDIOENC_PROCESS_PARAM struEncParam = new HCNetSDK.NET_DVR_AUDIOENC_PROCESS_PARAM();
            struEncParam.in_buf = ptrPcmData.getPointer();
            struEncParam.out_buf = ptrG711Data.getPointer();
            struEncParam.out_frame_size = 320;
            struEncParam.g711_type = 0;//G711编码类型：0- U law，1- A law
            struEncParam.write();

            if (!hCNetSDK.NET_DVR_EncodeG711Frame(encoder, struEncParam)) {
                System.out.println("NET_DVR_EncodeG711Frame failed, error code:" + hCNetSDK.NET_DVR_GetLastError());
                hCNetSDK.NET_DVR_ReleaseG711Encoder(encoder);
            }
            struEncParam.read();
            ptrG711Data.read();

            long offsetG711 = 0;
            ByteBuffer buffersG711 = struEncParam.out_buf.getByteBuffer(offsetG711, struEncParam.out_frame_size);
            byte[] bytesG711 = new byte[struEncParam.out_frame_size];
            buffersG711.rewind();
            buffersG711.get(bytesG711);
            try {
                outputStreamG711.write(bytesG711);
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            iEncodeSize += 640;
            System.out.println("编码字节数：" + iEncodeSize);

            for (int i = 0; i < struEncParam.out_frame_size / 160; i++) {
                HCISUPStream.BYTE_ARRAY ptrG711Send = new HCISUPStream.BYTE_ARRAY(160);
                System.arraycopy(ptrG711Data.byValue, i * 160, ptrG711Send.byValue, 0, 160);
                ptrG711Send.write();
                HCISUPStream.NET_EHOME_VOICETALK_DATA struVoicTalkData = new HCISUPStream.NET_EHOME_VOICETALK_DATA();
                struVoicTalkData.pData = ptrG711Send.getPointer();
                struVoicTalkData.dwDataLen = 160;
                // 将音频数据发送给设备
                if (hCEhomeVoice.NET_ESTREAM_SendVoiceTalkData(lVoiceLinkHandle, struVoicTalkData) <= -1) {
                    System.out.println("NET_ESTREAM_SendVoiceTalkData failed, error code:" + hCEhomeVoice.NET_ESTREAM_GetLastError());
                }

                //需要实时速率发送数据
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

    }

    private FileOutputStream getFileFromAbsPath(String fileAbsPath) {
        File fileEncode = new File(fileAbsPath);  //保存音频编码数据

        if (!fileEncode.exists()) {
            try {
                fileEncode.createNewFile();
                return new FileOutputStream(fileEncode);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;
    }


    /**
     * 停止语音对讲
     */
    public void StopVoiceTrans(int lLoginID) {
        //SMS 停止语音对讲
        if (lVoiceLinkHandle >= 0) {
            if (!hCEhomeVoice.NET_ESTREAM_StopVoiceTalk(lVoiceLinkHandle)) {
                System.out.println("NET_ESTREAM_StopVoiceTalk failed, error code:" + hCEhomeVoice.NET_ESTREAM_GetLastError());
                return;
            }
        }
        //释放语音对讲请求资源
        if (!CmsDemo.hCEhomeCMS.NET_ECMS_StopVoiceTalkWithStmServer(lLoginID, voiceTalkSessionId)) {
            System.out.println("NET_ECMS_StopVoiceTalkWithStmServer failed, error code:" + CmsDemo.hCEhomeCMS.NET_ECMS_GetLastError());
            return;
        }
    }


    /**
     * 语音对讲请求的回调函数
     */
    public class VOICETALK_NEWLINK_CB implements HCISUPStream.VOICETALK_NEWLINK_CB {
        public boolean invoke(int lHandle, HCISUPStream.NET_EHOME_VOICETALK_NEWLINK_CB_INFO pNewLinkCBInfo, Pointer pUserData) {
            System.out.println("fVOICE_NEWLINK_CB callback");
            lVoiceLinkHandle = lHandle;
            HCISUPStream.NET_EHOME_VOICETALK_DATA_CB_PARAM net_ehome_voicetalk_data_cb_param = new HCISUPStream.NET_EHOME_VOICETALK_DATA_CB_PARAM();
            if (VOICE_data_cb == null) {
                VOICE_data_cb = new VOICE_DATA_CB();
            }
            net_ehome_voicetalk_data_cb_param.fnVoiceTalkDataCB = VOICE_data_cb;

            if (!hCEhomeVoice.NET_ESTREAM_SetVoiceTalkDataCB(lHandle, net_ehome_voicetalk_data_cb_param)) {
                System.out.println("NET_ESTREAM_SetVoiceTalkDataCB()错误代码号：" + hCEhomeVoice.NET_ESTREAM_GetLastError());
                return false;
            }
            return true;
        }

    }

    /**
     * 语音对讲数据的回调函数
     */
    public class VOICE_DATA_CB implements HCISUPStream.VOICETALK_DATA_CB {
        private File filePcm = null;

        private FileOutputStream outputStreamPcm = null;

        public VOICE_DATA_CB() {
            // 保存回调函数的音频数据
            filePcm = new File(CommonMethod.getResFileAbsPath("audioFile/DevicetoPlat.g7"));
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

        public boolean invoke(int lHandle, HCISUPStream.NET_EHOME_VOICETALK_DATA_CB_INFO pNewLinkCBInfo, Pointer pUserData) {
            //回调函数保存设备返回的语音数据
            //将设备发送过来的语音数据写入文件
            System.out.println("设备音频发送.....");
            VoiceHandle = lHandle;
            long offset = 0;
            ByteBuffer buffers = pNewLinkCBInfo.pData.getByteBuffer(offset, pNewLinkCBInfo.dwDataLen);
            byte[] bytes = new byte[pNewLinkCBInfo.dwDataLen];
            buffers.rewind();
            buffers.get(bytes);
            try {
                outputStreamPcm.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }

    }


    /**
     * 动态库加载
     *
     * @return
     */
    private static boolean CreateSDKInstance() {
        if (hCEhomeVoice == null) {
            synchronized (HCISUPStream.class) {
                String strDllPath = "";
                try {
                    if (OsSelect.isWindows())
                        //win系统加载库路径(路径不要带中文)
                        strDllPath = System.getProperty("user.dir") + "\\sdk\\windows\\HCISUPStream.dll";

                    else if (OsSelect.isLinux())
                        //Linux系统加载库路径(路径不要带中文)
                        strDllPath = System.getProperty("user.dir") + "/sdk/linux/libHCISUPStream.so";
                    hCEhomeVoice = (HCISUPStream) Native.loadLibrary(strDllPath, HCISUPStream.class);
                } catch (Exception ex) {
                    System.out.println("loadLibrary: " + strDllPath + " Error: " + ex.getMessage());
                    return false;
                }
            }
        }
        return true;

    }
}






















