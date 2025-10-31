package com.oldwei.isup.sdk.structure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import static com.oldwei.isup.sdk.service.constant.EHOME_ALARM_TYPE.*;

public class NET_EHOME_CID_INFO extends Structure {
    public int dwSize;
    public byte[] byDeviceID = new byte[MAX_DEVICE_ID_LEN];//设备注册ID
    public int dwCIDCode; //CID报告代码
    public int dwCIDType; //CID报警类型
    public int dwSubSysNo; //产生报告的子系统号，0为全局报告，子系统范围0~32
    public byte[] byCIDDescribe = new byte[CID_DES_LEN]; //CID报警描述
    public byte[] byTriggerTime = new byte[MAX_TIME_LEN];  //CID报警发生时间（设备本地时间），格式：YYYY-MM-DD HH:MM:SS
    public byte[] byUploadTime = new byte[MAX_TIME_LEN];//CID报告上传时间（设备本地时间），格式：YYYY-MM-DD HH:MM:SS
    public NET_EHOME_CID_PARAM struCIDParam; //CID报警参数
    public byte byTimeDiffH;//byTriggerTime，byUploadTime与国际标准时间（UTC）的时差（小时），-12 ... +14,0xff表示无效
    public byte byTimeDiffM;//byTriggerTime，byUploadTime与国际标准时间（UTC）的时差（分钟），-30,0, 30, 45, 0xff表示无效
    public byte byExtend;//是否有扩展字段
    public byte[] byRes1 = new byte[5];
    public Pointer pCidInfoEx; //byExtend为1是有效，指向NET_EHOME_CID_INFO_INTERNAL_EX结构体
    public Pointer pPicInfoEx;
    public byte[] byRes = new byte[44];
}
