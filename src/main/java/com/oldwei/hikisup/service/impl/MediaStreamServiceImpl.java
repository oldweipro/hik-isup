package com.oldwei.hikisup.service.impl;

import com.oldwei.hikisup.config.HikIsupProperties;
import com.oldwei.hikisup.domain.DeviceCache;
import com.oldwei.hikisup.sdk.service.IHCISUPCMS;
import com.oldwei.hikisup.sdk.service.IHikISUPStream;
import com.oldwei.hikisup.sdk.structure.*;
import com.oldwei.hikisup.service.IMediaStreamService;
import com.oldwei.hikisup.util.FileUtil;
import com.oldwei.hikisup.util.GlobalCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaStreamServiceImpl implements IMediaStreamService {

    private final HikIsupProperties hikIsupProperties;
    private final IHikISUPStream hikISUPStream;
    private final IHCISUPCMS ihcisupcms;

    @Async("taskExecutor")
    @Override
    public void preview(int lLoginID, int lChannel, String deviceId, String randomPort) {
        int lListenHandle = -1;
        int sessionID = -1;


        try {
            lListenHandle = startPlayBackListen(randomPort);
            if (lListenHandle == -1) {
                log.error("启动预览监听失败");
                return;
            }

            sessionID = RealPlay(lLoginID, lChannel, randomPort);
            if (sessionID == -1) {
                log.error("启动实时流失败");
                return;
            }

            DeviceCache stream = (DeviceCache) GlobalCacheService.getInstance().get(deviceId);
            stream.setSessionId(sessionID);
            GlobalCacheService.getInstance().put(deviceId, stream);
            log.info("sessionID: {}, lListenHandle: {}", sessionID, lListenHandle);

            // 等待30秒
            Thread.sleep(600 * 1000);

        } catch (InterruptedException e) {
            log.error("线程被中断", e);
            Thread.currentThread().interrupt(); // 恢复中断状态
        } catch (Exception e) {
            log.error("处理流时发生异常", e);
        } finally {
            // 确保资源被正确清理
            if (sessionID != -1) {
                StopRealPlay(lLoginID, sessionID, lListenHandle, lListenHandle, hikISUPStream);
            }
            log.info("保存流{}结束", deviceId);
        }
    }

    private int startPlayBackListen(String randomPort) {
        log.info("========================= 启动SMS =========================");
        NET_EHOME_LISTEN_PREVIEW_CFG netEhomeListenPreviewCfg = new NET_EHOME_LISTEN_PREVIEW_CFG();
        System.arraycopy(hikIsupProperties.getSmsServer().getListenIp().getBytes(), 0, netEhomeListenPreviewCfg.struIPAdress.szIP, 0, hikIsupProperties.getSmsServer().getListenIp().length());
        netEhomeListenPreviewCfg.struIPAdress.wPort = Short.parseShort(randomPort); //流媒体服务器监听端口

        netEhomeListenPreviewCfg.fnNewLinkCB = (lLinkHandle, pNewLinkCBMsg, pUserData) -> {
            //预览数据回调参数
            System.out.println("[lPreviewHandle 默认值 -1]预览数据回调参数:" + lLinkHandle);
            NET_EHOME_PREVIEW_DATA_CB_PARAM struDataCB = new NET_EHOME_PREVIEW_DATA_CB_PARAM();
            struDataCB.fnPreviewDataCB = (iPreviewHandle, pPreviewCBMsg, pud) -> {
//                log.info("预览数据回调, iPreviewHandle: {}, dwDataLen: {}", iPreviewHandle, pPreviewCBMsg.dwDataLen);
                long offset = 0;
                ByteBuffer buffers = pPreviewCBMsg.pRecvdata.getByteBuffer(offset, pPreviewCBMsg.dwDataLen);
                byte[] bytes = new byte[pPreviewCBMsg.dwDataLen];
                buffers.rewind();
                buffers.get(bytes);
                FileUtil.writeFile("./output/preview.mp4", bytes);
            };

            if (!this.hikISUPStream.NET_ESTREAM_SetPreviewDataCB(lLinkHandle, struDataCB)) {
                System.out.println("NET_ESTREAM_SetPreviewDataCB failed err:：" + this.hikISUPStream.NET_ESTREAM_GetLastError());
                return false;
            }
            return true;
        }; //预览连接请求回调函数
        netEhomeListenPreviewCfg.pUser = null;
        netEhomeListenPreviewCfg.byLinkMode = 0; //0- TCP方式，1- UDP方式
        netEhomeListenPreviewCfg.write();
        int lListenHandle = hikISUPStream.NET_ESTREAM_StartListenPreview(netEhomeListenPreviewCfg);
        log.info("lListenHandle: {}", lListenHandle);
        if (lListenHandle == -1) {
            hikISUPStream.NET_ESTREAM_Fini();
            log.error("流媒体预览监听启动失败, error code: {}", hikISUPStream.NET_ESTREAM_GetLastError());
        } else {
            String StreamListenInfo = new String(netEhomeListenPreviewCfg.struIPAdress.szIP).trim() + "_" + netEhomeListenPreviewCfg.struIPAdress.wPort;
            log.info("{}, 流媒体服务：流媒体预览监听启动成功", StreamListenInfo);
        }
        return lListenHandle;
    }

    /**
     * 开启预览
     *
     * @param lLoginID
     * @param lChannel
     * @return sessionID 会话id
     */
    public int RealPlay(int lLoginID, int lChannel, String randomPort) {
        int sessionID = -1; //预览sessionID
        NET_EHOME_PREVIEWINFO_IN_V11 struPreviewInV11 = new NET_EHOME_PREVIEWINFO_IN_V11();
        struPreviewInV11.iChannel = lChannel; //通道号
        struPreviewInV11.dwLinkMode = 0; //0- TCP方式，1- UDP方式
        struPreviewInV11.dwStreamType = 0; //码流类型：0- 主码流，1- 子码流, 2- 第三码流
        log.info("ip: {}, port: {}", hikIsupProperties.getSmsServer().getIp(), hikIsupProperties.getSmsServer().getPort());
        struPreviewInV11.struStreamSever.szIP = hikIsupProperties.getSmsServer().getIp().getBytes();//流媒体服务器IP地址,公网地址
        struPreviewInV11.struStreamSever.wPort = Short.parseShort(randomPort); //流媒体服务器端口，需要跟服务器启动监听端口一致
        struPreviewInV11.write();
        //预览请求
        NET_EHOME_PREVIEWINFO_OUT struPreviewOut = new NET_EHOME_PREVIEWINFO_OUT();
        boolean getRS = ihcisupcms.NET_ECMS_StartGetRealStreamV11(lLoginID, struPreviewInV11, struPreviewOut);
        log.info("NET_ECMS_StartGetRealStream 预览请求: {}", getRS);
        if (!getRS) {
            log.error("NET_ECMS_StartGetRealStream failed, error code: {}", ihcisupcms.NET_ECMS_GetLastError());
            return sessionID;
        } else {
            struPreviewOut.read();
            log.info("NET_ECMS_StartGetRealStream succeed, sessionID: {}", struPreviewOut.lSessionID);
            sessionID = struPreviewOut.lSessionID;
        }
        NET_EHOME_PUSHSTREAM_IN struPushInfoIn = new NET_EHOME_PUSHSTREAM_IN();
        struPushInfoIn.read();
        struPushInfoIn.dwSize = struPushInfoIn.size();
        struPushInfoIn.lSessionID = sessionID;
        struPushInfoIn.write();
        NET_EHOME_PUSHSTREAM_OUT struPushInfoOut = new NET_EHOME_PUSHSTREAM_OUT();
        struPushInfoOut.read();
        struPushInfoOut.dwSize = struPushInfoOut.size();
        struPushInfoOut.write();
        if (!ihcisupcms.NET_ECMS_StartPushRealStream(lLoginID, struPushInfoIn, struPushInfoOut)) {
            log.error("NET_ECMS_StartPushRealStream failed, error code: {}", ihcisupcms.NET_ECMS_GetLastError());
            return sessionID;
        } else {
            log.info("NET_ECMS_StartPushRealStream succeed, sessionID: {}", struPushInfoIn.lSessionID);
        }
        return sessionID;
    }

    /**
     * 停止预览,Stream服务停止实时流转发，CMS向设备发送停止预览请求
     */
    public void StopRealPlay(int lLoginID, int sessionID, int lPreviewHandle, int lListenHandle, IHikISUPStream hikISUPStream) {
        log.info("停止获取实时流");
        if (!ihcisupcms.NET_ECMS_StopGetRealStream(lLoginID, sessionID)) {
            log.error("NET_ECMS_StopGetRealStream failed,err = {}", ihcisupcms.NET_ECMS_GetLastError());
            return;
        }
        log.info("停止预览");
        if (!hikISUPStream.NET_ESTREAM_StopPreview(lPreviewHandle)) {
            log.error("NET_ESTREAM_StopPreview failed,err = {}", hikISUPStream.NET_ESTREAM_GetLastError());
            return;
        }
        log.info("停止监听预览");
        if (!hikISUPStream.NET_ESTREAM_StopListenPreview(lListenHandle)) {
            log.error("NET_ESTREAM_StopListenPreview failed,err = {}", ihcisupcms.NET_ECMS_GetLastError());
        }
    }
}
