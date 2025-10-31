package com.oldwei.isup.sdk.structure;

import com.sun.jna.Structure;

import static com.oldwei.isup.sdk.service.constant.EHOME_ALARM_TYPE.MAX_DEVICE_ID_LEN;

public class NET_EHOME_NOTIFY_FAIL_INFO extends Structure {
    public int dwSize;
    public byte[] byDeviceID = new byte[MAX_DEVICE_ID_LEN];
    public short wFailedCommand;
    public short wPicType;
    public int dwManualSnapSeq;
    public byte byRetransFlag;
    public byte[] byRes = new byte[31];
}
