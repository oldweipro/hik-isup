package com.oldwei.hikisup.sdk.structure;

import com.oldwei.hikisup.sdk.service.PREVIEW_DATA_CB;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class NET_EHOME_PREVIEW_DATA_CB_PARAM extends Structure {
    public PREVIEW_DATA_CB fnPreviewDataCB;    //数据回调函数
    public Pointer pUserData;         //用户参数, 在fnPreviewDataCB回调出来
    public byte[] byRes = new byte[128];          //保留
}