package com.oldwei.isup.sdk.structure;

import com.sun.jna.Structure;

public class NET_EHOME_DEVICE_INFO extends Structure {
    public int dwSize;
    public int dwChannelNumber;
    public int dwChannelAmount;
    public int dwDevType;
    public int dwDiskNumber;
    public byte[] sSerialNumber = new byte[128];
    public int dwAlarmInPortNum;
    public int dwAlarmInAmount;
    public int dwAlarmOutPortNum;
    public int dwAlarmOutAmount;
    public int dwStartChannel;
    public int dwAudioChanNum;
    public int dwMaxDigitChannelNum;
    public int dwAudioEncType;  // 语音对讲的音频格式：0-G.722，1-G.711U，2-G.711A，3-G.726，4-AAC，5-MP2L2。
    public byte[] sSIMCardSN = new byte[128];
    public byte[] sSIMCardPhoneNum = new byte[32];
    public int dwSupportZeroChan;
    public int dwStartZeroChan;
    public int dwSmartType;
    public byte[] byRes1 = new byte[2];
    public byte byStartDTalkChan;
    public byte[] byRes = new byte[157];
}
