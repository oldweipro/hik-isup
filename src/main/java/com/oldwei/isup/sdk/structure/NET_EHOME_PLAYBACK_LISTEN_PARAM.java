package com.oldwei.isup.sdk.structure;

import com.oldwei.isup.sdk.service.PLAYBACK_NEWLINK_CB;
import com.sun.jna.Pointer;
import com.oldwei.isup.sdk.HIKSDKStructure;

public class NET_EHOME_PLAYBACK_LISTEN_PARAM extends HIKSDKStructure {
    public NET_EHOME_IPADDRESS struIPAdress; //本地监听信息，IP为0.0.0.0的情况下，默认为本地地址，多个网卡的情况下，默认为从操作系统获取到的第一个
    public PLAYBACK_NEWLINK_CB fnNewLinkCB; //预览请求回调函数，当收到预览连接请求后，SDK会回调该回调函数。
    public Pointer pUser;        // 用户参数，在fnNewLinkCB中返回出来
    public byte byLinkMode;   //0：TCP，1：UDP 2: HRUDP方式
    public byte[] byRes = new byte[127];
}
