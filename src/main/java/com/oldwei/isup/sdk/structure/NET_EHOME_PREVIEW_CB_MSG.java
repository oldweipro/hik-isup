package com.oldwei.isup.sdk.structure;

import com.sun.jna.Pointer;
import com.oldwei.isup.sdk.HIKSDKStructure;

public class NET_EHOME_PREVIEW_CB_MSG extends HIKSDKStructure {
    /**
     * NET_DVR_SYSHEAD(1)-码流头，NET_DVR_STREAMDATA(2)-码流数据
     */
    public byte byDataType;
    public byte[] byRes1 = new byte[3];
    /**
     * 码流头或者数据
     */
    public Pointer pRecvdata;
    /**
     * 数据长度
     */
    public int dwDataLen;
    public byte[] byRes2 = new byte[128];
}