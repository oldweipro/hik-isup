package com.oldwei.isup.sdk.structure;

import com.sun.jna.Structure;

public class NET_EHOME_SS_CLOUD_PARAM extends Structure {
    public String pPoolId;
    public byte byPoolIdLength;
    public int dwErrorCode;
    public byte[] byRes = new byte[503];
}
