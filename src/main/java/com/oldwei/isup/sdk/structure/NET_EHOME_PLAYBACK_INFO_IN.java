package com.oldwei.isup.sdk.structure;

import com.oldwei.isup.sdk.HIKSDKStructure;

public class NET_EHOME_PLAYBACK_INFO_IN extends HIKSDKStructure {
    public int dwSize;
    public int dwChannel;                    //回放的通道号
    public byte byPlayBackMode;               //回放下载模式 0－按名字 1－按时间
    public byte byStreamPackage;              //回放码流类型，设备端发出的码流格式 0－PS（默认） 1－RTP
    public byte[] byRes = new byte[2];
    public NET_EHOME_PLAYBACKMODE unionPlayBackMode;
    public NET_EHOME_IPADDRESS struStreamSever;
}
