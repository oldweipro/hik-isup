package com.oldwei.hikisup.sdk.structure;

import com.oldwei.hikisup.sdk.service.DEVICE_REGISTER_CB;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class NET_EHOME_CMS_LISTEN_PARAM extends Structure {
    public NET_EHOME_IPADDRESS struAddress;  //本地监听信息，IP为0.0.0.0的情况下，默认为本地地址，多个网卡的情况下，默认为从操作系统获取到的第一个
    public DEVICE_REGISTER_CB fnCB; //报警信息回调函数
    public Pointer pUserData;   //用户数据
    public byte[] byRes = new byte[32];
}