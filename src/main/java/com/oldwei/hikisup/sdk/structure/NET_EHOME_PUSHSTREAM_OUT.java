package com.oldwei.hikisup.sdk.structure;

import com.sun.jna.Structure;

public class NET_EHOME_PUSHSTREAM_OUT extends Structure {
    public int dwSize;
    public byte[] byRes = new byte[128];
}