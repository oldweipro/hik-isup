package com.oldwei.hikisup.sdk.structure;

import com.sun.jna.Structure;

public class NET_EHOME_IPADDRESS extends Structure {
    public byte[] szIP = new byte[128];
    public short wPort;     //端口
    public byte[] byRes = new byte[2];
}