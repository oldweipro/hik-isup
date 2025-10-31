package com.oldwei.isup.sdk.structure;

import com.sun.jna.Structure;

public class NET_EHOME_CHAN_STATUS_CHANGED extends Structure {
    public short wChanNO; // 通道号
    public byte byChanStatus; //通道状态，按位表示
    //bit0：启用状态，0-禁用/删除，1-启用/添加
    //模拟通道由禁用到启用，或者启用到禁用，上报该字段
    //数字通道添加到删除，或者删除到重新添加，上报该字段
    //bit1：在线状态，0-不在线，1-在线
    //bit2：信号状态，0-无信号，1-有信号
    //bit3：录像状态，0-不在录像 1-在录像
    //bit4：IP通道信息改变状态，0-未改变 1-有改变，这位表示该通道的配
    //置信息发生了改变，比如添加的IPC有过更换，通知上层更新能力集

    public byte[] byRes = new byte[9];
}
