package com.oldwei.isup.sdk.structure;

import com.sun.jna.Structure;

import static com.oldwei.isup.sdk.service.constant.EHOME_ALARM_TYPE.MAX_TIME_LEN;

/**
 * 车载客流统计的GPS定位信息
 */
public class NET_EHOME_MPDATA extends Structure {
    public byte byIndex; //当前车门编号，从1开始
    public byte byVideoChannel;   //当前对应视屏通道号，从1开始
    public byte byRes;
    public byte byLevel;  //车内拥挤情况登记，0-空（count<20）,1-一般（20<=count<=30）,2-较多（30<=count<=50）,3-拥挤（count>=50）
    public byte[] byStarttime = new byte[MAX_TIME_LEN]; //开始统计时间/开门时间，格式：YYYY-MM-DD HH:MM:SS
    public byte[] byStoptime = new byte[MAX_TIME_LEN];  //结束统计时间/关门时间，格式：YYYY-MM-DD HH:MM:SS
    public int dwEnterNum;  //记录时间点计入人数
    public int dwLeaveNum;  //记录时间点离开人数
    public int dwCount;     // 当前时刻车内人数
}
