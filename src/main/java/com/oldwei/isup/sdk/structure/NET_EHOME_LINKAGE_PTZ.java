package com.oldwei.isup.sdk.structure;

import com.sun.jna.Structure;

public class NET_EHOME_LINKAGE_PTZ extends Structure {
    public byte byUsePreset;    //是否调用预置点，0：不使用；1：使用
    public byte byUseCurise;    //是否调用巡航，0：不使用；1：使用
    public byte byUseTrack;     //是否调用轨迹，0：不使用；1：使用
    public byte byRes1;         //保留
    public short wPresetNo;      //预置点号，范围：1~256，协议中规定是1～256，实际已有设备支持300
    public short wCuriseNo;      //巡航路径号，范围：1~16
    public short wTrackNo;       //轨迹号，范围：1~16
    public byte[] byRes2 = new byte[6];      //保留
}
