package com.oldwei.isup.sdk.structure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class NET_EHOME_PTXML_PARAM extends Structure {
    public Pointer pRequestUrl;        //请求URL
    public int dwRequestUrlLen;    //请求URL长度
    public Pointer pCondBuffer;        //条件缓冲区（XML格式数据）
    public int dwCondSize;         //条件缓冲区大小
    public Pointer pInBuffer;          //输入缓冲区（XML格式数据）
    public int dwInSize;           //输入缓冲区大小
    public Pointer pOutBuffer;         //输出缓冲区（XML格式数据）
    public int dwOutSize;          //输出缓冲区大小
    public int dwReturnedXMLLen;   //实际从设备接收到的XML数据的长度
    public int dwRecvTimeOut;      //默认5000ms
    public int dwHandle;           //（输出参数）设置了回放异步回调之后，该值为消息句柄，回调中用于标识（新增）
    public byte[] byRes = new byte[24];          //保留
}
