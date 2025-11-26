package com.oldwei.isup.sdk.structure;

import com.oldwei.isup.sdk.service.VOICETALK_DATA_CB;
import com.sun.jna.Pointer;
import com.oldwei.isup.sdk.HIKSDKStructure;

public class NET_EHOME_VOICETALK_DATA_CB_PARAM extends HIKSDKStructure {
    public VOICETALK_DATA_CB fnVoiceTalkDataCB;  //数据回调函数
    public Pointer pUserData;  //用户参数, 在fnVoiceTalkDataCB回调出来
    public byte[] byRes = new byte[128]; //保留
}

