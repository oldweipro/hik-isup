package com.oldwei.isup.sdk.structure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class NET_DVR_AUDIOENC_PROCESS_PARAM extends Structure {
    public Pointer in_buf;                      /* 输入buf */
    public Pointer out_buf;                     /* 输出buf */
    public int out_frame_size;               /* 编码一帧后的BYTE数 */
    public int g726enc_reset;                /* 重置开关 */
    public int g711_type;                    /* g711编码类型,0 - U law, 1- A law */
    public int enc_mode;                     /* 音频编码模式，AMR编码配置 */
    public int[] reserved = new int[16];                 /* 保留 */
}
