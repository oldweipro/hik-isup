package com.oldwei.isup.sdk.structure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class NET_EHOME_ALARM_ISAPI_INFO extends Structure {
    public Pointer pAlarmData;           // 报警数据
    public int dwAlarmDataLen;   // 报警数据长度
    public byte byDataType;        // 0-invalid,1-xml,2-json
    public byte byPicturesNumber;  // 图片数量
    public byte[] byRes = new byte[2];
    public Pointer pPicPackData;         // 图片变长部分,byPicturesNumber个NET_EHOME_ALARM_ISAPI_PICDATA
    public byte[] byRes1 = new byte[32];
}
