package com.oldwei.isup.runner;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.oldwei.isup.config.HikIsupProperties;
import com.oldwei.isup.mapper.DeviceMapper;
import com.oldwei.isup.model.Device;
import com.oldwei.isup.sdk.service.*;
import com.oldwei.isup.sdk.service.impl.FPREVIEW_NEWLINK_CB_FILE;
import com.oldwei.isup.sdk.service.impl.FRegisterCallBack;
import com.oldwei.isup.sdk.structure.*;
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
    private final HCISUPCMS hcisupcms;
    private final IHikISUPStream hikISUPStream;
    private final FRegisterCallBack fRegisterCallBack;
    private final FPREVIEW_NEWLINK_CB_FILE fnNewLinkCB;
    private final DeviceMapper deviceMapper;
    private final VOICETALK_NEWLINK_CB voiceCallBack;
    private final IHikISUPStorage hikISUPStorage;
    private final IHikISUPAlarm hikISUPAlarm;
    private final EHomeSSStorageCallBack pssStorageCallback;
    private final EHomeSSMsgCallBack pssMessageCallback;
    private final EHomeMsgCallBack alarmMsgCallBack;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        log.info("=========================  开启存储服务监听  =========================");
        //设置图片存储服务器公网地址 （当存在内外网映射时使用
        NET_EHOME_IPADDRESS ipaddress = new NET_EHOME_IPADDRESS();
        String ServerIP = hikIsupProperties.getPicServer().getIp();
        System.arraycopy(ServerIP.getBytes(), 0, ipaddress.szIP, 0, ServerIP.length());
        ipaddress.wPort = Short.parseShort(hikIsupProperties.getPicServer().getPort());
        ipaddress.write();
        boolean b = hikISUPStorage.NET_ESS_SetSDKInitCfg(3, ipaddress.getPointer());
        if (!b) {
            System.out.println("NET_ESS_SetSDKInitCfg失败，错误码：" + hikISUPStorage.NET_ESS_GetLastError());
        }
        NET_EHOME_SS_LISTEN_PARAM pSSListenParam = new NET_EHOME_SS_LISTEN_PARAM();
        String SSIP = hikIsupProperties.getPicServer().getListenIp();
        System.arraycopy(SSIP.getBytes(), 0, pSSListenParam.struAddress.szIP, 0, SSIP.length());
        pSSListenParam.struAddress.wPort = Short.parseShort(hikIsupProperties.getPicServer().getListenPort());
        // TODO 存储服务参数
        String strKMS_UserName = "test";
        System.arraycopy(strKMS_UserName.getBytes(), 0, pSSListenParam.szKMS_UserName, 0, strKMS_UserName.length());
        String strKMS_Password = "12345";
        System.arraycopy(strKMS_Password.getBytes(), 0, pSSListenParam.szKMS_Password, 0, strKMS_Password.length());
        String strAccessKey = "test";
        System.arraycopy(strAccessKey.getBytes(), 0, pSSListenParam.szAccessKey, 0, strAccessKey.length());
        String strSecretKey = "12345";
        System.arraycopy(strSecretKey.getBytes(), 0, pSSListenParam.szSecretKey, 0, strSecretKey.length());
        pSSListenParam.byHttps = 0;
        /******************************************************************
         * 存储信息回调
         */
        pSSListenParam.fnSSMsgCb = pssMessageCallback;

        /******************************************************************
         * 存储数据回调
         * fnSStorageCb或者fnSSRWCbEx，只需要设置一种回调函数
         * 简单功能测试可以使用存储回调(SDK底层使用db数据库自动存取数据，因此会受到db数据库的性能瓶颈影响)
         * 需要自定义URL或者自己读写图片数据，则使用读写扩展回调(推荐)
         */
        //存储信息回调
        pSSListenParam.fnSStorageCb = pssStorageCallback;
        //读写扩展回调
//        if (fEHomeSSRWCallBackEx == null) {
//            fEHomeSSRWCallBackEx = new cbEHomeSSRWCallBackEx();
//        }
//        pSSListenParam.fnSSRWCbEx = fEHomeSSRWCallBackEx;
        pSSListenParam.bySecurityMode = 1;
        pSSListenParam.write();
        int SsHandle = hikISUPStorage.NET_ESS_StartListen(pSSListenParam);
        if (SsHandle == -1) {
            int err = hikISUPStorage.NET_ESS_GetLastError();
            System.out.println("NET_ESS_StartListen failed,error:" + err);
            hikISUPStorage.NET_ESS_Fini();
            return;
        } else {
            String SsListenInfo = new String(pSSListenParam.struAddress.szIP).trim() + "_" + pSSListenParam.struAddress.wPort;
            System.out.println("存储服务器：" + SsListenInfo + ",NET_ESS_StartListen succeed!\n");
        }

        log.info("=========================  开启报警服务监听  =========================");
        NET_EHOME_ALARM_LISTEN_PARAM net_ehome_alarm_listen_param = new NET_EHOME_ALARM_LISTEN_PARAM();
        System.arraycopy(hikIsupProperties.getAlarmServer().getListenIp().getBytes(), 0, net_ehome_alarm_listen_param.struAddress.szIP,
                0, hikIsupProperties.getAlarmServer().getListenIp().length());
        if (Short.parseShort(hikIsupProperties.getAlarmServer().getType()) == 2) {
            net_ehome_alarm_listen_param.struAddress.wPort = Short.parseShort(hikIsupProperties.getAlarmServer().getListenTcpPort());
            net_ehome_alarm_listen_param.byProtocolType = 2; //协议类型：0- TCP，1- UDP, 2-MQTT
        } else {
            net_ehome_alarm_listen_param.struAddress.wPort = Short.parseShort(hikIsupProperties.getAlarmServer().getListenUdpPort());
            net_ehome_alarm_listen_param.byProtocolType = 1; //协议类型：0- TCP，1- UDP, 2-MQTT
        }
        net_ehome_alarm_listen_param.fnMsgCb = alarmMsgCallBack;
        net_ehome_alarm_listen_param.byUseCmsPort = 0; //是否复用CMS端口：0- 不复用，非0- 复用
        net_ehome_alarm_listen_param.write();

        //启动报警服务器监听
        int AlarmHandle = -1;
        AlarmHandle = hikISUPAlarm.NET_EALARM_StartListen(net_ehome_alarm_listen_param);
        System.out.println("AlarmHandle: " + AlarmHandle);
        if (AlarmHandle < 0) {
            System.out.println("NET_EALARM_StartListen failed, error:" + hikISUPAlarm.NET_EALARM_GetLastError());
            hikISUPAlarm.NET_EALARM_Fini();
            return;
        } else {
            String AlarmListenInfo = new String(net_ehome_alarm_listen_param.struAddress.szIP).trim() + "_" + net_ehome_alarm_listen_param.struAddress.wPort;
            System.out.println("报警服务器：" + AlarmListenInfo + ",NET_EALARM_StartListen succeed");
        }

        log.info("========================= 重置设备状态 =========================");
        Long count = deviceMapper.selectCount(new LambdaQueryWrapper<>());
        if (count > 0) {
            boolean updated = new LambdaUpdateChainWrapper<>(deviceMapper)
                    .set(Device::getIsOnline, 0)
                    .set(Device::getLoginId, -1)
                    .set(Device::getChannel, -1)
                    .set(Device::getIsPush, -1)
                    .set(Device::getPreviewHandle, -1)
                    .set(Device::getPreviewListenHandle, -1)
                    .set(Device::getPreviewSessionId, -1)
                    .update();
            if (updated) {
                log.info("重置设备状态完成");
            } else {
                log.warn("重置设备状态失败");
                // 退出程序
                System.exit(1);
            }
        }
        log.info("========================= 启动CMS =========================");
        NET_EHOME_CMS_LISTEN_PARAM struCMSListenPara = new NET_EHOME_CMS_LISTEN_PARAM();
        System.arraycopy(hikIsupProperties.getCmsServer().getIp().getBytes(), 0, struCMSListenPara.struAddress.szIP, 0, hikIsupProperties.getCmsServer().getIp().length());
        struCMSListenPara.struAddress.wPort = Short.parseShort(hikIsupProperties.getCmsServer().getPort());
        struCMSListenPara.fnCB = fRegisterCallBack;
        struCMSListenPara.write();
        //启动监听，接收设备注册信息
        int CmsHandle = hcisupcms.NET_ECMS_StartListen(struCMSListenPara);
        if (CmsHandle < 0) {
            hcisupcms.NET_ECMS_Fini();
            log.error("NET_ECMS_StartListen failed, error code: {}", hcisupcms.NET_ECMS_GetLastError());
        } else {
            String CmsListenInfo = new String(struCMSListenPara.struAddress.szIP).trim() + "_" + struCMSListenPara.struAddress.wPort;
            log.info("register service: {}, NET_ECMS_StartListen succeed!", CmsListenInfo);
        }

        log.info("=========================  流媒体服务(需要预览取流时使用)  =========================");
        NET_EHOME_LISTEN_PREVIEW_CFG netEhomeListenPreviewCfg = new NET_EHOME_LISTEN_PREVIEW_CFG();
        System.arraycopy(hikIsupProperties.getSmsServer().getListenIp().getBytes(), 0, netEhomeListenPreviewCfg.struIPAdress.szIP, 0, hikIsupProperties.getSmsServer().getListenIp().length());
        netEhomeListenPreviewCfg.struIPAdress.wPort = Short.parseShort(hikIsupProperties.getSmsServer().getListenPort()); //流媒体服务器监听端口

        netEhomeListenPreviewCfg.fnNewLinkCB = fnNewLinkCB; //预览连接请求回调函数
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

        log.info("=========================  开启语音流媒体服务监听  =========================");
        NET_EHOME_LISTEN_VOICETALK_CFG net_ehome_listen_voicetalk_cfg = new NET_EHOME_LISTEN_VOICETALK_CFG();
        net_ehome_listen_voicetalk_cfg.struIPAdress.szIP = hikIsupProperties.getVoiceSmsServer().getListenIp().getBytes();
        net_ehome_listen_voicetalk_cfg.struIPAdress.wPort = Short.parseShort(hikIsupProperties.getVoiceSmsServer().getPort());
        net_ehome_listen_voicetalk_cfg.fnNewLinkCB = voiceCallBack;
        net_ehome_listen_voicetalk_cfg.byLinkMode = 0;
        net_ehome_listen_voicetalk_cfg.write();
        int VoicelServHandle = hikISUPStream.NET_ESTREAM_StartListenVoiceTalk(net_ehome_listen_voicetalk_cfg);
        if (VoicelServHandle == -1) {
            System.out.println("VoiceDemo NET_ESTREAM_StartListenPreview failed, error code:" + hikISUPStream.NET_ESTREAM_GetLastError());
            hikISUPStream.NET_ESTREAM_Fini();
            return;
        } else {
            String VoiceStreamListenInfo = new String(net_ehome_listen_voicetalk_cfg.struIPAdress.szIP).trim() + "_" + net_ehome_listen_voicetalk_cfg.struIPAdress.wPort;
            System.out.println("语音流媒体服务：" + VoiceStreamListenInfo + ",NET_ESTREAM_StartListenVoiceTalk succeed, " + voiceCallBack);
        }
        log.info("========================= 项目启动完成 =========================");
    }

    @Override
    public void destroy() {
        hikISUPStream.NET_ESTREAM_Fini();
        hcisupcms.NET_ECMS_Fini();
        hikISUPAlarm.NET_EALARM_Fini();
        hikISUPStorage.NET_ESS_Fini();
    }
}
