package com.oldwei.isup.sdk.structure;

import com.oldwei.isup.sdk.HIKSDKStructure;

public class NET_EHOME_HUMAN_FEATURE extends HIKSDKStructure {
    public byte byAgeGroup;    //年龄属性，1-婴幼儿，2-儿童，3-少年，4-青少年，5-青年，6-壮年，7-中年，8-中老年，9-老年
    public byte bySex;            //性别属性，1-男，2-女
    public byte byEyeGlass;    //是否戴眼睛，1-不戴，2-戴
    public byte byMask;        //是否戴口罩，1-不戴，2-戴
    public byte[] byRes = new byte[12];
}
