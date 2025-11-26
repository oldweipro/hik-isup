package com.oldwei.isup.sdk.structure;

import com.oldwei.isup.sdk.service.VOICETALK_NEWLINK_CB;
import com.sun.jna.Pointer;
import com.oldwei.isup.sdk.HIKSDKStructure;

public class NET_EHOME_LISTEN_VOICETALK_CFG extends HIKSDKStructure {
    public NET_EHOME_IPADDRESS struIPAdress;       //本地监听信息，IP为0.0.0.0的情况下，默认为本地地址，
    //多个网卡的情况下，默认为从操作系统获取到的第一个
    public VOICETALK_NEWLINK_CB fnNewLinkCB;   //新连接回调函数
    public Pointer pUser;                   //用户参数，在fnNewLinkCB中返回出来
    public byte byLinkMode;     //0：TCP，1：UDP (UDP保留)
    public byte byLinkEncrypt;  //是否启用链路加密,TCP通过TLS传输，UDP(包括NPQ)使用DTLS传输，0-不启用，1-启用
    public byte[] byRes = new byte[126];
}
