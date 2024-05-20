package com.oldwei.hikisup.sdk.service.impl;

import com.oldwei.hikisup.domain.DeviceCache;
import com.oldwei.hikisup.sdk.SdkService.AlarmService.AlarmDemo;
import com.oldwei.hikisup.sdk.SdkService.CmsService.HCISUPCMS;
import com.oldwei.hikisup.sdk.service.DEVICE_REGISTER_CB;
import com.oldwei.hikisup.sdk.service.IHCISUPCMS;
import com.oldwei.hikisup.util.GlobalCacheService;
import com.oldwei.hikisup.util.PropertiesUtil;
import com.sun.jna.Pointer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FRegisterCallBack implements DEVICE_REGISTER_CB {
    private final PropertiesUtil propertiesUtil;
    private final IHCISUPCMS ihcisupcms;
    @Override
    public boolean invoke(int lUserID, int dwDataType, Pointer pOutBuffer, int dwOutLen, Pointer pInBuffer, int dwInLen, Pointer pUser) {
        System.out.println("FRegisterCallBack, dwDataType:" + dwDataType + ", lUserID:" + lUserID);
        switch (dwDataType) {
            case HCISUPCMS.EHOME_REGISTER_TYPE.ENUM_DEV_ON:  //ENUM_DEV_ON
                HCISUPCMS.NET_EHOME_DEV_REG_INFO_V12 strDevRegInfo = new HCISUPCMS.NET_EHOME_DEV_REG_INFO_V12();
                strDevRegInfo.write();
                Pointer pDevRegInfo = strDevRegInfo.getPointer();
                pDevRegInfo.write(0, pOutBuffer.getByteArray(0, strDevRegInfo.size()), 0, strDevRegInfo.size());
                strDevRegInfo.read();
                HCISUPCMS.NET_EHOME_SERVER_INFO_V50 strEhomeServerInfo = new HCISUPCMS.NET_EHOME_SERVER_INFO_V50();
                strEhomeServerInfo.read();
                //strEhomeServerInfo.dwSize = strEhomeServerInfo.size();
                byte[] byCmsIP = new byte[0];
                //设置报警服务器地址、端口、类型
                byCmsIP = propertiesUtil.readValue("AlarmServerIP").getBytes();
                System.arraycopy(byCmsIP, 0, strEhomeServerInfo.struUDPAlarmSever.szIP, 0, byCmsIP.length);
                System.arraycopy(byCmsIP, 0, strEhomeServerInfo.struTCPAlarmSever.szIP, 0, byCmsIP.length);
                //报警服务器类型：0- 只支持UDP协议上报，1- 支持UDP、TCP两种协议上报 2-MQTT

                strEhomeServerInfo.dwAlarmServerType = Integer.parseInt(propertiesUtil.readValue("AlarmServerType"));
                strEhomeServerInfo.struTCPAlarmSever.wPort = Short.parseShort(propertiesUtil.readValue("AlarmServerTCPPort"));
                strEhomeServerInfo.struUDPAlarmSever.wPort = Short.parseShort(propertiesUtil.readValue("AlarmServerUDPPort"));

                byte[] byClouldAccessKey = "test".getBytes();
                System.arraycopy(byClouldAccessKey, 0, strEhomeServerInfo.byClouldAccessKey, 0, byClouldAccessKey.length);
                byte[] byClouldSecretKey = "12345".getBytes();
                System.arraycopy(byClouldSecretKey, 0, strEhomeServerInfo.byClouldSecretKey, 0, byClouldSecretKey.length);
                strEhomeServerInfo.dwClouldPoolId = 1;

                //设置图片存储服务器地址、端口、类型
                byte[] bySSIP = new byte[0];
                bySSIP = propertiesUtil.readValue("PicServerIP").getBytes();
                System.arraycopy(bySSIP, 0, strEhomeServerInfo.struPictureSever.szIP, 0, bySSIP.length);
                strEhomeServerInfo.struPictureSever.wPort = Short.parseShort(propertiesUtil.readValue("PicServerPort"));
                strEhomeServerInfo.dwPicServerType = Integer.parseInt(propertiesUtil.readValue("PicServerType"));    //存储服务器（SS）类型：0-Tomcat，1-VRB，2-云存储，3-KMS，4-ISUP5.0。
                strEhomeServerInfo.write();
                dwInLen = strEhomeServerInfo.size();
                pInBuffer.write(0, strEhomeServerInfo.getPointer().getByteArray(0, dwInLen), 0, dwInLen);

                // FIXME demo逻辑中默认只支持一台设备的功能演示，多台设备需要自行调整这里设备登录后的句柄信息
                String trim = new String(strDevRegInfo.struRegInfo.byDeviceID).trim();
                System.out.println("Device online, DeviceID is:" + trim);
                DeviceCache deviceCache = new DeviceCache();
                deviceCache.setLLoginID(lUserID);
                deviceCache.setDeviceId(trim);
                GlobalCacheService.getInstance().put(trim, deviceCache);
                log.info("{}", deviceCache);

                return true;
            case HCISUPCMS.EHOME_REGISTER_TYPE.ENUM_DEV_AUTH: //ENUM_DEV_AUTH
                strDevRegInfo = new HCISUPCMS.NET_EHOME_DEV_REG_INFO_V12();
                strDevRegInfo.write();
                pDevRegInfo = strDevRegInfo.getPointer();
                pDevRegInfo.write(0, pOutBuffer.getByteArray(0, strDevRegInfo.size()), 0, strDevRegInfo.size());
                strDevRegInfo.read();
                byte[] bs = new byte[0];
                String szEHomeKey = propertiesUtil.readValue("ISUPKey"); //ISUP5.0登录校验值
                bs = szEHomeKey.getBytes();
                pInBuffer.write(0, bs, 0, szEHomeKey.length());
                break;
            case HCISUPCMS.EHOME_REGISTER_TYPE.ENUM_DEV_SESSIONKEY: //HCISUPCMS.ENUM_DEV_SESSIONKEY
                strDevRegInfo = new HCISUPCMS.NET_EHOME_DEV_REG_INFO_V12();
                strDevRegInfo.write();
                pDevRegInfo = strDevRegInfo.getPointer();
                pDevRegInfo.write(0, pOutBuffer.getByteArray(0, strDevRegInfo.size()), 0, strDevRegInfo.size());
                strDevRegInfo.read();
                HCISUPCMS.NET_EHOME_DEV_SESSIONKEY struSessionKey = new HCISUPCMS.NET_EHOME_DEV_SESSIONKEY();
                System.arraycopy(strDevRegInfo.struRegInfo.byDeviceID, 0, struSessionKey.sDeviceID, 0, strDevRegInfo.struRegInfo.byDeviceID.length);
                System.arraycopy(strDevRegInfo.struRegInfo.bySessionKey, 0, struSessionKey.sSessionKey, 0, strDevRegInfo.struRegInfo.bySessionKey.length);
                struSessionKey.write();
                Pointer pSessionKey = struSessionKey.getPointer();
                ihcisupcms.NET_ECMS_SetDeviceSessionKey(pSessionKey);
//             TODO   AlarmDemo.hcEHomeAlarm.NET_EALARM_SetDeviceSessionKey(pSessionKey);
                break;
            case HCISUPCMS.EHOME_REGISTER_TYPE.ENUM_DEV_DAS_REQ: //HCISUPCMS.ENUM_DEV_DAS_REQ
                String dasInfo = "{\n" +
                        "    \"Type\":\"DAS\",\n" +
                        "    \"DasInfo\": {\n" +
                        "        \"Address\":\"" + propertiesUtil.readValue("DasServerIP") + "\",\n" +
                        "        \"Domain\":\"\",\n" +
                        "        \"ServerID\":\"\",\n" +
                        "        \"Port\":" + propertiesUtil.readValue("DasServerPort") + ",\n" +
                        "        \"UdpPort\":\n" +
                        "    }\n" +
                        "}";
                System.out.println(dasInfo);
                byte[] bs1 = dasInfo.getBytes();
                pInBuffer.write(0, bs1, 0, dasInfo.length());
                break;
            default:
                System.out.println("FRegisterCallBack default type:" + dwDataType);
                break;
        }
        return true;
    }
}
