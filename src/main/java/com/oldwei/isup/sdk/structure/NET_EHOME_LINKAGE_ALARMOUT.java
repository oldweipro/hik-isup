package com.oldwei.isup.sdk.structure;

import com.oldwei.isup.sdk.HIKSDKStructure;

public class NET_EHOME_LINKAGE_ALARMOUT extends HIKSDKStructure {
    public int dwAnalogAlarmOutNum;                    //只读，模拟报警数量
    public byte[] byAnalogAlarmOut = new byte[32];  //模拟报警输出，0：不使用；1：使用
    public byte[] byRes = new byte[5000];
}
