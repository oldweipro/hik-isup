package com.oldwei.isup.sdk.structure;

import com.oldwei.isup.sdk.HIKSDKStructure;

public class NET_EHOME_PUSHVOICE_IN extends HIKSDKStructure {
    public int dwSize;
    public int lSessionID;
    public byte[] byToken = new byte[64];
    public byte[] byRes = new byte[64];
}
