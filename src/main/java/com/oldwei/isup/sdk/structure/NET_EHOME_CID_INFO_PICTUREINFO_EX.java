package com.oldwei.isup.sdk.structure;

import com.oldwei.isup.sdk.HIKSDKStructure;

import static com.oldwei.isup.sdk.service.constant.EHOME_ALARM_TYPE.MAX_PICTURE_NUM;
import static com.oldwei.isup.sdk.service.constant.EHOME_ALARM_TYPE.MAX_URL_LEN;

public class NET_EHOME_CID_INFO_PICTUREINFO_EX extends HIKSDKStructure {
    public byte[][] byPictureURL = new byte[MAX_PICTURE_NUM][MAX_URL_LEN];//图片URL
    public byte[] byRes1 = new byte[512];
}
