package com.oldwei.isup.sdk.structure;

import com.sun.jna.Structure;

public class NET_EHOME_ALARMIN_COND extends Structure {
    public int dwSize;
    public int dwAlarmInNum;
    public int dwPTZChan;
    public byte[] byRes = new byte[20];
}
