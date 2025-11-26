package com.oldwei.isup.sdk.structure;

import com.oldwei.isup.sdk.HIKSDKStructure;

public class NET_EHOME_PLAYBACKBYNAME extends HIKSDKStructure {
    public byte[] szFileName = new byte[100/*MAX_FILE_NAME_LEN*/];   //回放的文件名
    public int dwSeekType;                      //0-按字节长度计算偏移量  1-按时间（秒数）计算偏移量
    public int dwFileOffset;                    //文件偏移量，从哪个位置开始下载，如果dwSeekType为0，偏移则以字节计算，为1则以秒数计算
    public int dwFileSpan;                      //下载的文件大小，为0时，表示下载直到该文件结束为止，如果dwSeekType为0，大小则以字节计算，为1则以秒数计算
}
