package com.oldwei.hikisup.sdk.service;

import com.oldwei.hikisup.sdk.SdkService.CmsService.HCISUPCMS;
import com.oldwei.hikisup.sdk.structure.NET_EHOME_CMS_LISTEN_PARAM;
import com.oldwei.hikisup.sdk.structure.NET_EHOME_PREVIEWINFO_OUT;
import com.oldwei.hikisup.sdk.structure.NET_EHOME_PUSHSTREAM_IN;
import com.sun.jna.Library;
import com.sun.jna.Pointer;

public interface IHCISUPCMS extends Library {
    //初始化，反初始化
    boolean NET_ECMS_Init();

    boolean NET_ECMS_Fini();

    boolean NET_ECMS_SetSDKInitCfg(int enumType, Pointer lpInBuff);

    boolean NET_ECMS_SetSDKLocalCfg(int enumType, Pointer lpInBuff);
    boolean NET_ECMS_SetLogToFile(int iLogLevel, String strLogDir, boolean bAutoDel);
    boolean NET_ECMS_SetDeviceSessionKey(Pointer pDeviceKey);
    boolean  NET_ECMS_StartGetRealStream(int lUserID, HCISUPCMS.NET_EHOME_PREVIEWINFO_IN pPreviewInfoIn, NET_EHOME_PREVIEWINFO_OUT pPreviewInfoOut ); //lUserID由SDK分配的用户ID，由设备注册回调时fDeviceRegisterCallBack返回
    boolean  NET_ECMS_StartGetRealStreamV11(int lUserID, HCISUPCMS.NET_EHOME_PREVIEWINFO_IN_V11 pPreviewInfoIn, NET_EHOME_PREVIEWINFO_OUT NET_EHOME_PREVIEWINFO_OUT ); //lUserID由SDK分配的用户ID，由设备注册回调时fDeviceRegisterCallBack返回
    boolean NET_ECMS_StartPushRealStream(int lUserID, NET_EHOME_PUSHSTREAM_IN pPushInfoIn, HCISUPCMS.NET_EHOME_PUSHSTREAM_OUT pPushInfoOut);
    boolean NET_ECMS_XMLRemoteControl(int lUserID, HCISUPCMS.NET_EHOME_XML_REMOTE_CTRL_PARAM lpCtrlParam, int dwCtrlSize);
    //开启关闭监听
    int NET_ECMS_StartListen(NET_EHOME_CMS_LISTEN_PARAM lpCMSListenPara);
    boolean  NET_ECMS_StopGetRealStream(int lUserID, int lSessionID);
    //获取错误码
    int NET_ECMS_GetLastError();
}
