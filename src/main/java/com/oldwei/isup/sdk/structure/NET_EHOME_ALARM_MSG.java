package com.oldwei.isup.sdk.structure;

import com.sun.jna.Pointer;
import com.oldwei.isup.sdk.HIKSDKStructure;

public class NET_EHOME_ALARM_MSG extends HIKSDKStructure {
    public int dwAlarmType;      //报警类型，见EN_ALARM_TYPE
    public Pointer pAlarmInfo;       //报警内容（结构体）
    public int dwAlarmInfoLen;   //结构体报警内容长度
    public Pointer pXmlBuf;          //报警内容（XML）
    public int dwXmlBufLen;      //xml报警内容长度
    public byte[] sSerialNumber = new byte[12]; //设备序列号，用于进行Token认证
    public Pointer pHttpUrl;
    public int dwHttpUrlLen;
    public byte[] byRes = new byte[12];
}
