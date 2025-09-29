package com.oldwei.hikisup.sdk.service.constant;

public interface EHOME_REGISTER_TYPE {
    int ENUM_DEV_ON = 0;  //设备上线回调
    int ENUM_DEV_OFF = 1; //设备下线回调
    int ENUM_DEV_ADDRESS_CHANGED = 2;  //设备地址发生变化
    int ENUM_DEV_AUTH = 3;  //Ehome5.0设备认证回调
    int ENUM_DEV_SESSIONKEY = 4;  //Ehome5.0设备Sessionkey回调
    int ENUM_DEV_DAS_REQ = 5; //Ehome5.0设备重定向请求回调
    int ENUM_DEV_SESSIONKEY_REQ = 6;  //EHome5.0设备sessionkey请求回调
    int ENUM_DEV_DAS_REREGISTER = 7;  //设备重注册回调
    int ENUM_DEV_DAS_PINGREO = 8; //设备注册心跳
    int ENUM_DEV_DAS_EHOMEKEY_ERROR = 9; //校验密码失败
    int ENUM_DEV_SESSIONKEY_ERROR = 10;  //Sessionkey交互异常
    int ENUM_DEV_SLEEP = 11; //设备进入休眠状态（注：休眠状态下，设备无法做预览、回放、语音对讲、配置等CMS中的信令作响应；设备可通过NET_ECMS_WakeUp接口进行唤醒）
    int NET_DVR_SYSHEAD = 1;//系统头数据
    int NET_DVR_STREAMDATA = 2;//视频流数据（包括复合流和音视频分开的视频流数据）
}
