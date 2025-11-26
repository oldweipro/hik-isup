package com.oldwei.isup.sdk.structure;

import com.oldwei.isup.sdk.HIKSDKStructure;

public class NET_EHOME_TIME extends HIKSDKStructure {
    public short wYear;//年
    public byte byMonth;//月
    public byte byDay;//日
    public byte byHour;//时
    public byte byMinute;//分
    public byte bySecond;//秒
    public byte byRes1;//保留
    public short wMSecond;//毫秒
    public byte[] byRes2 = new byte[2];
}
