package com.oldwei.hikisup.sdk.service;

import com.oldwei.hikisup.sdk.structure.NET_EHOME_CMS_LISTEN_PARAM;
import com.oldwei.hikisup.sdk.structure.NET_EHOME_LISTEN_PREVIEW_CFG;
import com.oldwei.hikisup.sdk.structure.NET_EHOME_PREVIEW_DATA_CB_PARAM;
import com.sun.jna.Library;
import com.sun.jna.Pointer;

public interface IHikISUPStream extends Library {
    /**
     * 初始化流媒体
     * @return
     */
    boolean NET_ESTREAM_Init();
    boolean NET_ESTREAM_Fini();
    boolean NET_ESTREAM_SetSDKInitCfg(int enumType, Pointer lpInBuff);
    boolean NET_ESTREAM_SetSDKLocalCfg(int enumType, Pointer lpInBuff);
    int NET_ESTREAM_GetLastError();

    /**
     * 设置日志输出文件
     * @param iLogLevel
     * @param strLogDir
     * @param bAutoDel
     * @return
     */
    boolean NET_ESTREAM_SetLogToFile(int iLogLevel, String strLogDir, boolean bAutoDel);
    int NET_ESTREAM_StartListenPreview(NET_EHOME_LISTEN_PREVIEW_CFG pListenParam);
    boolean NET_ESTREAM_StopListenPreview(int lListenHandle);
    boolean NET_ESTREAM_SetPreviewDataCB(int iHandle, NET_EHOME_PREVIEW_DATA_CB_PARAM pStruCBParam);
    boolean NET_ESTREAM_StopPreview(int iPreviewHandle);
}
