package com.oldwei.isup.sdk.structure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class NET_EHOME_CONFIG extends Structure {
    public Pointer pCondBuf;
    public int dwCondSize;
    public Pointer pInBuf;
    public int dwInSize;
    public Pointer pOutBuf;
    public int dwOutSize;
    public byte[] byRes = new byte[40];
}
