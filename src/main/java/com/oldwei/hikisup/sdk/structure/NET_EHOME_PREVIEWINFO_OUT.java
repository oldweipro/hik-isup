package com.oldwei.hikisup.sdk.structure;

import com.sun.jna.Structure;

public class NET_EHOME_PREVIEWINFO_OUT extends Structure {
    public int lSessionID;
    public byte[] byRes = new byte[128];
}