package com.oldwei.hikisup.sdk.SdkService.StreamService;

import com.oldwei.hikisup.util.PropertiesUtil;
import com.oldwei.hikisup.util.OsSelect;
import com.oldwei.hikisup.sdk.SdkService.CmsService.CmsDemo;
import com.oldwei.hikisup.sdk.SdkService.CmsService.HCISUPCMS;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class StreamDemo {

    public static HCISUPStream hCEhomeStream = null;
    public static int m_lPlayBackListenHandle = -1; //回放监听句柄

    public static int StreamHandle = -1;   //预览监听句柄
    public static int lPreviewHandle = -1; //
    public static int sessionID = -1; //预览sessionID
    static String configPath = "./config.properties";
    PropertiesUtil propertiesUtil;

    {
        try {
            propertiesUtil = new PropertiesUtil(configPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    HCISUPStream.NET_EHOME_LISTEN_PREVIEW_CFG struPreviewListen = new HCISUPStream.NET_EHOME_LISTEN_PREVIEW_CFG();

//    private final FPREVIEW_NEWLINK_CB_FILE fPREVIEW_NEWLINK_CB_FILE;

    /**
     * 开启实时预览监听(保存到本地文件)
     */
    public void startRealPlayListen_File(String filename) {
        System.arraycopy(propertiesUtil.readValue("SmsServerListenIP").getBytes(), 0, struPreviewListen.struIPAdress.szIP, 0, propertiesUtil.readValue("SmsServerListenIP").length());
        struPreviewListen.struIPAdress.wPort = Short.parseShort(propertiesUtil.readValue("SmsServerListenPort")); //流媒体服务器监听端口
//        struPreviewListen.fnNewLinkCB = fPREVIEW_NEWLINK_CB_FILE; //预览连接请求回调函数
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
     * 开启实时预览监听(带界面窗口)
     */
    public void startRealPlayListen_Win() {

    }

    /**
     * 设置回放监听（带界面窗口）
     * 启用流媒体服务器（SMS）的回放监听并注册回调函数以接收设备连接请求
     */
    public void startPlayBackListen_WIN() {

    }

    /**
     * 设置回放监听（保存到本地文件）
     * 启用流媒体服务器（SMS）的回放监听并注册回调函数以接收设备连接请求
     */
    public void startPlayBackListen_FILE() {

    }

    /**
     * 按时间回放，ISUP5.0接入支持，需要确认设备上对应时间段是否有录像存储，设备中没有录像文件无法集成此模块
     *
     * @param lchannel 通道号
     */
    public void PlayBackByTime(int lLoginID, int lchannel) {

    }

    /**
     * 停止回放
     */
    public void stopPlayBackByTime(int lLoginID) {

    }
}



