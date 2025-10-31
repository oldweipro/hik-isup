package com.oldwei.isup.sdk.structure;

import com.sun.jna.Structure;

public class NET_EHOME_DEV_STATUS_CHANGED extends Structure {
    public byte byDeviceStatus;
    public byte[] byRes = new byte[11];
}
