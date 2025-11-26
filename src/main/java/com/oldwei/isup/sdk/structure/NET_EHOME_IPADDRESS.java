package com.oldwei.isup.sdk.structure;

import com.oldwei.isup.sdk.HIKSDKStructure;

public class NET_EHOME_IPADDRESS extends HIKSDKStructure {
    public byte[] szIP = new byte[128];
    public short wPort;     //端口
    public byte[] byRes = new byte[2];
}