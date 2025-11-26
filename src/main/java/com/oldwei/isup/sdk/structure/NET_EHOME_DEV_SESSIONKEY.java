package com.oldwei.isup.sdk.structure;

import com.oldwei.isup.sdk.HIKSDKStructure;

public class NET_EHOME_DEV_SESSIONKEY extends HIKSDKStructure {
    public byte[] sDeviceID = new byte[256];        //设备ID/*256*/
    public byte[] sSessionKey = new byte[16];     //设备Sessionkey/*16*/

}
