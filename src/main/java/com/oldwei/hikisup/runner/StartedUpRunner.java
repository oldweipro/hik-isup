package com.oldwei.hikisup.runner;

import com.oldwei.hikisup.sdk.service.IHCISUPCMS;
import com.oldwei.hikisup.sdk.service.IHikISUPStream;
import com.oldwei.hikisup.sdk.service.impl.FPREVIEW_NEWLINK_CB_FILE;
import com.oldwei.hikisup.sdk.service.impl.FRegisterCallBack;
import com.oldwei.hikisup.sdk.structure.NET_EHOME_CMS_LISTEN_PARAM;
import com.oldwei.hikisup.sdk.structure.NET_EHOME_LISTEN_PREVIEW_CFG;
import com.oldwei.hikisup.util.PropertiesUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartedUpRunner implements ApplicationRunner, DisposableBean {

    private final PropertiesUtil propertiesUtil;
    private final IHCISUPCMS ihcisupcms;
    private final IHikISUPStream hikISUPStream;
    private final FRegisterCallBack fRegisterCallBack;
    private final FPREVIEW_NEWLINK_CB_FILE fnNewLinkCB;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("========================= 启动CMS =========================");
        NET_EHOME_CMS_LISTEN_PARAM struCMSListenPara = new NET_EHOME_CMS_LISTEN_PARAM();
        System.arraycopy(propertiesUtil.readValue("CmsServerIP").getBytes(), 0, struCMSListenPara.struAddress.szIP, 0, propertiesUtil.readValue("CmsServerIP").length());
        struCMSListenPara.struAddress.wPort = Short.parseShort(propertiesUtil.readValue("CmsServerPort"));
        struCMSListenPara.fnCB = fRegisterCallBack;
        struCMSListenPara.write();
        //启动监听，接收设备注册信息
        int CmsHandle = ihcisupcms.NET_ECMS_StartListen(struCMSListenPara);
        if (CmsHandle < 0) {
            ihcisupcms.NET_ECMS_Fini();
            log.error("NET_ECMS_StartListen failed, error code: {}", ihcisupcms.NET_ECMS_GetLastError());
        } else {
            String CmsListenInfo = new String(struCMSListenPara.struAddress.szIP).trim() + "_" + struCMSListenPara.struAddress.wPort;
            log.info("register service: {}, NET_ECMS_StartListen succeed!", CmsListenInfo);
        }
        log.info("========================= 启动SMS =========================");
        NET_EHOME_LISTEN_PREVIEW_CFG netEhomeListenPreviewCfg = new NET_EHOME_LISTEN_PREVIEW_CFG();
        System.arraycopy(propertiesUtil.readValue("SmsServerListenIP").getBytes(), 0, netEhomeListenPreviewCfg.struIPAdress.szIP, 0, propertiesUtil.readValue("SmsServerListenIP").length());
        netEhomeListenPreviewCfg.struIPAdress.wPort = Short.parseShort(propertiesUtil.readValue("SmsServerListenPort")); //流媒体服务器监听端口
        netEhomeListenPreviewCfg.fnNewLinkCB = fnNewLinkCB; //预览连接请求回调函数
        netEhomeListenPreviewCfg.pUser = null;
        netEhomeListenPreviewCfg.byLinkMode = 0; //0- TCP方式，1- UDP方式
        netEhomeListenPreviewCfg.write();
        int StreamHandle = hikISUPStream.NET_ESTREAM_StartListenPreview(netEhomeListenPreviewCfg);
        if (StreamHandle == -1) {
            hikISUPStream.NET_ESTREAM_Fini();
            log.error("流媒体预览监听启动 失败, error code: {}", hikISUPStream.NET_ESTREAM_GetLastError());
        } else {
            String StreamListenInfo = new String(netEhomeListenPreviewCfg.struIPAdress.szIP).trim() + "_" + netEhomeListenPreviewCfg.struIPAdress.wPort;
            log.info("{}, 流媒体服务：流媒体预览监听启动 成功", StreamListenInfo);
        }
        log.info("=========================项目启动完成=========================");
    }

    @Override
    public void destroy() {
        hikISUPStream.NET_ESTREAM_Fini();
        ihcisupcms.NET_ECMS_Fini();
    }
}
