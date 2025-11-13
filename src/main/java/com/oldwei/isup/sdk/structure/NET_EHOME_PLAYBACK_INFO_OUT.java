package com.oldwei.isup.sdk.structure;

import com.sun.jna.Structure;

public class NET_EHOME_PLAYBACK_INFO_OUT extends Structure {
    public int lSessionID;     //目前协议不支持，返回-1
    public int lHandle;  //设置了回放异步回调之后，该值为消息句柄，回调中用于标识
    public byte[] byRes = new byte[124];
}
