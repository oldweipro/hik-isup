package com.oldwei.isup.sdk.structure;

import com.oldwei.isup.sdk.HIKSDKStructure;

public class NET_EHOME_SS_CLIENT_PARAM extends HIKSDKStructure {
    public int enumType; //图片上传客户端类型  NET_EHOME_SS_CLIENT_TYPE
    public NET_EHOME_IPADDRESS struAddress; //图片服务器地址
    public byte byHttps;//是否启用HTTPs
    public byte[] byRes = new byte[63];
}