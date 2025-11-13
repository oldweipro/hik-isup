package com.oldwei.isup.sdk.structure;

import com.sun.jna.Structure;

public class NET_EHOME_PUSHVOICE_IN extends Structure {
    public int dwSize;
    public int lSessionID;
    public byte[] byToken = new byte[64];
    public byte[] byRes = new byte[64];
}
