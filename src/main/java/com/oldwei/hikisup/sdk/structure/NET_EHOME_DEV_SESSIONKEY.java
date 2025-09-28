package com.oldwei.hikisup.sdk.structure;

import com.sun.jna.Structure;

public class NET_EHOME_DEV_SESSIONKEY extends Structure {
    public byte[] sDeviceID = new byte[256];        //设备ID/*256*/
    public byte[] sSessionKey = new byte[16];     //设备Sessionkey/*16*/

}
