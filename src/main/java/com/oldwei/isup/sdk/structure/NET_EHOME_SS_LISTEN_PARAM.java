package com.oldwei.isup.sdk.structure;

import com.oldwei.isup.sdk.service.EHomeSSMsgCallBack;
import com.oldwei.isup.sdk.service.EHomeSSRWCallBack;
import com.oldwei.isup.sdk.service.EHomeSSRWCallBackEx;
import com.oldwei.isup.sdk.service.EHomeSSStorageCallBack;
import com.sun.jna.Pointer;
import com.oldwei.isup.sdk.HIKSDKStructure;

public class NET_EHOME_SS_LISTEN_PARAM extends HIKSDKStructure {
    public NET_EHOME_IPADDRESS struAddress = new NET_EHOME_IPADDRESS(); //本地监听信息，IP为0.0.0.0的情况下，默认为本地地址，多个网卡的情况下，默认为从操作系统获取到的第一个
    public byte[] szKMS_UserName = new byte[512]; //KMS用户名
    public byte[] szKMS_Password = new byte[512]; //KMS用户名
    public EHomeSSStorageCallBack fnSStorageCb;//图片服务器信息存储回调函数
    public EHomeSSMsgCallBack fnSSMsgCb; //图片服务器信息Tomcat回调函数
    public byte[] szAccessKey = new byte[64]; //EHome5.0存储协议AK
    public byte[] szSecretKey = new byte[64]; //EHome5.0存储协议SK
    public Pointer pUserData; //用户参数
    public byte byHttps; //是否启用HTTPs
    public byte[] byRes1 = new byte[3];
    public EHomeSSRWCallBack fnSSRWCb;//读写回调函数
    public EHomeSSRWCallBackEx fnSSRWCbEx;
    public byte bySecurityMode;
    public byte[] byRes = new byte[51];
}
