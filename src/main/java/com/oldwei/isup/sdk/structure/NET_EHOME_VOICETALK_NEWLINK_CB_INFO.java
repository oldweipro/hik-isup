package com.oldwei.isup.sdk.structure;

import com.oldwei.isup.sdk.service.VOICETALK_DATA_CB;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class NET_EHOME_VOICETALK_NEWLINK_CB_INFO extends Structure {
    public byte[] szDeviceID = new byte[256/*256*/];   //设备标示符(出参)
    public int dwEncodeType; // //SDK赋值,当前对讲设备的语音编码类型,0- G722_1，1-G711U，2-G711A，3-G726，4-AAC，5-MP2L2，6-PCM, 7-MP3, 8-G723, 9-MP1L2, 10-ADPCM, 99-RAW(未识别类型)(出参)
    public byte[] sDeviceSerial = new byte[12/*12*/];    //设备序列号，数字序列号(出参)
    public int dwAudioChan; //对讲通道(出参)
    public int lSessionID;  //设备分配给该回放会话的ID，0表示无效(出参)
    public byte[] byToken = new byte[64];
    public VOICETALK_DATA_CB fnVoiceTalkDataCB;   //数据回调函数(入参)
    public Pointer pUserData;         //用户参数, 在fnVoiceTalkDataCB回调出来(入参)
    public byte[] byRes = new byte[48];
}
