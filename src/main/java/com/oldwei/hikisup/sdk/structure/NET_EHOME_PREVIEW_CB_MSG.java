package com.oldwei.hikisup.sdk.structure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class NET_EHOME_PREVIEW_CB_MSG extends Structure {
    public byte byDataType;       //NET_DVR_SYSHEAD(1)-码流头，NET_DVR_STREAMDATA(2)-码流数据
    public byte[] byRes1 = new byte[3];
    public Pointer pRecvdata;      //码流头或者数据
    public int dwDataLen;      //数据长度
    public byte[] byRes2 = new byte[128];
}