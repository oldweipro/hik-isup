package com.oldwei.isup.sdk.structure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class NET_EHOME_PLAYBACK_DATA_CB_INFO extends Structure {
    public int dwType;     //类型 1-头信息 2-码流数据 3-回放停止信令
    public Pointer pData;      //数据指针
    public int dwDataLen;  //数据长度
    public byte[] byRes = new byte[128]; //保留
}
