package com.oldwei.isup.sdk.structure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class NET_EHOME_VOICETALK_DATA_CB_INFO extends Structure {
    public Pointer pData;          //数据指针
    public int dwDataLen;      //数据长度
    public byte[] byRes = new byte[128];     //保留
}
