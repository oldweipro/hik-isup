package com.oldwei.isup.sdk.structure;

import com.oldwei.isup.sdk.HIKSDKStructure;

public class NET_EHOME_ALARM_STATUS_UNION extends HIKSDKStructure {
    public byte[] byRes = new byte[12]; // 联合体大小
    public NET_EHOME_DEV_STATUS_CHANGED struDevStatusChanged;
    public NET_EHOME_CHAN_STATUS_CHANGED struChanStatusChanged;
    public NET_EHOME_HD_STATUS_CHANGED struHdStatusChanged;
    public NET_EHOME_DEV_TIMING_STATUS struDevTimeStatus;
    public NET_EHOME_CHAN_TIMING_STATUS_SINGLE struChanTimeStatus;
    public NET_EHOME_HD_TIMING_STATUS_SINGLE struHdTimeStatus;
}