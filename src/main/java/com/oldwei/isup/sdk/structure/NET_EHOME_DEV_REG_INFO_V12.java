package com.oldwei.isup.sdk.structure;

import com.sun.jna.Structure;

public class NET_EHOME_DEV_REG_INFO_V12 extends Structure {
    public NET_EHOME_DEV_REG_INFO struRegInfo;
    public NET_EHOME_IPADDRESS struRegAddr;
    public byte[] sDevName = new byte[64]; //设备名最大长度
    public byte[] byDeviceFullSerial = new byte[64]; //最大完整序列号长度
    public byte[] byRes = new byte[128];

}