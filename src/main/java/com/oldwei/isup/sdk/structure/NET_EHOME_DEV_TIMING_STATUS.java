package com.oldwei.isup.sdk.structure;

import com.oldwei.isup.sdk.HIKSDKStructure;

public class NET_EHOME_DEV_TIMING_STATUS extends HIKSDKStructure {
    public int dwMemoryTotal; // 内存总量，单位Kbyte
    public int dwMemoryUsage; // 内存使用量，单位Kbyte
    public byte byCPUUsage;   // CPU使用率，0-100
    public byte byMainFrameTemp; // 机箱温度，单位：摄氏度
    public byte byBackPanelTemp; // 背板温度，单位：摄氏度
    public byte byRes;
}
