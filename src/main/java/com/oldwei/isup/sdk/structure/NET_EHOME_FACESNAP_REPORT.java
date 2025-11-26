package com.oldwei.isup.sdk.structure;

import com.oldwei.isup.sdk.HIKSDKStructure;

import static com.oldwei.isup.sdk.service.constant.EHOME_ALARM_TYPE.*;

public class NET_EHOME_FACESNAP_REPORT extends HIKSDKStructure {
    public int dwSize;
    public byte[] byDeviceID = new byte[MAX_DEVICE_ID_LEN];   //设备注册ID
    public int dwVideoChannel;                  //通道号
    public byte[] byAlarmTime = new byte[MAX_TIME_LEN];       //报警时间（设备本地时间），格式：YYYY-MM-DD HH:MM:SS
    public int dwFacePicID;                     //人脸图ID
    public int dwFaceScore;                     //人脸评分，0-100
    public int dwTargetID;                      //目标ID
    public NET_EHOME_ZONE struTarketZone;        //目标区域，归一化坐标：数值为当前画面的百分比大小*1000，精度为小数点后三位
    public NET_EHOME_ZONE struFacePicZone;         //人脸子图区域，归一化坐标：数值为当前画面的百分比大小*1000，精度为小数点后三位
    public NET_EHOME_HUMAN_FEATURE struHumanFeature;//人属性
    public int dwStayDuration;                 //停留画面中时间
    public int dwFacePicLen;                   //人脸图长度，单位：字节
    public byte[] byFacePicUrl = new byte[MAX_URL_LEN];       //人脸子图数据索引
    public int dwBackgroudPicLen;              //背景图片长度，单位：字节
    public byte[] byBackgroudPicUrl = new byte[MAX_URL_LEN];  //背景图片数据索引
    public byte byRetransFlag;                   //重传标记，0-实时包，1-重传包
    public byte byTimeDiffH;  //byAlamTime与国际标准时间（UTC）的时差（小时），-12 ... +14,0xff表示无效
    public byte byTimeDiffM;  //byAlamTime与国际标准时间（UTC）的时差（分钟），-30,0, 30, 45, 0xff表示无效
    public byte[] byRes = new byte[61];
}
