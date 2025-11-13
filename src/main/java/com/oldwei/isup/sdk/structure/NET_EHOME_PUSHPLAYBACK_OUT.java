package com.oldwei.isup.sdk.structure;

import com.sun.jna.Structure;

public class NET_EHOME_PUSHPLAYBACK_OUT extends Structure {
    public int dwSize;
    public int lHandle;
    public byte[] byRes = new byte[124];
}
