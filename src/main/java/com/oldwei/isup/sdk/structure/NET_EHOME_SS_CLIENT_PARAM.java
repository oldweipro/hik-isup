package com.oldwei.isup.sdk.structure;

import com.sun.jna.Structure;

public class NET_EHOME_SS_CLIENT_PARAM extends Structure {
    public int enumType; //图片上传客户端类型  NET_EHOME_SS_CLIENT_TYPE
    public NET_EHOME_IPADDRESS struAddress; //图片服务器地址
    public byte byHttps;//是否启用HTTPs
    public byte[] byRes = new byte[63];
}