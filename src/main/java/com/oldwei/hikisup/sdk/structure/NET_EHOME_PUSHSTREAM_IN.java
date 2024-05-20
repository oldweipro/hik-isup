package com.oldwei.hikisup.sdk.structure;

import com.sun.jna.Structure;

public class NET_EHOME_PUSHSTREAM_IN extends Structure {
    public int dwSize;
    public int lSessionID;
    public byte[] byRes = new byte[128];
}