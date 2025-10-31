package com.oldwei.isup.sdk.structure;

import com.sun.jna.Structure;

public class NET_EHOME_BLACKLIST_SEVER extends Structure {
    public NET_EHOME_IPADDRESS struAdd = new NET_EHOME_IPADDRESS();
    public byte[] byServerName = new byte[32];
    public byte[] byUserName = new byte[32];
    public byte[] byPassWord = new byte[32];
    public byte[] byRes = new byte[64];
}
