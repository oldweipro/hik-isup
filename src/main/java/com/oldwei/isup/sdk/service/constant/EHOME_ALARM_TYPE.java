package com.oldwei.isup.sdk.service.constant;

public interface EHOME_ALARM_TYPE {
    //常量
    public static final int MAX_DEVICE_ID_LEN = 256;    //设备ID长度
    public static final int NAME_LEN = 32;      //用户名长度（在HCNetSDK的头文件中也有定义）
    public static final int NET_EHOME_SERIAL_LEN = 12;  //设备序列号长度

    public static final int MAX_TIME_LEN = 32;       //时间字符串长度
    public static final int MAX_REMARK_LEN = 64;     //报警备注长度
    public static final int MAX_URL_LEN = 512;       //URL长度
    public static final int CID_DES_LEN = 32;        //CID报警描述长度
    public static final int MAX_FILE_PATH_LEN = 256;
    public static final int MAX_UUID_LEN = 64;       //最大UUID长度
    public static final int CID_DES_LEN_EX = 256;    //CID报警描述长度扩展
    public static final int MAX_PICTURE_NUM = 5;     //最大图片数量
    public static final int MAX_VIDEO_TYPE_LEN = 16;
    public static final int MAX_SUBSYSTEM_LEN = 64;  //子系统个数最大值

    public static final int EHOME_ALARM_UNKNOWN = 0;   //未知报警类型
    public static final int EHOME_ALARM = 1;   //Ehome基本报警
    public static final int EHOME_ALARM_HEATMAP_REPORT = 2;   //热度图报告
    public static final int EHOME_ALARM_FACESNAP_REPORT = 3;   //图片抓拍报告
    public static final int EHOME_ALARM_GPS = 4;   //GPS信息上传
    public static final int EHOME_ALARM_CID_REPORT = 5;   //报警主机CID告警上传
    public static final int EHOME_ALARM_NOTICE_PICURL = 6;   //图片URL上报
    public static final int EHOME_ALARM_NOTIFY_FAIL = 7;   //异步失败通知
    public static final int EHOME_ALARM_SELFDEFINE = 9;   //自定义报警上传
    public static final int EHOME_ALARM_DEVICE_NETSWITCH_REPORT = 10;    //设备网络切换上传
    public static final int EHOME_ALARM_ACS = 11;  //门禁事件上报
    public static final int EHOME_ALARM_WIRELESS_INFO = 12;  //无线网络信息上传
    public static final int EHOME_ISAPI_ALARM = 13;  //ISAPI报警上传
    public static final int EHOME_INFO_RELEASE_PRIVATE = 14;  //为了兼容信息发布产品的私有EHome协议报警（不再维护）
    public static final int EHOME_ALARM_MPDCDATA = 15;  //车载设备的客流数据
    public static final int EHOME_ALARM_QRCODE = 20;  //二维码报警上传
    public static final int EHOME_ALARM_FACETEMP = 21;  //人脸测温报警上传

    public static final int ALARM_TYPE_DEV_CHANGED_STATUS = 700;    //700-设备状态改变报警上传
    public static final int ALARM_TYPE_CHAN_CHANGED_STATUS = 701;   //701-通道状态改变报警上报
    public static final int ALARM_TYPE_HD_CHANGED_STATUS = 702;     //702-磁盘状态改变报警上报
    public static final int ALARM_TYPE_DEV_TIMING_STATUS = 703;     //703-定时上报设备状态信息
    public static final int ALARM_TYPE_CHAN_TIMING_STATUS = 704;    //704-定时上报通道状态信息
    public static final int ALARM_TYPE_HD_TIMING_STATUS = 705;      //705-定时上报磁盘状态信息
    public static final int ALARM_TYPE_RECORD_ABNORMAL = 706;       //706-录像异常，当前时间点本来应该是在执行录像的，结果因为异常原因导致设备无法正常录像。
}
