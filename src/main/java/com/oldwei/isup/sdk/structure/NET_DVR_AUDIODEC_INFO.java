package com.oldwei.isup.sdk.structure;

import com.oldwei.isup.sdk.HIKSDKStructure;

public class NET_DVR_AUDIODEC_INFO extends HIKSDKStructure {
    public int nchans;                         /* 声道数 */
    public int sample_rate;                  /* 采样率 */
    public int aacdec_profile;               /* 编码用的框架 */
    public int[] reserved = new int[16];                 /* 保留 */
}
