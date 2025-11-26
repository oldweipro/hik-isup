package com.oldwei.isup.sdk.structure;

import com.oldwei.isup.sdk.HIKSDKStructure;

import static com.oldwei.isup.sdk.service.constant.EHOME_ALARM_TYPE.NAME_LEN;

public class NET_EHOME_CID_PARAM extends HIKSDKStructure {
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
