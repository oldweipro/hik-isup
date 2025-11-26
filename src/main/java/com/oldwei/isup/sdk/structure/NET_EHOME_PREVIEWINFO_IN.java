package com.oldwei.isup.sdk.structure;

import com.oldwei.isup.sdk.HIKSDKStructure;

public class NET_EHOME_PREVIEWINFO_IN extends HIKSDKStructure {
    public int iChannel;                        //通道号
    public int dwStreamType;                    // 码流类型，0-主码流，1-子码流, 2-第三码流
    public int dwLinkMode;                        // 0：TCP方式,1：UDP方式,2: HRUDP方式
    public NET_EHOME_IPADDRESS struStreamSever;     //流媒体地址
}