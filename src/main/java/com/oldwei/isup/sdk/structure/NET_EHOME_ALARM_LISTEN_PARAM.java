package com.oldwei.isup.sdk.structure;

import com.oldwei.isup.sdk.service.EHomeMsgCallBack;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class NET_EHOME_ALARM_LISTEN_PARAM extends Structure {
    public NET_EHOME_IPADDRESS struAddress;
    public EHomeMsgCallBack fnMsgCb; //报警信息回调函数
    public Pointer pUserData;   //用户数据
    public byte byProtocolType;    //协议类型，0-TCP,1-UDP
    public byte byUseCmsPort; //是否复用CMS端口,0-不复用，非0-复用，如果复用cms端口，协议类型字段无效（此时本地监听信息struAddress填本地回环地址）
    public byte byUseThreadPool;  //0-回调报警时，使用线程池，1-回调报警时，不使用线程池，默认情况下，报警回调的时候，使用线程池
    public byte byRes[] = new byte[29];
}