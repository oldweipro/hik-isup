package com.oldwei.isup.sdk.structure;

import com.sun.jna.Structure;

import static com.oldwei.isup.sdk.service.constant.EHOME_ALARM_TYPE.*;

public class NET_EHOME_CID_INFO_INTERNAL_EX extends Structure {
    public byte byRecheck;                //是否是视频复核报警 1-复核报警，0-普通报警
    public byte[] byRes = new byte[3];
    public byte[] byUUID = new byte[MAX_UUID_LEN];     //报警唯一ID，区分是否属于同一个报警；不支持视频复核报警时，该字段为0；
    public byte[] byVideoURL = new byte[MAX_URL_LEN];  // byRecheck为1时有效，视频复核报警中视频的URL地址，用于从存储服务器获取视频；（复核报警第二次上报该URL）
    public byte[] byCIDDescribeEx = new byte[CID_DES_LEN_EX];  //CID报警描述扩展
    public byte[] byVideoType = new byte[MAX_VIDEO_TYPE_LEN];
    public byte[] byLinkageSubSystem = new byte[MAX_SUBSYSTEM_LEN];  //关联的子系统
    public byte[] byRes1 = new byte[176];
}
