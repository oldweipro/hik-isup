package com.oldwei.isup.sdk.structure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class NET_DVR_AUDIODEC_PROCESS_PARAM extends Structure {
    public Pointer in_buf;                      /* 输入数据buf */
    public Pointer out_buf;                     /* 输出数据buf */
    public int in_data_size;                 /* 输入in_buf内数据byte数 */
    public int proc_data_size;               /* 输出解码库处理in_buf中数据大小bytes */
    public int out_frame_size;               /* 解码一帧后数据BYTE数 */
    public NET_DVR_AUDIODEC_INFO dec_info = new NET_DVR_AUDIODEC_INFO();                     /* 输出解码信息 */
    public int g726dec_reset;                /* 重置开关 */
    public int g711_type;                    /* g711编码类型,0 - U law, 1- A law */
    public int[] reserved = new int[16];                 /* 保留 */
}