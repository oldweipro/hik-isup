package com.oldwei.isup.sdk.structure;

import com.sun.jna.Structure;

import static com.oldwei.isup.sdk.service.constant.EHOME_ALARM_TYPE.*;

public class NET_EHOME_HEATMAP_REPORT extends Structure {
    public int dwSize;
    public byte[] byDeviceID = new byte[MAX_DEVICE_ID_LEN]; //设备注册ID
    public int dwVideoChannel;  //通道号
    public byte[] byStartTime = new byte[MAX_TIME_LEN]; //开始时间（设备本地时间），格式：YYYY-MM-DD HH:MM:SS
    public byte[] byStopTime = new byte[MAX_TIME_LEN];  //结束时间（设备本地时间），格式：YYYY-MM-DD HH:MM:SS
    public NET_EHOME_HEATMAP_VALUE struHeatmapValue; //热度值
    public NET_EHOME_PIXEL_ARRAY_SIZE struPixelArraySize;  //热度图大小
    public byte[] byPixelArrayData = new byte[MAX_URL_LEN]; //热度图数据索引
    public byte byRetransFlag;   //重传标记，0-实时包，1-重传包
    public byte byTimeDiffH;  //byStartTime，byStopTime与国际标准时间（UTC）的时差（小时），-12 ... +14,0xff表示无效
    public byte byTimeDiffM; //byStartTime，byStopTime与国际标准时间（UTC）的时差（分钟），-30,0, 30, 45, 0xff表示无效
    public byte[] byRes = new byte[61];
}
