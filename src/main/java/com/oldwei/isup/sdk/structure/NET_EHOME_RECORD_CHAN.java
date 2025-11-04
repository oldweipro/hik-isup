package com.oldwei.isup.sdk.structure;

import com.sun.jna.Structure;

public class NET_EHOME_RECORD_CHAN extends Structure {
    public byte byAnalogChanNum;                    //只读，模拟通道数
    public byte[] byAnalogChan = new byte[32];   //模拟通道，0：不使用；1：使用
    public byte[] byRes1 = new byte[3];                          //保留
    public short wDigitChanNum;                      //只读，数字通道数
    public byte[] byDigitChan = new byte[480];     //数字通道，0：不使用；1：使用
    public byte[] byRes2 = new byte[62];
}
