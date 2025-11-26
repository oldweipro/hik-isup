package com.oldwei.isup.sdk.structure;

import com.oldwei.isup.sdk.HIKSDKStructure;

public class NET_DVR_AUDIOENC_INFO extends HIKSDKStructure {
    public int in_frame_size;                /* 输入一帧数据大小(BYTES)，由GetInfoParam函数返回         */
    public int[] reserved = new int[16];                 /* 保留 */
}
