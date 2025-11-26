package com.oldwei.isup.sdk.structure;

import com.oldwei.isup.sdk.HIKSDKStructure;

public class NET_EHOME_CHAN_TIMING_STATUS_SINGLE extends HIKSDKStructure {
    public int dwBitRate; // 实际码率，单位kbps
    public short wChanNO; // 通道号
    public byte byLinkNum; // 客户端连接的个数
    public byte[] byRes = new byte[5];
}
