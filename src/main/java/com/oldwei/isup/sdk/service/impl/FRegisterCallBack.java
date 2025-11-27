package com.oldwei.isup.sdk.service.impl;

import com.oldwei.isup.config.HikIsupProperties;
import com.oldwei.isup.model.Device;
import com.oldwei.isup.sdk.isapi.ISAPIService;
import com.oldwei.isup.sdk.service.DEVICE_REGISTER_CB;
import com.oldwei.isup.sdk.service.HCISUPCMS;
import com.oldwei.isup.sdk.service.IHikISUPAlarm;
import com.oldwei.isup.sdk.service.constant.EHOME_REGISTER_TYPE;
import com.oldwei.isup.sdk.structure.NET_EHOME_DEV_REG_INFO_V12;
import com.oldwei.isup.sdk.structure.NET_EHOME_DEV_SESSIONKEY;
import com.oldwei.isup.sdk.structure.NET_EHOME_SERVER_INFO_V50;
import com.oldwei.isup.service.DeviceCacheService;
import com.sun.jna.Pointer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FRegisterCallBack implements DEVICE_REGISTER_CB {
    private final HikIsupProperties hikIsupProperties;
    private final HCISUPCMS hcisupcms;
    private final DeviceCacheService deviceCacheService;
    private final IHikISUPAlarm hikISUPAlarm;
    private final ISAPIService isapiService;
    private final CmsUtil cmsUtil;

    @Override
    public boolean invoke(int lUserID, int dwDataType, Pointer pOutBuffer, int dwOutLen, Pointer pInBuffer, int dwInLen, Pointer pUser) {
        log.info("设备注册状态回调 FRegisterCallBack, dwDataType: {}, lUserID: {}", dwDataType, lUserID);
        NET_EHOME_DEV_REG_INFO_V12 strDevRegInfo = new NET_EHOME_DEV_REG_INFO_V12();
        Pointer pDevRegInfo = strDevRegInfo.getPointer();
        switch (dwDataType) {
            case EHOME_REGISTER_TYPE.ENUM_DEV_ON: // 0
                // 设备上线回调
                strDevRegInfo.write();
                pDevRegInfo.write(0, pOutBuffer.getByteArray(0, strDevRegInfo.size()), 0, strDevRegInfo.size());
                strDevRegInfo.read();
//                log.info("""
//                                设备注册信息
//                                dwSize: {}
//                                dwNetUnitType: {}
//                                byDeviceID: {}
//                                byFirmwareVersion: {}
//                                struDevAdd.szIP: {}
//                                struDevAdd.wPort: {}
//                                dwDevType: {}
//                                dwManufacture: {}
//                                byPassWord: {}
//                                sDeviceSerial: {}
//                                byReliableTransmission: {}
//                                byWebSocketTransmission: {}
//                                bySupportRedirect: {}
//                                byDevProtocolVersion (hex): {}
//                                bySessionKey (hex): {}
//                                byMarketType: {}
//                                """,
//                        strDevRegInfo.struRegInfo.dwSize,
//                        strDevRegInfo.struRegInfo.dwNetUnitType,
//                        new String(strDevRegInfo.struRegInfo.byDeviceID).trim(),
//                        new String(strDevRegInfo.struRegInfo.byFirmwareVersion).trim(),
//                        new String(strDevRegInfo.struRegInfo.struDevAdd.szIP).trim(),
//                        strDevRegInfo.struRegInfo.struDevAdd.wPort,
//                        strDevRegInfo.struRegInfo.dwDevType,
//                        strDevRegInfo.struRegInfo.dwManufacture,
//                        new String(strDevRegInfo.struRegInfo.byPassWord).trim(),
//                        new String(strDevRegInfo.struRegInfo.sDeviceSerial).trim(),
//                        strDevRegInfo.struRegInfo.byReliableTransmission,
//                        strDevRegInfo.struRegInfo.byWebSocketTransmission,
//                        strDevRegInfo.struRegInfo.bySupportRedirect,
//                        new String(strDevRegInfo.struRegInfo.byDevProtocolVersion),
//                        new String(strDevRegInfo.struRegInfo.bySessionKey),
//                        strDevRegInfo.struRegInfo.byMarketType
//                );
//                log.info("设备注册地址: {}", strDevRegInfo.struRegAddr.toString());
                NET_EHOME_SERVER_INFO_V50 strEhomeServerInfo = new NET_EHOME_SERVER_INFO_V50();
                strEhomeServerInfo.read();
                //strEhomeServerInfo.dwSize = strEhomeServerInfo.size();
                //设置报警服务器地址、端口、类型
                byte[] byCmsIP = hikIsupProperties.getAlarmServer().getIp().getBytes();
                System.arraycopy(byCmsIP, 0, strEhomeServerInfo.struUDPAlarmSever.szIP, 0, byCmsIP.length);
                System.arraycopy(byCmsIP, 0, strEhomeServerInfo.struTCPAlarmSever.szIP, 0, byCmsIP.length);
                //报警服务器类型：0- 只支持UDP协议上报，1- 支持UDP、TCP两种协议上报 2-MQTT

                strEhomeServerInfo.dwAlarmServerType = Integer.parseInt(hikIsupProperties.getAlarmServer().getType());
                strEhomeServerInfo.struTCPAlarmSever.wPort = Short.parseShort(hikIsupProperties.getAlarmServer().getTcpPort());
                strEhomeServerInfo.struUDPAlarmSever.wPort = Short.parseShort(hikIsupProperties.getAlarmServer().getUdpPort());

                byte[] byClouldAccessKey = "test".getBytes();
                System.arraycopy(byClouldAccessKey, 0, strEhomeServerInfo.byClouldAccessKey, 0, byClouldAccessKey.length);
                byte[] byClouldSecretKey = "12345".getBytes();
                System.arraycopy(byClouldSecretKey, 0, strEhomeServerInfo.byClouldSecretKey, 0, byClouldSecretKey.length);
                strEhomeServerInfo.dwClouldPoolId = 1;

                //设置图片存储服务器地址、端口、类型
                byte[] bySSIP = hikIsupProperties.getPicServer().getIp().getBytes();
                System.arraycopy(bySSIP, 0, strEhomeServerInfo.struPictureSever.szIP, 0, bySSIP.length);
                strEhomeServerInfo.struPictureSever.wPort = Short.parseShort(hikIsupProperties.getPicServer().getPort());
                strEhomeServerInfo.dwPicServerType = Integer.parseInt(hikIsupProperties.getPicServer().getType());    //存储服务器（SS）类型：0-Tomcat，1-VRB，2-云存储，3-KMS，4-ISUP5.0。
                strEhomeServerInfo.write();
                dwInLen = strEhomeServerInfo.size();
                pInBuffer.write(0, strEhomeServerInfo.getPointer().getByteArray(0, dwInLen), 0, dwInLen);

                String deviceId = new String(strDevRegInfo.struRegInfo.byDeviceID).trim();
                log.info("设备上线, DeviceID: {}, LoginID: {}", deviceId, lUserID);
                // 注册登录句柄映射
                deviceCacheService.registerLoginId(lUserID, deviceId);
                // 获取或创建设备对象
                Device device = deviceCacheService.getByDeviceId(deviceId).orElse(new Device());
                device.setDeviceId(deviceId);
//                device.setDeviceType(deviceType);
                device.setIsOnline(1);
                device.setLoginId(lUserID);
                // 更新通道列表
//                updateDeviceChannels(device, onlineChannelIds);
                deviceCacheService.saveOrUpdate(device);
                break;
            case EHOME_REGISTER_TYPE.ENUM_DEV_OFF:// TODO 1
                log.info("设备下线回调 Device off, lUserID is: {}", lUserID);
                Optional<Device> deviceOpt = deviceCacheService.getByLoginId(lUserID);
                if (deviceOpt.isEmpty()) {
                    log.warn("未找到登录句柄{}对应的设备", lUserID);
                } else {
                    Device deviceOffline = deviceOpt.get();
                    deviceCacheService.removeByLoginId(lUserID);
                    log.info("设备{}下线，影响{}个通道", deviceOffline.getDeviceId(), deviceOffline.getChannels().size());
                    // TODO 如果设备正在预览，停止该设备的所有预览流
                }
                break;
            case EHOME_REGISTER_TYPE.ENUM_DEV_AUTH:// 3
                // Ehome5.0设备认证回调
                strDevRegInfo.write();
                pDevRegInfo.write(0, pOutBuffer.getByteArray(0, strDevRegInfo.size()), 0, strDevRegInfo.size());
                strDevRegInfo.read();
                String szEHomeKey = hikIsupProperties.getIsupKey(); //ISUP5.0登录校验值
                byte[] bs = szEHomeKey.getBytes();
                pInBuffer.write(0, bs, 0, szEHomeKey.length());
                log.info("Ehome5.0设备认证回调 Device auth, DeviceID is: {}", new String(strDevRegInfo.struRegInfo.byDeviceID).trim());
                break;
            case EHOME_REGISTER_TYPE.ENUM_DEV_SESSIONKEY:// 4
                // Ehome5.0设备Sessionkey回调
                strDevRegInfo.write();
                pDevRegInfo.write(0, pOutBuffer.getByteArray(0, strDevRegInfo.size()), 0, strDevRegInfo.size());
                strDevRegInfo.read();
                NET_EHOME_DEV_SESSIONKEY struSessionKey = new NET_EHOME_DEV_SESSIONKEY();
                System.arraycopy(strDevRegInfo.struRegInfo.byDeviceID, 0, struSessionKey.sDeviceID, 0, strDevRegInfo.struRegInfo.byDeviceID.length);
                System.arraycopy(strDevRegInfo.struRegInfo.bySessionKey, 0, struSessionKey.sSessionKey, 0, strDevRegInfo.struRegInfo.bySessionKey.length);
                struSessionKey.write();
                Pointer pSessionKey = struSessionKey.getPointer();
                hcisupcms.NET_ECMS_SetDeviceSessionKey(pSessionKey);
                log.info("Ehome5.0设备Sessionkey回调 Device session key, DeviceID is: {}", new String(strDevRegInfo.struRegInfo.byDeviceID).trim());
                hikISUPAlarm.NET_EALARM_SetDeviceSessionKey(pSessionKey);
                break;
            case EHOME_REGISTER_TYPE.ENUM_DEV_DAS_REQ: // 5 HCISUPCMS.ENUM_DEV_DAS_REQ
                String dasInfo = "{\n" +
                        "    \"Type\":\"DAS\",\n" +
                        "    \"DasInfo\": {\n" +
                        "        \"Address\":\"" + hikIsupProperties.getDasServer().getIp() + "\",\n" +
                        "        \"Domain\":\"\",\n" +
                        "        \"ServerID\":\"\",\n" +
                        "        \"Port\":" + hikIsupProperties.getDasServer().getPort() + ",\n" +
                        "        \"UdpPort\":\n" +
                        "    }\n" +
                        "}";
                byte[] bs1 = dasInfo.getBytes();
                pInBuffer.write(0, bs1, 0, dasInfo.length());
                log.info("Ehome5.0设备DAS请求回调 Device DAS request: {}", dasInfo);
                break;
            default:
                log.info("FRegisterCallBack default type: {}", dwDataType);
                break;
        }
        return true;
    }
}
