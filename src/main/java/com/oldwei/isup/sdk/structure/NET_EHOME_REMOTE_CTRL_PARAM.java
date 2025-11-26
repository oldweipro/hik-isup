package com.oldwei.isup.sdk.structure;

import com.sun.jna.Pointer;
import com.oldwei.isup.sdk.HIKSDKStructure;

public class NET_EHOME_REMOTE_CTRL_PARAM extends HIKSDKStructure {
    public int dwSize;
    public Pointer lpCondBuffer;        //条件参数缓冲区
    public int dwCondBufferSize;    //条件参数缓冲区长度
    public Pointer lpInbuffer;          //控制参数缓冲区
    public int dwInBufferSize;      //控制参数缓冲区长度
    public byte[] byRes = new byte[32];
}
