package com.oldwei.isup.sdk.service;

import com.oldwei.isup.sdk.structure.NET_DVR_AUDIODEC_PROCESS_PARAM;
import com.oldwei.isup.sdk.structure.NET_DVR_AUDIOENC_INFO;
import com.oldwei.isup.sdk.structure.NET_DVR_AUDIOENC_PROCESS_PARAM;
import com.sun.jna.Library;
import com.sun.jna.Pointer;

public interface IHikNet extends Library {
    boolean NET_DVR_Init();

    Pointer NET_DVR_InitG711Decoder();

    boolean NET_DVR_DecodeG711Frame(Pointer handle, NET_DVR_AUDIODEC_PROCESS_PARAM p_dec_proc_param);

    int NET_DVR_GetLastError();

    boolean NET_DVR_EncodeG711Frame(Pointer handle, NET_DVR_AUDIOENC_PROCESS_PARAM p_enc_proc_param);

    boolean NET_DVR_ReleaseG711Encoder(Pointer pEncodeHandle);

    //启用日志文件写入接口
    boolean NET_DVR_SetLogToFile(int bLogEnable, String strLogDir, boolean bAutoDel);

    //G711: Win64、Linux32、Linux64
    Pointer NET_DVR_InitG711Encoder(NET_DVR_AUDIOENC_INFO enc_info);//NET_DVR_AUDIOENC_INFO//NET_DVR_AUDIOENC_INFO

    boolean NET_DVR_Cleanup();
}
