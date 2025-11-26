package com.oldwei.isup.sdk.structure;

import com.oldwei.isup.sdk.HIKSDKStructure;

public class NET_EHOME_SS_EX_PARAM extends HIKSDKStructure {
    public byte byProtoType;
    public byte[] byRes = new byte[23];
    public NET_EHOME_SS_Union unionStoreInfo = new NET_EHOME_SS_Union();
}
