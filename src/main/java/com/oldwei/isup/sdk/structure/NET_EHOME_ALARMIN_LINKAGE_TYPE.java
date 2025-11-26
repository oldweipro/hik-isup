package com.oldwei.isup.sdk.structure;

import com.oldwei.isup.sdk.HIKSDKStructure;

public class NET_EHOME_ALARMIN_LINKAGE_TYPE extends HIKSDKStructure {
    public byte byMonitorAlarm;     //监视器上警告，0：不使用；1：使用
    public byte bySoundAlarm;       //声音报警，0：不使用；1：使用
    public byte byUpload;           //上传中心，0：不使用；1：使用
    public byte byAlarmout;         //触发报警输出，0：不使用；1：使用-
    public byte byEmail;            //邮件联动，0：不使用；1：使用
    public byte[] byRes1 = new byte[3];          //保留
    public NET_EHOME_LINKAGE_PTZ struPTZLinkage = new NET_EHOME_LINKAGE_PTZ();    //PTZ联动
    public NET_EHOME_LINKAGE_ALARMOUT struAlarmOut = new NET_EHOME_LINKAGE_ALARMOUT();    //报警输出联动
    public byte[] byRes = new byte[128];
}
