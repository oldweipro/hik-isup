package com.oldwei.isup.sdk.structure;

import com.sun.jna.Structure;

import static com.oldwei.isup.sdk.service.constant.EHOME_ALARM_TYPE.MAX_DEVICE_ID_LEN;

/**
 * 车载客流统计的定位信息
 */
public class NET_EHOME_ALARMWIRELESSINFO extends Structure {
    public byte[] byDeviceID = new byte[MAX_DEVICE_ID_LEN];
    public int dwDataTraffic;
    public byte bySignalIntensity;
    public byte[] byRes = new byte[127];
}
