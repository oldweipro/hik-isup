package com.oldwei.isup.sdk.structure;

import com.sun.jna.Structure;

import static com.oldwei.isup.sdk.service.constant.EHOME_ALARM_TYPE.MAX_DEVICE_ID_LEN;
import static com.oldwei.isup.sdk.service.constant.EHOME_ALARM_TYPE.MAX_TIME_LEN;

public class NET_EHOME_GPS_INFO extends Structure {
    public int dwSize;
    public byte[] bySampleTime = new byte[MAX_TIME_LEN]; //GPS采样时间，设备本地时间，格式：YYYY-MM-DD HH:MM:SS
    public byte[] byDeviceID = new byte[MAX_DEVICE_ID_LEN];  //设备注册ID
    public byte[] byDivision = new byte[2];  //division[0]:'E'or'W'(东经/西经), division[1]:'N'or'S'(北纬/南纬)
    public byte bySatelites;//卫星数量
    public byte byPrecision; //精度因子，原始值*100
    public int dwLongitude; //经度，取值范围为（0～180*3600*100），转换公式为：longitude= 实际度*3600*100+实际分*60*100+实际秒*100
    public int dwLatitude; //纬度，取值范围为（0～90*3600*100），转换公式为：latitude = 实际度*3600*100+实际分*60*100+实际秒*100
    public int dwDirection;  //方向，取值范围为（0～359.9*100），正北方向为0，转换公式为：direction= 实际方向*100
    public int dwSpeed;//速度，取值范围为（0～999.9*100000），转换公式为：speed =实际速度*100000，相当于cm/h
    public int dwHeight; //高度，单位：cm
    public byte byRetransFlag; //重传标记，0-实时包，1-重传包
    public byte byLocateMode; //定位模式(初值0)，仅NMEA0183 3.00版本输出，值：0-自主定位，1- 差分，2- 估算，3- 数据无效
    public byte byTimeDiffH; //bySampleTime与国际标准时间（UTC）的时差（小时），-12 ... +14,0xff表示无效
    public byte byTimeDiffM;  //bySampleTimee与国际标准时间（UTC）的时差（分钟），-30,0, 30, 45, 0xff表示无效
    public int dwMileage; //设备里程数统计，单位为米，整型，每天第一次开机或者00:00:00时清零，后续累加当天里程数上报到中心
    public byte[] byRes = new byte[56];
}
