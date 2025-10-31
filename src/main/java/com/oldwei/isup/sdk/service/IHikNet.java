package com.oldwei.isup.sdk.service;

import com.oldwei.isup.sdk.structure.NET_DVR_AUDIODEC_PROCESS_PARAM;
import com.sun.jna.Library;
import com.sun.jna.Pointer;

public interface IHikNet extends Library {
    boolean NET_DVR_Init();

    Pointer NET_DVR_InitG711Decoder();

    boolean NET_DVR_DecodeG711Frame(Pointer handle, NET_DVR_AUDIODEC_PROCESS_PARAM p_dec_proc_param);

    int NET_DVR_GetLastError();
}
