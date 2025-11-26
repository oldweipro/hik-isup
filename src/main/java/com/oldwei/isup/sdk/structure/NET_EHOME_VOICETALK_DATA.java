package com.oldwei.isup.sdk.structure;

import com.sun.jna.Pointer;
import com.oldwei.isup.sdk.HIKSDKStructure;

public class NET_EHOME_VOICETALK_DATA extends HIKSDKStructure {
    public Pointer pData;          //数据指针
    public int dwDataLen;         //数据长度
    public byte[] byRes = new byte[128];     //保留
}
