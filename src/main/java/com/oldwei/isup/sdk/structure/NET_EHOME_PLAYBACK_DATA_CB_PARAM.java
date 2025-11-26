package com.oldwei.isup.sdk.structure;

import com.oldwei.isup.sdk.service.PLAYBACK_DATA_CB;
import com.sun.jna.Pointer;
import com.oldwei.isup.sdk.HIKSDKStructure;

public class NET_EHOME_PLAYBACK_DATA_CB_PARAM extends HIKSDKStructure {
    public PLAYBACK_DATA_CB fnPlayBackDataCB;   //数据回调函数
    public Pointer pUserData;          //用户参数, 在fnPlayBackDataCB回调出来
    public byte byStreamFormat;     //码流封装格式：0-PS 1-RTP
    public byte[] byRes = new byte[127];         //保留
}
