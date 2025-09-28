package com.oldwei.hikisup.runner;

import com.oldwei.hikisup.config.HikIsupProperties;
import com.oldwei.hikisup.sdk.service.IHCISUPCMS;
import com.oldwei.hikisup.sdk.service.IHikISUPStream;
import com.oldwei.hikisup.sdk.service.impl.FPREVIEW_NEWLINK_CB_FILE;
import com.oldwei.hikisup.sdk.service.impl.FRegisterCallBack;
import com.oldwei.hikisup.sdk.structure.NET_EHOME_CMS_LISTEN_PARAM;
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

    private final HikIsupProperties hikIsupProperties;
    private final IHCISUPCMS ihcisupcms;
    private final IHikISUPStream hikISUPStream;
    private final FRegisterCallBack fRegisterCallBack;
    private final FPREVIEW_NEWLINK_CB_FILE fnNewLinkCB;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("========================= 启动CMS =========================");
        NET_EHOME_CMS_LISTEN_PARAM struCMSListenPara = new NET_EHOME_CMS_LISTEN_PARAM();
        System.arraycopy(hikIsupProperties.getCmsServer().getIp().getBytes(), 0, struCMSListenPara.struAddress.szIP, 0, hikIsupProperties.getCmsServer().getIp().length());
        struCMSListenPara.struAddress.wPort = Short.parseShort(hikIsupProperties.getCmsServer().getPort());
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
        log.info("=========================项目启动完成=========================");
    }

    @Override
    public void destroy() {
        hikISUPStream.NET_ESTREAM_Fini();
        ihcisupcms.NET_ECMS_Fini();
    }
}
