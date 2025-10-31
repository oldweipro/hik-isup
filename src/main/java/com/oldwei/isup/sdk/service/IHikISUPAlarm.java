package com.oldwei.isup.sdk.service;

import com.oldwei.isup.sdk.structure.NET_EHOME_ALARM_LISTEN_PARAM;
import com.sun.jna.Library;
import com.sun.jna.Pointer;

public interface IHikISUPAlarm extends Library {
    //初始化，反初始化
    boolean NET_EALARM_Init();

    boolean NET_EALARM_Fini();

    boolean NET_EALARM_SetSDKLocalCfg(int enumType, Pointer lpInbuffer);

    int NET_EALARM_StartListen(NET_EHOME_ALARM_LISTEN_PARAM pAlarmListenParam);

    int NET_EALARM_GetLastError();

    boolean NET_EALARM_StopListen(int iListenHandle);

    boolean NET_EALARM_SetDeviceSessionKey(Pointer pDeviceKey);

    boolean NET_EALARM_SetLogToFile(int iLogLevel, String strLogDir, boolean bAutoDel);

    boolean NET_EALARM_SetSDKInitCfg(int enumType, Pointer lpInBuff);
}
