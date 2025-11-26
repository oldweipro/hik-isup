package com.oldwei.isup.sdk.structure;

import com.oldwei.isup.sdk.HIKSDKStructure;

import static com.oldwei.isup.sdk.service.constant.EHOME_ALARM_TYPE.*;

public class NET_EHOME_ALARM_INFO extends HIKSDKStructure {
    public int dwSize;
    public byte[] szAlarmTime = new byte[MAX_TIME_LEN];  //报警触发时间（设备本地时间），格式，YYYY-MM-DD HH:MM:SS
    public byte[] szDeviceID = new byte[MAX_DEVICE_ID_LEN]; //设备注册ID
    public int dwAlarmType;  //报警类型见EN_ALARM_TYPE枚举变量
    public int dwAlarmAction; //报警动作0:开始    1:停止
    public int dwVideoChannel;//各报警中的意义见注释
    public int dwAlarmInChannel;//各报警中的意义见注释
    public int dwDiskNumber;  //各报警中的意义见注释
    public byte[] byRemark = new byte[MAX_REMARK_LEN];  //重传标记，0-实时包，1-重传包
    public byte byRetransFlag;  //重传标记，0-实时包，1-重传包
    public byte byTimeDiffH;  //重传标记，0-实时包，1-重传包
    public byte byTimeDiffM;//szAlarmTime，szAlarmUploadTime与国际标准时间（UTC）的时差（小时），-12 ... +14,0xff表示无效
    public byte byRes1;
    public byte[] szAlarmUploadTime = new byte[MAX_TIME_LEN];   //报警上传时间（设备本地时间），时间格式，YYYY-MM-DD HH:MM:SS
    public NET_EHOME_ALARM_STATUS_UNION uStatusUnion;
    public byte[] byRes2 = new byte[16];
}
