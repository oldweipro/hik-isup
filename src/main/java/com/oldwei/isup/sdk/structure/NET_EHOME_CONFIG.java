package com.oldwei.isup.sdk.structure;

import com.sun.jna.Pointer;
import com.oldwei.isup.sdk.HIKSDKStructure;

public class NET_EHOME_CONFIG extends HIKSDKStructure {
    public Pointer pCondBuf;
    public int dwCondSize;
    public Pointer pInBuf;
    public int dwInSize;
    public Pointer pOutBuf;
    public int dwOutSize;
    public byte[] byRes = new byte[40];
}
