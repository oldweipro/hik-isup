package com.oldwei.isup.sdk.structure;

import com.sun.jna.Structure;

public class NET_EHOME_SERVER_INFO_V50 extends Structure {
    public int dwSize;
    public int dwKeepAliveSec;         //心跳间隔（单位：秒,0:默认为15S）
    public int dwTimeOutCount;         //心跳超时次数（0：默认为6）
    public NET_EHOME_IPADDRESS struTCPAlarmSever = new NET_EHOME_IPADDRESS();      //报警服务器地址（TCP协议）
    public NET_EHOME_IPADDRESS struUDPAlarmSever = new NET_EHOME_IPADDRESS();      //报警服务器地址（UDP协议）
    public int dwAlarmServerType;      //报警服务器类型0-只支持UDP协议上报，1-支持UDP、TCP两种协议上报
    public NET_EHOME_IPADDRESS struNTPSever = new NET_EHOME_IPADDRESS();           //NTP服务器地址
    public int dwNTPInterval;          //NTP校时间隔（单位：秒）
    public NET_EHOME_IPADDRESS struPictureSever = new NET_EHOME_IPADDRESS();       //图片服务器地址
    public int dwPicServerType;        //图片服务器类型图片服务器类型，1-VRB图片服务器，0-Tomcat图片服务,2-云存储3,3-KMS
    public NET_EHOME_BLACKLIST_SEVER struBlackListServer = new NET_EHOME_BLACKLIST_SEVER();//黑名单服务器
    public NET_EHOME_IPADDRESS struRedirectSever = new NET_EHOME_IPADDRESS();      //Redirect Server
    public byte[] byClouldAccessKey = new byte[64];  //云存储AK
    public byte[] byClouldSecretKey = new byte[64];  //云存储SK
    public byte byClouldHttps;          //云存储HTTPS使能 1-HTTPS 0-HTTP
    public byte[] byRes1 = new byte[3];
    public int dwAlarmKeepAliveSec;    //报警心跳间隔（单位：秒,0:默认为30s）
    public int dwAlarmTimeOutCount;    //报警心跳超时次数（0：默认为3）
    public int dwClouldPoolId;         //云存储PoolId
    public byte[] byRes = new byte[368];
}
