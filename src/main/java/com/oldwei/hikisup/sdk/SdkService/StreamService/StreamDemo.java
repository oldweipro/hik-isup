package com.oldwei.hikisup.sdk.SdkService.StreamService;

import com.oldwei.hikisup.util.JavaCVProcessThread;
import com.oldwei.hikisup.util.PropertiesUtil;
import com.oldwei.hikisup.util.OsSelect;
import com.oldwei.hikisup.sdk.SdkService.AlarmService.HCISUPAlarm;
import com.oldwei.hikisup.sdk.SdkService.CmsService.CmsDemo;
import com.oldwei.hikisup.sdk.SdkService.CmsService.HCISUPCMS;
import com.oldwei.hikisup.sdk.UIModule.PlaybackVideoUI;
import com.oldwei.hikisup.util.FileUtil;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.examples.win32.W32API;
import com.sun.jna.ptr.IntByReference;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.ByteBuffer;

@Slf4j
public class StreamDemo {

    public static HCISUPStream hCEhomeStream = null;
    public static PlayCtrl playCtrl = null;
    static int m_lPlayBackLinkHandle = -1;   //回放句柄
    public static int m_lPlayBackListenHandle = -1; //回放监听句柄

    public static int StreamHandle = -1;   //预览监听句柄
    public static int lPreviewHandle = -1; //
    public static int sessionID = -1; //预览sessionID
    static int backSessionID = -1;  //回放sessionID
    static int Count = 0;
    static int iCount = 0;
    static IntByReference m_lPort = new IntByReference(-1);//回调预览时播放库端口指针
    static String configPath = "./config.properties";
    PropertiesUtil propertiesUtil;

    {
        try {
            propertiesUtil = new PropertiesUtil(configPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    HCISUPStream.NET_EHOME_PLAYBACK_LISTEN_PARAM struPlayBackListen = new HCISUPStream.NET_EHOME_PLAYBACK_LISTEN_PARAM();
    HCISUPAlarm.NET_EHOME_ALARM_LISTEN_PARAM net_ehome_alarm_listen_param = new HCISUPAlarm.NET_EHOME_ALARM_LISTEN_PARAM();
    HCISUPStream.NET_EHOME_LISTEN_PREVIEW_CFG struPreviewListen = new HCISUPStream.NET_EHOME_LISTEN_PREVIEW_CFG();
    static FPREVIEW_NEWLINK_CB_WIN fPREVIEW_NEWLINK_CB_WIN;//预览监听回调函数实现 - 带窗口
    static FPREVIEW_DATA_CB_WIN fPREVIEW_DATA_CB_WIN;//预览回调函数实现 - 带窗口

    static FPREVIEW_NEWLINK_CB_FILE fPREVIEW_NEWLINK_CB_FILE;//预览监听回调函数实现 - 文件模式
    private FPREVIEW_DATA_CB_FILE fPREVIEW_DATA_CB_FILE; // 预览回调函数实现 - 文件存储

    static PLAYBACK_NEWLINK_CB_WIN fPLAYBACK_NEWLINK_CB_WIN; //回放监听回调函数实现 - 带窗口
    static PLAYBACK_DATA_CB_WIN fPLAYBACK_DATA_CB_WIN;   //回放回调实现 - 带窗口

    static PLAYBACK_NEWLINK_CB_FILE fPLAYBACK_NEWLINK_CB_FILE; //回放监听回调函数实现 - 文件存储
    static PLAYBACK_DATA_CB_FILE fPLAYBACK_DATA_CB_FILE;   //回放回调实现 - 文件存储


    /**
     * 动态库加载
     *
     * @return
     */
    private static boolean CreateSDKInstance() {
        if (hCEhomeStream == null) {
            synchronized (HCISUPStream.class) {
                String strDllPath = "";
                try {
                    if (OsSelect.isWindows())
                        //win系统加载库路径
                        strDllPath = System.getProperty("user.dir") + "\\sdk\\windows\\HCISUPStream.dll";

                    else if (OsSelect.isLinux())
                        //Linux系统加载库路径
                        strDllPath = System.getProperty("user.dir") + "/sdk/linux/libHCISUPStream.so";
                    hCEhomeStream = (HCISUPStream) Native.loadLibrary(strDllPath, HCISUPStream.class);
                } catch (Exception ex) {
                    System.out.println("loadLibrary: " + strDllPath + " Error: " + ex.getMessage());
                    return false;
                }
            }
        }
        return true;
    }

    public void eStream_Init() {
        if (hCEhomeStream == null) {
            if (!CreateSDKInstance()) {
                System.out.println("Load Stream SDK fail");
                return;
            }
        }
        if (playCtrl == null) {
            if (!CreatePlayInstance()) {
                System.out.println("Load PlayCtrl fail");
                return;
            }

        }
        if (OsSelect.isWindows()) {
            HCISUPCMS.BYTE_ARRAY ptrByteArrayCrypto = new HCISUPCMS.BYTE_ARRAY(256);
            String strPathCrypto = System.getProperty("user.dir") + "\\sdk\\windows\\libeay32.dll"; //Linux版本是libcrypto.so库文件的路径
            System.arraycopy(strPathCrypto.getBytes(), 0, ptrByteArrayCrypto.byValue, 0, strPathCrypto.length());
            ptrByteArrayCrypto.write();
            if (!hCEhomeStream.NET_ESTREAM_SetSDKInitCfg(0, ptrByteArrayCrypto.getPointer())) {
                System.out.println("NET_ESTREAM_SetSDKInitCfg 0 failed, error:" + hCEhomeStream.NET_ESTREAM_GetLastError());
            }
            HCISUPCMS.BYTE_ARRAY ptrByteArraySsl = new HCISUPCMS.BYTE_ARRAY(256);
            String strPathSsl = System.getProperty("user.dir") + "\\sdk\\windows\\ssleay32.dll";    //Linux版本是libssl.so库文件的路径
            System.arraycopy(strPathSsl.getBytes(), 0, ptrByteArraySsl.byValue, 0, strPathSsl.length());
            ptrByteArraySsl.write();
            if (!hCEhomeStream.NET_ESTREAM_SetSDKInitCfg(1, ptrByteArraySsl.getPointer())) {
                System.out.println("NET_ESTREAM_SetSDKInitCfg 1 failed, error:" + hCEhomeStream.NET_ESTREAM_GetLastError());
            }
            //流媒体初始化
            hCEhomeStream.NET_ESTREAM_Init();
            //设置HCAapSDKCom组件库文件夹所在路径
            HCISUPCMS.BYTE_ARRAY ptrByteArrayCom = new HCISUPCMS.BYTE_ARRAY(256);
            String strPathCom = System.getProperty("user.dir") + "\\sdk\\windows\\HCAapSDKCom";      //只支持绝对路径，建议使用英文路径
            System.arraycopy(strPathCom.getBytes(), 0, ptrByteArrayCom.byValue, 0, strPathCom.length());
            ptrByteArrayCom.write();
            if (!hCEhomeStream.NET_ESTREAM_SetSDKLocalCfg(5, ptrByteArrayCom.getPointer())) {
                System.out.println("NET_ESTREAM_SetSDKLocalCfg 5 failed, error:" + hCEhomeStream.NET_ESTREAM_GetLastError());
            }
            hCEhomeStream.NET_ESTREAM_SetLogToFile(3, "..\\EHomeSDKLog", false);
        } else if (OsSelect.isLinux()) {
            //设置libcrypto.so所在路径
            HCISUPCMS.BYTE_ARRAY ptrByteArrayCrypto = new HCISUPCMS.BYTE_ARRAY(256);
            String strPathCrypto = System.getProperty("user.dir") + "/sdk/linux/libcrypto.so"; //Linux版本是libcrypto.so库文件的路径
            System.arraycopy(strPathCrypto.getBytes(), 0, ptrByteArrayCrypto.byValue, 0, strPathCrypto.length());
            ptrByteArrayCrypto.write();
            if (!hCEhomeStream.NET_ESTREAM_SetSDKInitCfg(0, ptrByteArrayCrypto.getPointer())) {
                System.out.println("NET_ESTREAM_SetSDKInitCfg 0 failed, error:" + hCEhomeStream.NET_ESTREAM_GetLastError());
            }
            //设置libssl.so所在路径
            HCISUPCMS.BYTE_ARRAY ptrByteArraySsl = new HCISUPCMS.BYTE_ARRAY(256);
            String strPathSsl = System.getProperty("user.dir") + "/sdk/linux/libssl.so";    //Linux版本是libssl.so库文件的路径
            System.arraycopy(strPathSsl.getBytes(), 0, ptrByteArraySsl.byValue, 0, strPathSsl.length());
            ptrByteArraySsl.write();
            if (!hCEhomeStream.NET_ESTREAM_SetSDKInitCfg(1, ptrByteArraySsl.getPointer())) {
                System.out.println("NET_ESTREAM_SetSDKInitCfg 1 failed, error:" + hCEhomeStream.NET_ESTREAM_GetLastError());
            }
            hCEhomeStream.NET_ESTREAM_Init();
            //设置HCAapSDKCom组件库文件夹所在路径
            HCISUPCMS.BYTE_ARRAY ptrByteArrayCom = new HCISUPCMS.BYTE_ARRAY(256);
            String strPathCom = System.getProperty("user.dir") + "/sdk/linux/HCAapSDKCom/";      //只支持绝对路径，建议使用英文路径
            System.arraycopy(strPathCom.getBytes(), 0, ptrByteArrayCom.byValue, 0, strPathCom.length());
            ptrByteArrayCom.write();
            if (!hCEhomeStream.NET_ESTREAM_SetSDKLocalCfg(5, ptrByteArrayCom.getPointer())) {
                System.out.println("NET_ESTREAM_SetSDKLocalCfg 5 failed, error:" + hCEhomeStream.NET_ESTREAM_GetLastError());
            }
            hCEhomeStream.NET_ESTREAM_SetLogToFile(3, "./EHomeSDKLog", false);
        }

    }

    /**
     * 开启实时预览监听(带界面窗口)
     */
    public void startRealPlayListen_Win() {
        // 预览监听
        // 预览监听回调函数实现 - 带窗口
        if (fPREVIEW_NEWLINK_CB_WIN == null) {
            fPREVIEW_NEWLINK_CB_WIN = new FPREVIEW_NEWLINK_CB_WIN();
        }
        System.arraycopy(propertiesUtil.readValue("SmsServerListenIP").getBytes(), 0, struPreviewListen.struIPAdress.szIP, 0, propertiesUtil.readValue("SmsServerListenIP").length());
        struPreviewListen.struIPAdress.wPort = Short.parseShort(propertiesUtil.readValue("SmsServerListenPort")); //流媒体服务器监听端口
        struPreviewListen.fnNewLinkCB = fPREVIEW_NEWLINK_CB_WIN; //预览连接请求回调函数
        struPreviewListen.pUser = null;
        struPreviewListen.byLinkMode = 0; //0- TCP方式，1- UDP方式
        struPreviewListen.write();

        if (StreamHandle < 0) {
            StreamHandle = hCEhomeStream.NET_ESTREAM_StartListenPreview(struPreviewListen);
            if (StreamHandle == -1) {

                System.out.println("NET_ESTREAM_StartListenPreview failed, error code:" + hCEhomeStream.NET_ESTREAM_GetLastError());
                hCEhomeStream.NET_ESTREAM_Fini();
                return;
            } else {
                String StreamListenInfo = new String(struPreviewListen.struIPAdress.szIP).trim() + "_" + struPreviewListen.struIPAdress.wPort;
                log.info("流媒体服务：{}, NET_ESTREAM_StartListenPreview succeed", StreamListenInfo);
                System.out.println();
            }
        }
    }

    /**
     * 开启实时预览监听(保存到本地文件)
     */
    public void startRealPlayListen_File(String filename) {
        //预览监听
        if (fPREVIEW_NEWLINK_CB_FILE == null) {
            fPREVIEW_NEWLINK_CB_FILE = new FPREVIEW_NEWLINK_CB_FILE(filename);
        }
        System.arraycopy(propertiesUtil.readValue("SmsServerListenIP").getBytes(), 0, struPreviewListen.struIPAdress.szIP, 0, propertiesUtil.readValue("SmsServerListenIP").length());
        struPreviewListen.struIPAdress.wPort = Short.parseShort(propertiesUtil.readValue("SmsServerListenPort")); //流媒体服务器监听端口
        struPreviewListen.fnNewLinkCB = fPREVIEW_NEWLINK_CB_FILE; //预览连接请求回调函数
        struPreviewListen.pUser = null;
        struPreviewListen.byLinkMode = 0; //0- TCP方式，1- UDP方式
        struPreviewListen.write();

        if (StreamHandle < 0) {
            StreamHandle = hCEhomeStream.NET_ESTREAM_StartListenPreview(struPreviewListen);
            if (StreamHandle == -1) {

                System.out.println("NET_ESTREAM_StartListenPreview failed, error code:" + hCEhomeStream.NET_ESTREAM_GetLastError());
                hCEhomeStream.NET_ESTREAM_Fini();
            } else {
                String StreamListenInfo = new String(struPreviewListen.struIPAdress.szIP).trim() + "_" + struPreviewListen.struIPAdress.wPort;
                System.out.println("流媒体服务：" + StreamListenInfo + ",NET_ESTREAM_StartListenPreview succeed");
            }
        }
    }

    /**
     * 设置回放监听（带界面窗口）
     * 启用流媒体服务器（SMS）的回放监听并注册回调函数以接收设备连接请求
     */
    public void startPlayBackListen_WIN() {
        //回放监听
        if (fPLAYBACK_NEWLINK_CB_WIN == null) {
            fPLAYBACK_NEWLINK_CB_WIN = new PLAYBACK_NEWLINK_CB_WIN();
        }
        System.arraycopy(propertiesUtil.readValue("SmsBackServerListenIP").getBytes(), 0, struPlayBackListen.struIPAdress.szIP, 0, propertiesUtil.readValue("SmsBackServerListenIP").length());
        struPlayBackListen.struIPAdress.wPort = Short.parseShort(propertiesUtil.readValue("SmsBackServerListenPort")); //流媒体服务器监听端口
        struPlayBackListen.fnNewLinkCB = fPLAYBACK_NEWLINK_CB_WIN;
        struPlayBackListen.byLinkMode = 0; //0- TCP方式，1- UDP方式
        if (m_lPlayBackLinkHandle < 0) {
            m_lPlayBackListenHandle = hCEhomeStream.NET_ESTREAM_StartListenPlayBack(struPlayBackListen);
            if (m_lPlayBackListenHandle < -1) {
                System.out.println("NET_ESTREAM_StartListenPlayBack failed, error code:" + hCEhomeStream.NET_ESTREAM_GetLastError());
                hCEhomeStream.NET_ESTREAM_Fini();
                return;
            } else {
                String BackStreamListenInfo = new String(struPlayBackListen.struIPAdress.szIP).trim() + "_" + struPlayBackListen.struIPAdress.wPort;
                System.out.println("回放流媒体服务：" + BackStreamListenInfo + ",NET_ESTREAM_StartListenPlayBack succeed");
            }
        }
    }

    /**
     * 设置回放监听（保存到本地文件）
     * 启用流媒体服务器（SMS）的回放监听并注册回调函数以接收设备连接请求
     */
    public void startPlayBackListen_FILE() {
        //回放监听
        if (fPLAYBACK_NEWLINK_CB_FILE == null) {
            fPLAYBACK_NEWLINK_CB_FILE = new PLAYBACK_NEWLINK_CB_FILE();
        }
        System.arraycopy(propertiesUtil.readValue("SmsBackServerListenIP").getBytes(), 0, struPlayBackListen.struIPAdress.szIP, 0, propertiesUtil.readValue("SmsBackServerListenIP").length());
        struPlayBackListen.struIPAdress.wPort = Short.parseShort(propertiesUtil.readValue("SmsBackServerListenPort")); //流媒体服务器监听端口
        struPlayBackListen.fnNewLinkCB = fPLAYBACK_NEWLINK_CB_FILE;
        struPlayBackListen.byLinkMode = 0; //0- TCP方式，1- UDP方式
        if (m_lPlayBackLinkHandle < 0) {
            m_lPlayBackListenHandle = hCEhomeStream.NET_ESTREAM_StartListenPlayBack(struPlayBackListen);
            if (m_lPlayBackListenHandle < -1) {
                System.out.println("NET_ESTREAM_StartListenPlayBack failed, error code:" + hCEhomeStream.NET_ESTREAM_GetLastError());
                hCEhomeStream.NET_ESTREAM_Fini();
                return;
            } else {
                String BackStreamListenInfo = new String(struPlayBackListen.struIPAdress.szIP).trim() + "_" + struPlayBackListen.struIPAdress.wPort;
                System.out.println("回放流媒体服务：" + BackStreamListenInfo + ",NET_ESTREAM_StartListenPlayBack succeed");
            }
        }
    }

    /**
     * 开启预览，
     *
     * @param lChannel 预览通道号
     */
    public void RealPlay(int lLoginID, int lChannel) {
        HCISUPCMS.NET_EHOME_PREVIEWINFO_IN struPreviewIn = new HCISUPCMS.NET_EHOME_PREVIEWINFO_IN();
        struPreviewIn.iChannel = lChannel; //通道号
        struPreviewIn.dwLinkMode = 0; //0- TCP方式，1- UDP方式
        struPreviewIn.dwStreamType = 0; //码流类型：0- 主码流，1- 子码流, 2- 第三码流
        struPreviewIn.struStreamSever.szIP = propertiesUtil.readValue("SmsServerIP").getBytes();//流媒体服务器IP地址,公网地址
        struPreviewIn.struStreamSever.wPort = Short.parseShort(propertiesUtil.readValue("SmsServerPort")); //流媒体服务器端口，需要跟服务器启动监听端口一致
        struPreviewIn.write();
        //预览请求
        HCISUPCMS.NET_EHOME_PREVIEWINFO_OUT struPreviewOut = new HCISUPCMS.NET_EHOME_PREVIEWINFO_OUT();
        boolean getRS = CmsDemo.hCEhomeCMS.NET_ECMS_StartGetRealStream(lLoginID, struPreviewIn, struPreviewOut);
        //Thread.sleep(10000);
        if (!CmsDemo.hCEhomeCMS.NET_ECMS_StartGetRealStream(lLoginID, struPreviewIn, struPreviewOut)) {
            System.out.println("NET_ECMS_StartGetRealStream failed, error code:" + CmsDemo.hCEhomeCMS.NET_ECMS_GetLastError());
            return;
        } else {
            struPreviewOut.read();
            System.out.println("NET_ECMS_StartGetRealStream succeed, sessionID:" + struPreviewOut.lSessionID);
            sessionID = struPreviewOut.lSessionID;
        }
        HCISUPCMS.NET_EHOME_PUSHSTREAM_IN struPushInfoIn = new HCISUPCMS.NET_EHOME_PUSHSTREAM_IN();
        struPushInfoIn.read();
        struPushInfoIn.dwSize = struPushInfoIn.size();
        struPushInfoIn.lSessionID = sessionID;
        struPushInfoIn.write();
        HCISUPCMS.NET_EHOME_PUSHSTREAM_OUT struPushInfoOut = new HCISUPCMS.NET_EHOME_PUSHSTREAM_OUT();
        struPushInfoOut.read();
        struPushInfoOut.dwSize = struPushInfoOut.size();
        struPushInfoOut.write();
        if (!CmsDemo.hCEhomeCMS.NET_ECMS_StartPushRealStream(lLoginID, struPushInfoIn, struPushInfoOut)) {
            System.out.println("NET_ECMS_StartPushRealStream failed, error code:" + CmsDemo.hCEhomeCMS.NET_ECMS_GetLastError());
            return;
        } else {
            System.out.println("NET_ECMS_StartPushRealStream succeed, sessionID:" + struPushInfoIn.lSessionID);
        }
    }

    /**
     * 停止预览,Stream服务停止实时流转发，CMS向设备发送停止预览请求
     */
    public void StopRealPlay(int lLoginID) {
        if (!hCEhomeStream.NET_ESTREAM_StopPreview(lPreviewHandle)) {
            System.out.println("NET_ESTREAM_StopPreview failed,err = " + hCEhomeStream.NET_ESTREAM_GetLastError());
            return;
        }
        System.out.println("停止Stream的实时流转发");
        if (!CmsDemo.hCEhomeCMS.NET_ECMS_StopGetRealStream(lLoginID, sessionID)) {
            System.out.println("NET_ECMS_StopGetRealStream failed,err = " + CmsDemo.hCEhomeCMS.NET_ECMS_GetLastError());
            return;
        }
        System.out.println("CMS发送预览停止请求");
    }


    /**
     * 按时间回放，ISUP5.0接入支持，需要确认设备上对应时间段是否有录像存储，设备中没有录像文件无法集成此模块
     *
     * @param lchannel 通道号
     */
    public void PlayBackByTime(int lLoginID, int lchannel) {

        HCISUPCMS.NET_EHOME_PLAYBACK_INFO_IN m_struPlayBackInfoIn = new HCISUPCMS.NET_EHOME_PLAYBACK_INFO_IN();
        m_struPlayBackInfoIn.read();
        m_struPlayBackInfoIn.dwSize = m_struPlayBackInfoIn.size();
        m_struPlayBackInfoIn.dwChannel = lchannel; //通道号
        m_struPlayBackInfoIn.byPlayBackMode = 1;//0- 按文件名回放，1- 按时间回放
        m_struPlayBackInfoIn.unionPlayBackMode.setType(HCISUPCMS.NET_EHOME_PLAYBACKBYTIME.class);
        // FIXME 这里的时间参数需要根据实际设备上存在的时间段进行设置, 否则可能可能提示：3505 - 该时间段内无录像。
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStartTime.wYear = 2023;
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStartTime.byMonth = 8;
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStartTime.byDay = 17;
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStartTime.byHour = 15;
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStartTime.byMinute = 0;
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStartTime.bySecond = 0;

        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStopTime.wYear = 2023;
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStopTime.byMonth = 8;
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStopTime.byDay = 17;
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStopTime.byHour = 16;
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStopTime.byMinute = 20;
        m_struPlayBackInfoIn.unionPlayBackMode.struPlayBackbyTime.struStopTime.bySecond = 0;

        System.arraycopy(propertiesUtil.readValue("SmsBackServerIP").getBytes(), 0, m_struPlayBackInfoIn.struStreamSever.szIP,
                0, propertiesUtil.readValue("SmsBackServerIP").length());
        m_struPlayBackInfoIn.struStreamSever.wPort = Short.parseShort(propertiesUtil.readValue("SmsBackServerPort"));
        m_struPlayBackInfoIn.write();
        HCISUPCMS.NET_EHOME_PLAYBACK_INFO_OUT m_struPlayBackInfoOut = new HCISUPCMS.NET_EHOME_PLAYBACK_INFO_OUT();
        m_struPlayBackInfoOut.write();
        if (!CmsDemo.hCEhomeCMS.NET_ECMS_StartPlayBack(lLoginID, m_struPlayBackInfoIn, m_struPlayBackInfoOut)) {
            System.out.println("NET_ECMS_StartPlayBack failed, error code:" + CmsDemo.hCEhomeCMS.NET_ECMS_GetLastError());
            return;
        } else {
            m_struPlayBackInfoOut.read();
            System.out.println("NET_ECMS_StartPlayBack succeed, lSessionID:" + m_struPlayBackInfoOut.lSessionID);
        }

        HCISUPCMS.NET_EHOME_PUSHPLAYBACK_IN m_struPushPlayBackIn = new HCISUPCMS.NET_EHOME_PUSHPLAYBACK_IN();
        m_struPushPlayBackIn.read();
        m_struPushPlayBackIn.dwSize = m_struPushPlayBackIn.size();
        m_struPushPlayBackIn.lSessionID = m_struPlayBackInfoOut.lSessionID;
        m_struPushPlayBackIn.write();

        backSessionID = m_struPushPlayBackIn.lSessionID;

        HCISUPCMS.NET_EHOME_PUSHPLAYBACK_OUT m_struPushPlayBackOut = new HCISUPCMS.NET_EHOME_PUSHPLAYBACK_OUT();
        m_struPushPlayBackOut.read();
        m_struPushPlayBackOut.dwSize = m_struPushPlayBackOut.size();
        m_struPushPlayBackOut.write();

        if (!CmsDemo.hCEhomeCMS.NET_ECMS_StartPushPlayBack(lLoginID, m_struPushPlayBackIn, m_struPushPlayBackOut)) {
            System.out.println("NET_ECMS_StartPushPlayBack failed, error code:" + CmsDemo.hCEhomeCMS.NET_ECMS_GetLastError());
            return;
        } else {
            System.out.println("NET_ECMS_StartPushPlayBack succeed, sessionID:" + m_struPushPlayBackIn.lSessionID + ",lUserID:" + lLoginID);
        }
    }


    /**
     * 停止回放
     */
    public void stopPlayBackByTime(int lLoginID) {
        if (!CmsDemo.hCEhomeCMS.NET_ECMS_StopPlayBack(lLoginID, backSessionID)) {
            System.out.println("NET_ECMS_StopPlayBack failed,err = " + CmsDemo.hCEhomeCMS.NET_ECMS_GetLastError());
            return;
        }
        System.out.println("CMS发送回放停止请求");
        if (!hCEhomeStream.NET_ESTREAM_StopPlayBack(m_lPlayBackLinkHandle)) {
            System.out.println("NET_ESTREAM_StopPlayBack failed,err = " + hCEhomeStream.NET_ESTREAM_GetLastError());
            return;
        }
        System.out.println("停止回放Stream服务的实时流转发");
    }

    //测试暂停
    public void testPause(int lLoginID) {
        HCISUPCMS.NET_EHOME_PLAYBACK_PAUSE_RESTART_PARAM struPlaybackPauseParam = new HCISUPCMS.NET_EHOME_PLAYBACK_PAUSE_RESTART_PARAM();
        struPlaybackPauseParam.read();
        struPlaybackPauseParam.lSessionID = backSessionID;
        struPlaybackPauseParam.write();
        if (!CmsDemo.hCEhomeCMS.NET_ECMS_PlayBackOperate(lLoginID, 0, struPlaybackPauseParam.getPointer())) {
            System.out.println("NET_ECMS_PlayBackOperate failed, error code:" + CmsDemo.hCEhomeCMS.NET_ECMS_GetLastError());
            return;
        } else {
            System.out.println("NET_ECMS_PlayBackOperate succeed, sessionID:" + backSessionID);
        }
    }

    /**
     * 播放库加载
     *
     * @return
     */
    private static boolean CreatePlayInstance() {
        if (playCtrl == null) {
            synchronized (PlayCtrl.class) {
                String strPlayPath = "";
                try {
                    if (OsSelect.isWindows())
                        //win系统加载库路径(路径不要带中文)
                        strPlayPath = System.getProperty("user.dir") + "\\sdk\\windows\\PlayCtrl.dll";
                    else if (OsSelect.isLinux())
                        //Linux系统加载库路径(路径不要带中文)
                        strPlayPath = System.getProperty("user.dir") + "/sdk/linux/libPlayCtrl.so";
                    playCtrl = (PlayCtrl) Native.loadLibrary(strPlayPath, PlayCtrl.class);

                } catch (Exception ex) {
                    System.out.println("loadLibrary: " + strPlayPath + " Error: " + ex.getMessage());
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * 实时预览数据回调（带窗口）调取实时
     */
    public class FPREVIEW_NEWLINK_CB_WIN implements HCISUPStream.PREVIEW_NEWLINK_CB {
        public boolean invoke(int lLinkHandle, HCISUPStream.NET_EHOME_NEWLINK_CB_MSG pNewLinkCBMsg, Pointer pUserData) {
            System.out.println("FPREVIEW_NEWLINK_CB_WIN callback");

            //预览数据回调参数
            lPreviewHandle = lLinkHandle;
            HCISUPStream.NET_EHOME_PREVIEW_DATA_CB_PARAM struDataCB = new HCISUPStream.NET_EHOME_PREVIEW_DATA_CB_PARAM();
            if (fPREVIEW_DATA_CB_WIN == null) {
                fPREVIEW_DATA_CB_WIN = new FPREVIEW_DATA_CB_WIN();
            }
            struDataCB.fnPreviewDataCB = fPREVIEW_DATA_CB_WIN;

            if (!hCEhomeStream.NET_ESTREAM_SetPreviewDataCB(lLinkHandle, struDataCB)) {
                System.out.println("NET_ESTREAM_SetPreviewDataCB failed err:：" + hCEhomeStream.NET_ESTREAM_GetLastError());
                return false;
            }
            return true;
        }
    }

    /**
     * 实时预览数据回调（预览数据存储到文件）
     */
    public class FPREVIEW_NEWLINK_CB_FILE implements HCISUPStream.PREVIEW_NEWLINK_CB {
        //        private PipedOutputStream outputStream;
//
//        FPREVIEW_NEWLINK_CB_FILE(PipedOutputStream outputStream) {
//            this.outputStream = outputStream;
//        }
        private String filename;

        public FPREVIEW_NEWLINK_CB_FILE(String filename) {
            this.filename = filename;
        }

        public boolean invoke(int lLinkHandle, HCISUPStream.NET_EHOME_NEWLINK_CB_MSG pNewLinkCBMsg, Pointer pUserData) {
            System.out.println("FPREVIEW_NEWLINK_CB_File callback");

            //预览数据回调参数
            lPreviewHandle = lLinkHandle;
            HCISUPStream.NET_EHOME_PREVIEW_DATA_CB_PARAM struDataCB = new HCISUPStream.NET_EHOME_PREVIEW_DATA_CB_PARAM();
            if (fPREVIEW_DATA_CB_FILE == null) {
                fPREVIEW_DATA_CB_FILE = new FPREVIEW_DATA_CB_FILE(filename);
            }
            struDataCB.fnPreviewDataCB = fPREVIEW_DATA_CB_FILE;

            if (!hCEhomeStream.NET_ESTREAM_SetPreviewDataCB(lLinkHandle, struDataCB)) {
                System.out.println("NET_ESTREAM_SetPreviewDataCB failed err:：" + hCEhomeStream.NET_ESTREAM_GetLastError());
                return false;
            }
            return true;
        }
    }

    /**
     * 预览数据的回调函数 - 窗口实时预览
     */
    public class FPREVIEW_DATA_CB_WIN implements HCISUPStream.PREVIEW_DATA_CB {
        //实时流回调函数/
        public void invoke(int iPreviewHandle, HCISUPStream.NET_EHOME_PREVIEW_CB_MSG pPreviewCBMsg, Pointer pUserData) {
            if (Count == 100) {//降低打印频率
                System.out.println("调试实时视频流 FPREVIEW_DATA_CB callback, data length:" + pPreviewCBMsg.dwDataLen);
                Count = 0;
            }
            Count++;
            //播放库SDK解码显示在win窗口上，
            switch (pPreviewCBMsg.byDataType) {
                case HCNetSDK.NET_DVR_SYSHEAD: //系统头
                {
                    boolean b_port = playCtrl.PlayM4_GetPort(m_lPort);
                    if (b_port == false) //获取播放库未使用的通道号
                    {
                        break;
                    }
                    if (pPreviewCBMsg.dwDataLen > 0) {
                        if (!playCtrl.PlayM4_SetStreamOpenMode(m_lPort.getValue(), PlayCtrl.STREAME_REALTIME))  //设置实时流播放模式
                        {
                            break;
                        }

                        if (!playCtrl.PlayM4_OpenStream(m_lPort.getValue(), pPreviewCBMsg.pRecvdata, pPreviewCBMsg.dwDataLen, 1024 * 1024)) //打开流接口
                        {
                            break;
                        }
//                        W32API.HWND hwnd = new W32API.HWND(Native.getComponentPointer(PreviewVideoUI.panelRealPlay));
//                        if (!playCtrl.PlayM4_Play(m_lPort.getValue(), hwnd)) //播放开始
//                        {
//                            break;
//                        }
                    }
                }
                case HCNetSDK.NET_DVR_STREAMDATA:   //码流数据
                {
                    if ((pPreviewCBMsg.dwDataLen > 0) && (m_lPort.getValue() != -1)) {
                        // 推流过快可能导致推流失败，这里处理重试逻辑
                        int tryInputTime = 5;
                        for (int i = 0; i < tryInputTime; i++) {
                            long offset = 0;
                            byte[] videoStreamData = pPreviewCBMsg.pRecvdata.getByteArray(offset, pPreviewCBMsg.dwDataLen);
                            FileUtil.writeFile("output.mp4", videoStreamData);
//                            if (!playCtrl.PlayM4_InputData(m_lPort.getValue(), pPreviewCBMsg.pRecvdata, pPreviewCBMsg.dwDataLen))  //输入流数据
//                            {
//                                if (i >= tryInputTime - 1) {
//                                    System.out.println("PlayM4_InputData,failed err:" + playCtrl.PlayM4_GetLastError(m_lPort.getValue()));
//                                }
//                                try {
//                                    // FIXME 这里根据具体业务场景自行调整等待时间
//                                    Thread.sleep(200);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
//                            } else {
//                                break;
//                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 预览数据的回调函数 - 保存到文件
     */
    public class FPREVIEW_DATA_CB_FILE implements HCISUPStream.PREVIEW_DATA_CB {
        private String filename;

        public FPREVIEW_DATA_CB_FILE(String filename) {
            this.filename = filename;
        }

        //        JavaCVProcessThread t = null;
        //实时流回调函数/
        public void invoke(int iPreviewHandle, HCISUPStream.NET_EHOME_PREVIEW_CB_MSG pPreviewCBMsg, Pointer pUserData) {
            if (Count == 100) {//降低打印频率
                System.out.println("回调预览数据, 长度:" + pPreviewCBMsg.dwDataLen);
                Count = 0;
            }
            Count++;
            // 推流过快可能导致推流失败，这里处理重试逻辑
            int offset = 0;
            byte[] videoStreamData = pPreviewCBMsg.pRecvdata.getByteArray(offset, pPreviewCBMsg.dwDataLen);
            FileUtil.writeFile("/opt/hik-isup/video/" + filename, videoStreamData);
//            try {
//                if (Objects.isNull(t)) {
//                    //启动javacv解析处理器线程
//                    t = new JavaCVProcessThread();
//                    t.start();
//                }
//                if (Objects.nonNull(t)) {
//                    //写出视频码流到javacv多线程解析处理器
//                    t.push(videoStreamData, pPreviewCBMsg.dwDataLen);
//                }
//            } catch (IOException e) {
//                System.out.println("估计又是管道关闭.");
//            }
        }
    }

    /**
     * 回放请求的回调函数(带窗口)
     */
    public static class PLAYBACK_NEWLINK_CB_WIN implements HCISUPStream.PLAYBACK_NEWLINK_CB {
        public boolean invoke(int lPlayBackLinkHandle, HCISUPStream.NET_EHOME_PLAYBACK_NEWLINK_CB_INFO pNewLinkCBInfo, Pointer pUserData) {
            pNewLinkCBInfo.read();
            System.out.println("PLAYBACK_NEWLINK_CB callback, szDeviceID:" + new String(pNewLinkCBInfo.szDeviceID).trim()
                    + ",lSessionID:" + pNewLinkCBInfo.lSessionID
                    + ",dwChannelNo:" + pNewLinkCBInfo.dwChannelNo);
            m_lPlayBackLinkHandle = lPlayBackLinkHandle;
            HCISUPStream.NET_EHOME_PLAYBACK_DATA_CB_PARAM struCBParam = new HCISUPStream.NET_EHOME_PLAYBACK_DATA_CB_PARAM();
            //预览数据回调参数
            if (fPLAYBACK_DATA_CB_WIN == null) {
                fPLAYBACK_DATA_CB_WIN = new PLAYBACK_DATA_CB_WIN();
            }
            struCBParam.fnPlayBackDataCB = fPLAYBACK_DATA_CB_WIN;
            struCBParam.byStreamFormat = 0;
            struCBParam.write();
            if (!hCEhomeStream.NET_ESTREAM_SetPlayBackDataCB(lPlayBackLinkHandle, struCBParam)) {
                System.out.println("NET_ESTREAM_SetPlayBackDataCB failed");
            }
            return true;
        }
    }

    /**
     * 回放请求的回调函数(文件模式)
     */
    public static class PLAYBACK_NEWLINK_CB_FILE implements HCISUPStream.PLAYBACK_NEWLINK_CB {
        public boolean invoke(int lPlayBackLinkHandle, HCISUPStream.NET_EHOME_PLAYBACK_NEWLINK_CB_INFO pNewLinkCBInfo, Pointer pUserData) {
            pNewLinkCBInfo.read();
            System.out.println("PLAYBACK_NEWLINK_CB callback, szDeviceID:" + new String(pNewLinkCBInfo.szDeviceID).trim()
                    + ",lSessionID:" + pNewLinkCBInfo.lSessionID
                    + ",dwChannelNo:" + pNewLinkCBInfo.dwChannelNo);
            m_lPlayBackLinkHandle = lPlayBackLinkHandle;
            HCISUPStream.NET_EHOME_PLAYBACK_DATA_CB_PARAM struCBParam = new HCISUPStream.NET_EHOME_PLAYBACK_DATA_CB_PARAM();
            //预览数据回调参数
            if (fPLAYBACK_DATA_CB_FILE == null) {
                fPLAYBACK_DATA_CB_FILE = new PLAYBACK_DATA_CB_FILE();
            }
            struCBParam.fnPlayBackDataCB = fPLAYBACK_DATA_CB_FILE;
            struCBParam.byStreamFormat = 0;
            struCBParam.write();
            if (!hCEhomeStream.NET_ESTREAM_SetPlayBackDataCB(lPlayBackLinkHandle, struCBParam)) {
                System.out.println("NET_ESTREAM_SetPlayBackDataCB failed");
            }
            return true;
        }
    }

    /**
     * 回放数据的回调函数(码流数据)
     */
    public static class PLAYBACK_DATA_CB_WIN implements HCISUPStream.PLAYBACK_DATA_CB {
        //实时流回调函数
        public boolean invoke(int iPlayBackLinkHandle, HCISUPStream.NET_EHOME_PLAYBACK_DATA_CB_INFO pDataCBInfo, Pointer pUserData) {
            if (iCount == 500) {//降低打印频率
                System.out.println("PLAYBACK_DATA_CB callback , dwDataLen:" + pDataCBInfo.dwDataLen + ",dwType:" + pDataCBInfo.dwType);
                iCount = 0;
            }
            iCount++;
            //播放库SDK解码显示在win窗口上，
            switch (pDataCBInfo.dwType) {
                case HCNetSDK.NET_DVR_SYSHEAD: //系统头
                    boolean b_port = playCtrl.PlayM4_GetPort(m_lPort);
                    if (b_port == false) //获取播放库未使用的通道号
                    {
                        break;
                    }
                    if (pDataCBInfo.dwDataLen > 0) {
                        if (!playCtrl.PlayM4_SetOverlayMode(m_lPort.getValue(), false, 0)) {
                            break;
                        }

                        if (!playCtrl.PlayM4_SetStreamOpenMode(m_lPort.getValue(), PlayCtrl.STREAME_FILE))  //设置文件流播放模式
                        {
                            break;
                        }

                        if (!playCtrl.PlayM4_OpenStream(m_lPort.getValue(), pDataCBInfo.pData, pDataCBInfo.dwDataLen, 2 * 1024 * 1024)) //打开流接口
                        {
                            break;
                        }
//                        W32API.HWND hwnd = new W32API.HWND(Native.getComponentPointer(PlaybackVideoUI.panelPlay));
//                        if (!playCtrl.PlayM4_Play(m_lPort.getValue(), hwnd)) //播放开始
//                        {
//                            break;
//                        }
                    }
                case HCNetSDK.NET_DVR_STREAMDATA:   //码流数据
                    if ((pDataCBInfo.dwDataLen > 0) && (m_lPort.getValue() != -1)) {
                        // 推流过快可能导致推流失败，这里处理重试逻辑
                        int tryInputTime = 5;
                        for (int i = 0; i < tryInputTime; i++) {
                            boolean bRet = playCtrl.PlayM4_InputData(m_lPort.getValue(), pDataCBInfo.pData, pDataCBInfo.dwDataLen);
                            if (!bRet) {
                                if (i >= tryInputTime - 1) {
                                    System.out.println("PlayM4_InputData,failed err:" + playCtrl.PlayM4_GetLastError(m_lPort.getValue()));
                                }
                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                break;
                            }
                        }
                    }
                    break;
            }
            return true;
        }
    }


    /**
     * 回放数据的回调函数(码流数据)
     */
    public static class PLAYBACK_DATA_CB_FILE implements HCISUPStream.PLAYBACK_DATA_CB {

        private File playbackFile = new File(System.getProperty("user.dir") + "\\outputFiles\\playbackVideo.mp4");
        private FileOutputStream playbackFileOutput;

        //实时流回调函数
        public boolean invoke(int iPlayBackLinkHandle, HCISUPStream.NET_EHOME_PLAYBACK_DATA_CB_INFO pDataCBInfo, Pointer pUserData) {
            if (iCount == 500) {//降低打印频率
                System.out.println("PLAYBACK_DATA_CB callback , dwDataLen:" + pDataCBInfo.dwDataLen + ",dwType:" + pDataCBInfo.dwType);
                iCount = 0;
            }
            iCount++;
            //仅保存回放的视频流文件
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
}



