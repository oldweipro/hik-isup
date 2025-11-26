package com.oldwei.isup.sdk.structure;

import com.oldwei.isup.sdk.HIKSDKStructure;

public class NET_EHOME_HD_STATUS_CHANGED extends HIKSDKStructure {
    public int dwVolume; //硬盘容量，单位：MB
    public short wHDNo; //硬盘号
    public byte byHDStatus; //硬盘的状态, 0-活动1-休眠,2-异常,3-休眠硬盘出错,4-未格式化, 5-未连接状态(网络硬盘),6-硬盘正在格式化
    public byte[] byRes = new byte[5]; //保留
}
