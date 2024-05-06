package com.oldwei.hikisup.sdk.SdkService.AlarmService;

import com.sun.jna.*;
import com.sun.jna.examples.win32.GDI32.RECT;
import com.sun.jna.examples.win32.W32API;

//SDK接口说明,HCNetSDK.dll

//windows gdi接口,gdi32.dll in system32 folder, 在设置遮挡区域,移动侦测区域等情况下使用
public interface HCISUPAlarm extends Library {

    /***宏定义***/
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

    // 报警事件
    public static final int EHOME_ALARM_UNKNOWN = 0;   //未知报警类型
    public static final int EHOME_ALARM = 1;   //Ehome基本报警
    public static final int EHOME_ALARM_HEATMAP_REPORT = 2;   //热度图报告
    public static final int EHOME_ALARM_FACESNAP_REPORT = 3;   //人脸抓拍报告
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

    public static class NET_EHOME_IPADDRESS extends Structure {
        public byte[] szIP = new byte[128];
        public short wPort;     //端口
        public byte[] byRes = new byte[2];
    }

    public static class BYTE_ARRAY extends Structure {
        public byte[] byValue;

        public BYTE_ARRAY(int iLen) {
            byValue = new byte[iLen];
        }
    }

    public static class NET_EHOME_ALARM_MSG extends Structure {
        public int dwAlarmType;      //报警类型，见EN_ALARM_TYPE
        public Pointer pAlarmInfo;       //报警内容（结构体）
        public int dwAlarmInfoLen;   //结构体报警内容长度
        public Pointer pXmlBuf;          //报警内容（XML）
        public int dwXmlBufLen;      //xml报警内容长度
        public byte[] sSerialNumber = new byte[NET_EHOME_SERIAL_LEN]; //设备序列号，用于进行Token认证
        public Pointer pHttpUrl;
        public int dwHttpUrlLen;
        public byte[] byRes = new byte[12];
    }

    public static class NET_EHOME_ALARM_ISAPI_INFO extends Structure {
        public Pointer pAlarmData;           // 报警数据
        public int dwAlarmDataLen;   // 报警数据长度
        public byte byDataType;        // 0-invalid,1-xml,2-json
        public byte byPicturesNumber;  // 图片数量
        public byte[] byRes = new byte[2];
        public Pointer pPicPackData;         // 图片变长部分,byPicturesNumber个NET_EHOME_ALARM_ISAPI_PICDATA
        public byte[] byRes1 = new byte[32];
    }

    public static class NET_EHOME_HEATMAP_REPORT extends Structure {
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

    public static class NET_EHOME_HEATMAP_VALUE extends Structure {
        public int dwMaxHeatMapValue;    //最大热度值
        public int dwMinHeatMapValue;    //最小热度值
        public int dwTimeHeatMapValue;   //平均热度值
    }

    public static class NET_EHOME_PIXEL_ARRAY_SIZE extends Structure {
        public int dwLineValue;   //像素点行值
        public int dwColumnValue; //像素点列值
    }

    public static class NET_EHOME_FACESNAP_REPORT extends Structure {
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

    public static class NET_EHOME_ZONE extends Structure {
        public int dwX;          //X轴坐标
        public int dwY;          //Y轴坐标
        public int dwWidth;      //宽度
        public int dwHeight;     //高度
    }

    public static class NET_EHOME_HUMAN_FEATURE extends Structure {
        public byte byAgeGroup;    //年龄属性，1-婴幼儿，2-儿童，3-少年，4-青少年，5-青年，6-壮年，7-中年，8-中老年，9-老年
        public byte bySex;            //性别属性，1-男，2-女
        public byte byEyeGlass;    //是否戴眼睛，1-不戴，2-戴
        public byte byMask;        //是否戴口罩，1-不戴，2-戴
        public byte[] byRes = new byte[12];
    }

    public static class NET_EHOME_GPS_INFO extends Structure {
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

    public static class NET_EHOME_CID_INFO extends Structure {
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

    public static class NET_EHOME_CID_PARAM extends Structure {
        public int dwUserType;  //用户类型，1键盘用户 2网络用户，其他值表示无效
        public int lUserNo;  //用户类型，-1表示无效
        public int lZoneNo;  //防区号，-1表示无效
        public int lKeyboardNo;  //键盘号
        public int lVideoChanNo; //视频通道号
        public int lDiskNo; //硬盘号
        public int lModuleAddr; //模块地址
        public byte[] byUserName = new byte[NAME_LEN];  //用户名
        public byte[] byRes = new byte[32];
    }

    public static class NET_EHOME_CID_INFO_INTERNAL_EX extends Structure {
        public byte byRecheck;                //是否是视频复核报警 1-复核报警，0-普通报警
        public byte[] byRes = new byte[3];
        public byte[] byUUID = new byte[MAX_UUID_LEN];     //报警唯一ID，区分是否属于同一个报警；不支持视频复核报警时，该字段为0；
        public byte[] byVideoURL = new byte[MAX_URL_LEN];  // byRecheck为1时有效，视频复核报警中视频的URL地址，用于从存储服务器获取视频；（复核报警第二次上报该URL）
        public byte[] byCIDDescribeEx = new byte[CID_DES_LEN_EX];  //CID报警描述扩展
        public byte[] byVideoType = new byte[MAX_VIDEO_TYPE_LEN];
        public byte[] byLinkageSubSystem = new byte[MAX_SUBSYSTEM_LEN];  //关联的子系统
        public byte[] byRes1 = new byte[176];
    }

    public static class NET_EHOME_CID_INFO_PICTUREINFO_EX extends Structure {
        public byte[][] byPictureURL = new byte[MAX_PICTURE_NUM][MAX_URL_LEN];//图片URL
        public byte[] byRes1 = new byte[512];
    }

    public static interface EHomeMsgCallBack extends Callback {
        public boolean invoke(int iHandle, NET_EHOME_ALARM_MSG pAlarmMsg, Pointer pUser);
    }

    public static class NET_EHOME_ALARM_LISTEN_PARAM extends Structure {
        public NET_EHOME_IPADDRESS struAddress;
        public EHomeMsgCallBack fnMsgCb; //报警信息回调函数
        public Pointer pUserData;   //用户数据
        public byte byProtocolType;    //协议类型，0-TCP,1-UDP
        public byte byUseCmsPort; //是否复用CMS端口,0-不复用，非0-复用，如果复用cms端口，协议类型字段无效（此时本地监听信息struAddress填本地回环地址）
        public byte byUseThreadPool;  //0-回调报警时，使用线程池，1-回调报警时，不使用线程池，默认情况下，报警回调的时候，使用线程池
        public byte byRes[] = new byte[29];
        ;
    }

    public static class NET_EHOME_LOCAL_GENERAL_CFG extends Structure {
        public byte byAlarmPictureSeparate;        //控制透传ISAPI报警数据和图片是否分离，0-不分离，1-分离（分离后走EHOME_ISAPI_ALARM回调返回）
        public byte[] byRes = new byte[127];
    }

    public static class NET_EHOME_ALARM_INFO extends Structure {
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

    public static class NET_EHOME_ALARM_STATUS_UNION extends Structure {
        public byte[] byRes = new byte[12]; // 联合体大小
        public NET_EHOME_DEV_STATUS_CHANGED struDevStatusChanged;
        public NET_EHOME_CHAN_STATUS_CHANGED struChanStatusChanged;
        public NET_EHOME_HD_STATUS_CHANGED struHdStatusChanged;
        public NET_EHOME_DEV_TIMING_STATUS struDevTimeStatus;
        public NET_EHOME_CHAN_TIMING_STATUS_SINGLE struChanTimeStatus;
        public NET_EHOME_HD_TIMING_STATUS_SINGLE struHdTimeStatus;
    }

    public static class NET_EHOME_DEV_STATUS_CHANGED extends Structure {
        public byte byDeviceStatus;
        public byte[] byRes = new byte[11];
    }

    public static class NET_EHOME_CHAN_STATUS_CHANGED extends Structure {
        public short wChanNO; // 通道号
        public byte byChanStatus; //通道状态，按位表示
        //bit0：启用状态，0-禁用/删除，1-启用/添加
        //模拟通道由禁用到启用，或者启用到禁用，上报该字段
        //数字通道添加到删除，或者删除到重新添加，上报该字段
        //bit1：在线状态，0-不在线，1-在线
        //bit2：信号状态，0-无信号，1-有信号
        //bit3：录像状态，0-不在录像 1-在录像
        //bit4：IP通道信息改变状态，0-未改变 1-有改变，这位表示该通道的配
        //置信息发生了改变，比如添加的IPC有过更换，通知上层更新能力集

        public byte[] byRes = new byte[9];
    }

    // 磁盘状态改变
    public static class NET_EHOME_HD_STATUS_CHANGED extends Structure {
        public int dwVolume; //硬盘容量，单位：MB
        public short wHDNo; //硬盘号
        public byte byHDStatus; //硬盘的状态, 0-活动1-休眠,2-异常,3-休眠硬盘出错,4-未格式化, 5-未连接状态(网络硬盘),6-硬盘正在格式化
        public byte[] byRes = new byte[5]; //保留
    }

    // 定时上报设备状态项
    public static class NET_EHOME_DEV_TIMING_STATUS extends Structure {
        public int dwMemoryTotal; // 内存总量，单位Kbyte
        public int dwMemoryUsage; // 内存使用量，单位Kbyte
        public byte byCPUUsage;   // CPU使用率，0-100
        public byte byMainFrameTemp; // 机箱温度，单位：摄氏度
        public byte byBackPanelTemp; // 背板温度，单位：摄氏度
        public byte byRes;
    }

    // 定时上报通道状态项-单个通道
    public static class NET_EHOME_CHAN_TIMING_STATUS_SINGLE extends Structure {
        public int dwBitRate; // 实际码率，单位kbps
        public short wChanNO; // 通道号
        public byte byLinkNum; // 客户端连接的个数
        public byte[] byRes = new byte[5];
    }

    // 定时上报磁盘状态项
    public static class NET_EHOME_HD_TIMING_STATUS_SINGLE extends Structure {
        public int dwHDFreeSpace;  // 硬盘剩余空间，单位：MB
        public short wHDNo;  // 磁盘号
        public byte[] byRes = new byte[6];
    }

    /**
     * 图片URL 报警
     */
    public static class NET_EHOME_NOTICE_PICURL extends Structure {
        public int dwSize;
        public byte[] byDeviceID = new byte[MAX_DEVICE_ID_LEN];
        public short wPicType;
        public short wAlarmType;
        public int dwAlarmChan;
        public byte[] byAlarmTime = new byte[MAX_TIME_LEN];
        public int dwCaptureChan;
        public byte[] byPicTime = new byte[MAX_TIME_LEN];
        public byte[] byPicUrl = new byte[MAX_URL_LEN];
        public int dwManualSnapSeq;
        public byte byRetransFlag;
        public byte byTimeDiffH;
        public byte byTimeDiffM;
        public byte[] byRes = new byte[29];
    }

    /**
     * 异步失败通知信息结构体
     */
    public static class NET_EHOME_NOTIFY_FAIL_INFO extends Structure {
        public int dwSize;
        public byte[] byDeviceID = new byte[MAX_DEVICE_ID_LEN];
        public short wFailedCommand;
        public short wPicType;
        public int dwManualSnapSeq;
        public byte byRetransFlag;
        public byte[] byRes = new byte[31];
    }

    /**
     * 无线报警信息结构体
     */
    public static class NET_EHOME_ALARMWIRELESSINFO extends Structure {
        public byte[] byDeviceID = new byte[MAX_DEVICE_ID_LEN];
        public int dwDataTraffic;
        public byte bySignalIntensity;
        public byte[] byRes = new byte[127];
    }

    /**
     * 车载客流统计的定位信息
     */
    public static class NET_EHOME_ALARM_MPDCDATA extends Structure {
        public byte[] byDeviceID = new byte[MAX_DEVICE_ID_LEN];//设备ID
        public byte[] bySampleTime = new byte[MAX_TIME_LEN]; //GPS采样时间，格式：YYYY-MM-DD HH:MM:SS
        public byte byTimeZoneIdx;  //时区
        public byte byRetranseFlag;  //重传标记, 0-实时包, 1-重传包
        public byte[] byRes = new byte[2];
        public NET_EHOME_MPGPS struGpsInfo;  //GPS信息
        public NET_EHOME_MPDATA struMPData;
    }

    /**
     * 车载客流统计的GPS定位信息
     */
    public static class NET_EHOME_MPGPS extends Structure {
        public int iLongitude;
        public int iLatitude;
        public int iSpeed;
        public int iDirection;
    }


    /**
     * 车载客流统计的GPS定位信息
     */
    public static class NET_EHOME_MPDATA extends Structure {
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

    //初始化，反初始化
    boolean NET_EALARM_Init();

    boolean NET_EALARM_Fini();

    boolean NET_EALARM_SetSDKLocalCfg(int enumType, Pointer lpInbuffer);

    int NET_EALARM_StartListen(NET_EHOME_ALARM_LISTEN_PARAM pAlarmListenParam);

    int NET_EALARM_GetLastError();

    boolean NET_EALARM_StopListen(int iListenHandle);

    boolean NET_EALARM_SetDeviceSessionKey(Pointer pDeviceKey);

    boolean NET_EALARM_SetLogToFile(int iLogLevel, String strLogDir, boolean bAutoDel);

    boolean NET_EALARM_SetSDKInitCfg(int enumType, Pointer lpInBuff);
}

//windows user32接口,user32.dll in system32 folder, 在设置遮挡区域,移动侦测区域等情况下使用
interface USER32 extends W32API {

    USER32 INSTANCE = (USER32) Native.loadLibrary("user32", USER32.class, DEFAULT_OPTIONS);

    public static final int BF_LEFT = 0x0001;
    public static final int BF_TOP = 0x0002;
    public static final int BF_RIGHT = 0x0004;
    public static final int BF_BOTTOM = 0x0008;
    public static final int BDR_SUNKENOUTER = 0x0002;
    public static final int BF_RECT = (BF_LEFT | BF_TOP | BF_RIGHT | BF_BOTTOM);

    boolean DrawEdge(HDC hdc, RECT qrc, int edge, int grfFlags);

    int FillRect(HDC hDC, RECT lprc, HANDLE hbr);
}
