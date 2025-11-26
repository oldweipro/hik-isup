package com.oldwei.isup.sdk.structure;

import com.oldwei.isup.sdk.HIKSDKStructure;

public class NET_EHOME_VOICE_TALK_OUT extends HIKSDKStructure {
    /**
     * 语音对讲请求的会话ID，由设备返回
     */
    public int lSessionID;
    /**
     * 输出参数句柄，在异步模式中作为异步回调的
     * 标识。对应 NET_EHOME_CMSCB_DATA 中的
     * dwHandle。
     */
    public int lHandle;
    /**
     * 保留，设为0。最大长度为124 字节。
     */
    public byte[] byRes = new byte[124];
}
