package com.oldwei.isup.sdk.service;

import com.oldwei.isup.sdk.structure.NET_EHOME_SS_CLIENT_PARAM;
import com.oldwei.isup.sdk.structure.NET_EHOME_SS_LISTEN_HTTPS_PARAM;
import com.oldwei.isup.sdk.structure.NET_EHOME_SS_LISTEN_PARAM;
import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public interface IHikISUPStorage extends Library {
    /**
     * 获取错误码
     */
    int NET_ESS_GetLastError();

    /**
     * 日志
     *
     * @param iLogLevel
     * @param strLogDir
     * @param bAutoDel
     * @return
     */
    boolean NET_ESS_SetLogToFile(int iLogLevel, String strLogDir, boolean bAutoDel);

    boolean NET_ESS_SetSDKInitCfg(int enumType, Pointer lpInBuff);

    /**
     * 获取版本号
     *
     * @return
     */
    int NET_ESS_GetBuildVersion();

    /**
     * 设置HTTP监听的Https参数
     *
     * @param pSSHttpsParam
     * @return
     */
    boolean NET_ESS_SetListenHttpsParam(NET_EHOME_SS_LISTEN_HTTPS_PARAM pSSHttpsParam);

    /**
     * 开启监听
     *
     * @param pSSListenParam
     * @return
     */
    int NET_ESS_StartListen(NET_EHOME_SS_LISTEN_PARAM pSSListenParam);

    /**
     * 关闭监听
     *
     * @param lListenHandle
     * @return
     */
    boolean NET_ESS_StopListen(int lListenHandle);

    /**
     * 设置初始化参数
     * @param enumType NET_EHOME_SS_INIT_CFG_TYPE enumType
     * @param lpInBuff
     * @return
     */


    /**
     * 创建图片上传/下载客户端
     *
     * @param pClientParam
     * @return
     */
    int NET_ESS_CreateClient(NET_EHOME_SS_CLIENT_PARAM pClientParam);

    /**
     * 设置图片上传/下载客户端超时时间,单位ms,默认为5s
     *
     * @param lHandle
     * @param dwSendTimeout
     * @param dwRecvTimeout
     * @return
     */
    boolean NET_ESS_ClientSetTimeout(int lHandle, int dwSendTimeout, int dwRecvTimeout);

    /**
     * 设置图片上传/下载客户端参数
     *
     * @param lHandle
     * @param strParamName
     * @param strParamVal
     * @return
     */
    boolean NET_ESS_ClientSetParam(int lHandle, String strParamName, String strParamVal);

    /**
     * 图片上传/下载客户端执行上传
     *
     * @param lHandle
     * @param strUrl
     * @param dwUrlLen
     * @return
     */
    boolean NET_ESS_ClientDoUpload(int lHandle, byte[] strUrl, int dwUrlLen);

    /**
     * 图片上传/下载客户端执行下载
     *
     * @param lHandle
     * @param strUrl
     * @param pFileContent
     * @param dwContentLen
     * @return
     */
    boolean NET_ESS_ClientDoDownload(int lHandle, String strUrl, PointerByReference pFileContent, IntByReference dwContentLen);

    /**
     * 销毁客户端
     *
     * @param lHandle
     * @return
     */
    boolean NET_ESS_DestroyClient(int lHandle);

    //计算HMAC-SHA256
    boolean NET_ESS_HAMSHA256(String pSrc, String pSecretKey, String pSingatureOut, int dwSingatureLen);

    //初始化，反初始化
    boolean NET_ESS_Init();

    boolean NET_ESS_Fini();

}
