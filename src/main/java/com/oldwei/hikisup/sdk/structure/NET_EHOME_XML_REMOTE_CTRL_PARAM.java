package com.oldwei.hikisup.sdk.structure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class NET_EHOME_XML_REMOTE_CTRL_PARAM extends Structure {
    public int dwSize;
    public Pointer lpInbuffer;
    public int dwInBufferSize;
    public int dwSendTimeOut;
    public int dwRecvTimeOut;
    public Pointer lpOutBuffer;     //输出缓冲区
    public int dwOutBufferSize;  //输出缓冲区大小
    public Pointer lpStatusBuffer;   //状态缓冲区,若不需要可置为NULL
    public int dwStatusBufferSize;  //状态缓冲区大小
    public byte[] byRes = new byte[16];
}