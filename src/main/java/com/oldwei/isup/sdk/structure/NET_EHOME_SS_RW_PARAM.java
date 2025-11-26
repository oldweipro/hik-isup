package com.oldwei.isup.sdk.structure;

import com.sun.jna.Pointer;
import com.oldwei.isup.sdk.HIKSDKStructure;
import com.sun.jna.ptr.IntByReference;

public class NET_EHOME_SS_RW_PARAM extends HIKSDKStructure {
    public Pointer pFileName;   //文件名
    public Pointer pFileBuf;          //文件内容
    public IntByReference dwFileLen;   //文件大小
    public Pointer pFileUrl;    //文件url
    public Pointer pUser;             //
    public byte byAct; //读写操作：0-写文件，1-读文件
    public byte byUseRetIndex;  //是否使用上层返回的pRetIndex：0-不使用，1-使用
    public byte[] byRes1 = new byte[2];
    public Pointer pRetIndex;     //上层设置的索引，pRetIndex为0时，可不设置，pRetIndex为1时候，设置
    public byte[] byRes = new byte[56];
}
