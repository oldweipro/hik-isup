package com.oldwei.isup.sdk.structure;

import com.sun.jna.Structure;

import static com.oldwei.isup.sdk.service.constant.EHOME_ALARM_TYPE.MAX_DEVICE_ID_LEN;
import static com.oldwei.isup.sdk.service.constant.EHOME_ALARM_TYPE.MAX_TIME_LEN;

/**
 * 车载客流统计的定位信息
 */
public class NET_EHOME_ALARM_MPDCDATA extends Structure {
    public byte[] byDeviceID = new byte[MAX_DEVICE_ID_LEN];//设备ID
    public byte[] bySampleTime = new byte[MAX_TIME_LEN]; //GPS采样时间，格式：YYYY-MM-DD HH:MM:SS
    public byte byTimeZoneIdx;  //时区
    public byte byRetranseFlag;  //重传标记, 0-实时包, 1-重传包
    public byte[] byRes = new byte[2];
    public NET_EHOME_MPGPS struGpsInfo;  //GPS信息
    public NET_EHOME_MPDATA struMPData;
}
