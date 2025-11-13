package com.oldwei.isup.sdk.structure;

import com.oldwei.isup.sdk.service.PLAYBACK_DATA_CB;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import static com.oldwei.isup.sdk.service.constant.EHOME_ALARM_TYPE.NET_EHOME_SERIAL_LEN;

public class NET_EHOME_PLAYBACK_NEWLINK_CB_INFO extends Structure {
    public byte[] szDeviceID = new byte[256];
    public int lSessionID;     //设备分配给该回放会话的ID，0表示无效(出参)
    public int dwChannelNo;    //设备通道号，0表示无效(出参)
    public byte[] sDeviceSerial = new byte[NET_EHOME_SERIAL_LEN/*12*/]; //设备序列号，数字序列号(出参)
    public byte byStreamFormat;         //码流封装格式：0-PS 1-RTP(入参)
    public byte[] byRes1 = new byte[3];
    public PLAYBACK_DATA_CB fnPlayBackDataCB;
    public Pointer pUserData;
    public byte[] byRes = new byte[88];
}
