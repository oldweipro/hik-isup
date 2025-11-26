package com.oldwei.isup.sdk.structure;

import com.oldwei.isup.sdk.HIKSDKStructure;

public class NET_EHOME_PUSHPLAYBACK_IN extends HIKSDKStructure {
    public int dwSize;
    public int lSessionID;
    public byte[] byKeyMD5 = new byte[32];//码流加密秘钥,两次MD5
    public byte[] byRes = new byte[96];
}
