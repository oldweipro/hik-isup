package com.oldwei.isup.sdk.structure;

import com.oldwei.isup.sdk.HIKSDKStructure;

public class NET_EHOME_SS_TOMCAT_MSG extends HIKSDKStructure {
    public byte[] szDevUri = new byte[4096]; //设备请求的URI
    public int dwPicNum; //图片数
    public String pPicURLs; //图片URL,每个URL MAX_URL_LEN_SS长度
    public byte[] byRes = new byte[64];
}
