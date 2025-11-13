package com.oldwei.isup.sdk.structure;

import com.sun.jna.Structure;

public class NET_EHOME_VOICE_TALK_IN extends Structure {
    public int dwVoiceChan;                                  //通道号
    public NET_EHOME_IPADDRESS struStreamSever;                //流媒体地址
    public byte[] byEncodingType = new byte[9];    //语音对讲编码类型
    public byte byLinkEncrypt;  //
    public byte byBroadcast;  //语音广播标识,设备接收到本标识为1后不进行音频采集发送给对端
    public byte byBroadLevel;//语音广播优先级标识,0~15优先级从低到高,当存在byBroadcast为1时,0标识最低优先级。当存在byBroadcast为0时，本节点无意义为保留字节
    public byte byBroadVolume; //语音广播音量,0~15音量从低到高,当存在byBroadcast为1时,0标识最低音量。当存在byBroadcast为0时，本节点无意义为保留字节
    public byte byAudioSamplingRate; //音频采样率 0-默认, 1-16kHZ, 2-32kHZ, 3-48kHZ, 4-44.1kHZ, 5-8kHZ
    public byte[] byRes = new byte[114];
}
