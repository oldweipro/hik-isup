package com.oldwei.isup.sdk.structure;

import com.sun.jna.Structure;

public class NET_EHOME_SS_EX_PARAM extends Structure {
    public byte byProtoType;
    public byte[] byRes = new byte[23];
    public NET_EHOME_SS_Union unionStoreInfo = new NET_EHOME_SS_Union();
}
