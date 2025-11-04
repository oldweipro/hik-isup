package com.oldwei.isup.sdk.structure;

import com.sun.jna.Structure;

import static com.oldwei.isup.sdk.service.constant.EHOME_ALARM_TYPE.NAME_LEN;

public class NET_EHOME_ALARMIN_CFG extends Structure {
    public int dwSize;                     //结构体大小
    public byte[] sAlarmInName = new byte[NAME_LEN];     //报警输入名称
    public byte byAlarmInType;              //报警器类型：0：常开；1：常闭
    public byte byUseAlarmIn;               //是否处理，0：不使用；1：使用
    public byte[] byRes1 = new byte[2];                  //保留
    public NET_EHOME_ALARMIN_LINKAGE_TYPE struLinkageType = new NET_EHOME_ALARMIN_LINKAGE_TYPE();    //联动模式
    public NET_EHOME_RECORD_CHAN struRecordChan = new NET_EHOME_RECORD_CHAN();//关联录像通道
    public byte[] byRes2 = new byte[128];                //保留
}
