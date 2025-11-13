package com.oldwei.isup.sdk.service;

import com.oldwei.isup.sdk.structure.*;
import com.sun.jna.Library;
import com.sun.jna.Pointer;

public interface IHikISUPStream extends Library {
    /**
     * 初始化流媒体
     *
     * @return
     */
    boolean NET_ESTREAM_Init();

    boolean NET_ESTREAM_Fini();

    boolean NET_ESTREAM_SetSDKInitCfg(int enumType, Pointer lpInBuff);

    boolean NET_ESTREAM_SetSDKLocalCfg(int enumType, Pointer lpInBuff);

    int NET_ESTREAM_GetLastError();

    /**
     * 设置日志输出文件
     *
     * @param iLogLevel
     * @param strLogDir
     * @param bAutoDel
     * @return
     */
    boolean NET_ESTREAM_SetLogToFile(int iLogLevel, String strLogDir, boolean bAutoDel);

    int NET_ESTREAM_StartListenPreview(NET_EHOME_LISTEN_PREVIEW_CFG pListenParam);

    /**
     * 停止监听预览
     *
     * @param lListenHandle
     * @return
     */
    boolean NET_ESTREAM_StopListenPreview(int lListenHandle);

    /**
     * 启动CMS监听
     *
     * @param iHandle
     * @param pStruCBParam
     * @return
     */
    boolean NET_ESTREAM_SetPreviewDataCB(int iHandle, NET_EHOME_PREVIEW_DATA_CB_PARAM pStruCBParam);

    /**
     * 停止预览
     *
     * @param iPreviewHandle
     * @return
     */
    boolean NET_ESTREAM_StopPreview(int iPreviewHandle);

    int NET_ESTREAM_StartListenVoiceTalk(NET_EHOME_LISTEN_VOICETALK_CFG pListenParam);

    boolean NET_ESTREAM_SetVoiceTalkDataCB(int lHandle, NET_EHOME_VOICETALK_DATA_CB_PARAM pStruCBParam);

    int NET_ESTREAM_StartListenPlayBack(NET_EHOME_PLAYBACK_LISTEN_PARAM pListenParam);

    boolean NET_ESTREAM_SetPlayBackDataCB(int iPlayBackLinkHandle, NET_EHOME_PLAYBACK_DATA_CB_PARAM pDataCBParam);

    boolean NET_ESTREAM_StopPlayBack(int iPlayBackLinkHandle);
}
