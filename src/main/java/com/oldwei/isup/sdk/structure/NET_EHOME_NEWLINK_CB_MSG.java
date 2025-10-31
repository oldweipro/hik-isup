package com.oldwei.isup.sdk.structure;

import com.sun.jna.Structure;

public class NET_EHOME_NEWLINK_CB_MSG extends Structure {
    public byte[] szDeviceID = new byte[256];   //设备标示符
    public int iSessionID;     //设备分配给该取流会话的ID
    public int dwChannelNo;    //设备通道号
    public byte byStreamType;   //0-主码流，1-子码流
    public byte[] byRes1 = new byte[3];
    public byte[] sDeviceSerial = new byte[12];    //设备序列号，数字序列号
    public byte[] byRes = new byte[112];
}