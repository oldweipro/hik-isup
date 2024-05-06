package com.oldwei.hikisup.sdk.SdkService.AlarmService;

import com.oldwei.hikisup.util.Alarm.AlarmEventHandle;
import com.oldwei.hikisup.util.CommonMethod;
import com.oldwei.hikisup.util.PropertiesUtil;
import com.oldwei.hikisup.util.OsSelect;
import com.oldwei.hikisup.sdk.SdkService.CmsService.HCISUPCMS;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import java.io.IOException;

public class AlarmDemo {
    public static HCISUPAlarm hcEHomeAlarm = null;
    public static int AlarmHandle = -1; //Alarm监听句柄
    static EHomeMsgCallBack cbEHomeMsgCallBack;//报警监听回调函数实现
    static HCISUPAlarm.NET_EHOME_ALARM_LISTEN_PARAM net_ehome_alarm_listen_param = new HCISUPAlarm.NET_EHOME_ALARM_LISTEN_PARAM();
    static String configPath = "./config.properties";
    PropertiesUtil propertiesUtil;

    {
        try {
            propertiesUtil = new PropertiesUtil(configPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据不同操作系统选择不同的库文件和库路径
     *
     * @return
     */
    private static boolean CreateSDKInstance() {
        if (hcEHomeAlarm == null) {
            synchronized (HCISUPAlarm.class) {
                String strDllPath = "";
                try {
                    //System.setProperty("jna.debug_load", "true");
                    if (OsSelect.isWindows())
                        //win系统加载库路径(路径不要带中文)
                        strDllPath = System.getProperty("user.dir") + "\\sdk\\windows\\HCISUPAlarm.dll";
                    else if (OsSelect.isLinux())
                        //Linux系统加载库路径(路径不要带中文)
                        strDllPath = System.getProperty("user.dir") + "/sdk/linux/libHCISUPAlarm.so";
                    hcEHomeAlarm = (HCISUPAlarm) Native.loadLibrary(strDllPath, HCISUPAlarm.class);
                } catch (Exception ex) {
                    System.out.println("loadLibrary: " + strDllPath + " Error: " + ex.getMessage());
                    return false;
                }
            }
        }
        return true;
    }

    public void eAlarm_Init() {
        if (hcEHomeAlarm == null) {
            if (!CreateSDKInstance()) {
                System.out.println("Load CMS SDK fail");
                return;
            }
        }
        if (OsSelect.isWindows()) {
            HCISUPCMS.BYTE_ARRAY ptrByteArrayCrypto = new HCISUPCMS.BYTE_ARRAY(256);
            String strPathCrypto = System.getProperty("user.dir") + "\\sdk\\windows\\libeay32.dll"; //Linux版本是libcrypto.so库文件的路径
            System.arraycopy(strPathCrypto.getBytes(), 0, ptrByteArrayCrypto.byValue, 0, strPathCrypto.length());
            ptrByteArrayCrypto.write();
            hcEHomeAlarm.NET_EALARM_SetSDKInitCfg(0, ptrByteArrayCrypto.getPointer());

            //设置libssl.so所在路径
            HCISUPCMS.BYTE_ARRAY ptrByteArraySsl = new HCISUPCMS.BYTE_ARRAY(256);
            String strPathSsl = System.getProperty("user.dir") + "\\sdk\\windows\\ssleay32.dll";    //Linux版本是libssl.so库文件的路径
            System.arraycopy(strPathSsl.getBytes(), 0, ptrByteArraySsl.byValue, 0, strPathSsl.length());
            ptrByteArraySsl.write();
            hcEHomeAlarm.NET_EALARM_SetSDKInitCfg(1, ptrByteArraySsl.getPointer());

            //报警服务初始化
            boolean bRet = hcEHomeAlarm.NET_EALARM_Init();
            if (!bRet) {
                System.out.println("NET_EALARM_Init failed!");
            }
            //设置HCAapSDKCom组件库文件夹所在路径
            HCISUPCMS.BYTE_ARRAY ptrByteArrayCom = new HCISUPCMS.BYTE_ARRAY(256);
            String strPathCom = System.getProperty("user.dir") + "\\sdk\\windows\\HCAapSDKCom";        //只支持绝对路径，建议使用英文路径
            System.arraycopy(strPathCom.getBytes(), 0, ptrByteArrayCom.byValue, 0, strPathCom.length());
            ptrByteArrayCom.write();
            hcEHomeAlarm.NET_EALARM_SetSDKLocalCfg(5, ptrByteArrayCom.getPointer());
        } else if (OsSelect.isLinux()) {
            HCISUPCMS.BYTE_ARRAY ptrByteArrayCrypto = new HCISUPCMS.BYTE_ARRAY(256);
            String strPathCrypto = System.getProperty("user.dir") + "/sdk/linux/libcrypto.so"; //Linux版本是libcrypto.so库文件的路径
            System.arraycopy(strPathCrypto.getBytes(), 0, ptrByteArrayCrypto.byValue, 0, strPathCrypto.length());
            ptrByteArrayCrypto.write();
            hcEHomeAlarm.NET_EALARM_SetSDKInitCfg(0, ptrByteArrayCrypto.getPointer());

            //设置libssl.so所在路径
            HCISUPCMS.BYTE_ARRAY ptrByteArraySsl = new HCISUPCMS.BYTE_ARRAY(256);
            String strPathSsl = System.getProperty("user.dir") + "/sdk/linux/libssl.so";    //Linux版本是libssl.so库文件的路径
            System.arraycopy(strPathSsl.getBytes(), 0, ptrByteArraySsl.byValue, 0, strPathSsl.length());
            ptrByteArraySsl.write();
            hcEHomeAlarm.NET_EALARM_SetSDKInitCfg(1, ptrByteArraySsl.getPointer());
            //报警服务初始化
            boolean bRet = hcEHomeAlarm.NET_EALARM_Init();
            if (!bRet) {
                System.out.println("NET_EALARM_Init failed!");
            }
            //设置HCAapSDKCom组件库文件夹所在路径
            HCISUPCMS.BYTE_ARRAY ptrByteArrayCom = new HCISUPCMS.BYTE_ARRAY(256);
            String strPathCom = System.getProperty("user.dir") + "/sdk/linux/HCAapSDKCom/";        //只支持绝对路径，建议使用英文路径
            System.arraycopy(strPathCom.getBytes(), 0, ptrByteArrayCom.byValue, 0, strPathCom.length());
            ptrByteArrayCom.write();
            hcEHomeAlarm.NET_EALARM_SetSDKLocalCfg(5, ptrByteArrayCom.getPointer());
        }

        //启用SDK写日志
        boolean logToFile = hcEHomeAlarm.NET_EALARM_SetLogToFile(3, System.getProperty("user.dir") + "/EHomeSDKLog", false);
    }

    /**
     * 开启报警服务监听
     */
    public void startAlarmListen() {
        if (cbEHomeMsgCallBack == null) {
            cbEHomeMsgCallBack = new EHomeMsgCallBack();
        }
        System.arraycopy(propertiesUtil.readValue("AlarmServerListenIP").getBytes(), 0, net_ehome_alarm_listen_param.struAddress.szIP, 0, propertiesUtil.readValue("AlarmServerListenIP").length());
        if (Short.parseShort(propertiesUtil.readValue("AlarmServerType")) == 2) {
            net_ehome_alarm_listen_param.struAddress.wPort = Short.parseShort(propertiesUtil.readValue("AlarmServerListenTCPPort"));
            net_ehome_alarm_listen_param.byProtocolType = 2; //协议类型：0- TCP，1- UDP, 2-MQTT
        } else {
            net_ehome_alarm_listen_param.struAddress.wPort = Short.parseShort(propertiesUtil.readValue("AlarmServerListenUDPPort"));
            net_ehome_alarm_listen_param.byProtocolType = 1; //协议类型：0- TCP，1- UDP, 2-MQTT
        }
        net_ehome_alarm_listen_param.fnMsgCb = cbEHomeMsgCallBack;
        net_ehome_alarm_listen_param.byUseCmsPort = 0; //是否复用CMS端口：0- 不复用，非0- 复用
        net_ehome_alarm_listen_param.write();

        //启动报警服务器监听
        AlarmHandle = hcEHomeAlarm.NET_EALARM_StartListen(net_ehome_alarm_listen_param);
        if (AlarmHandle < 0) {
            System.out.println("NET_EALARM_StartListen failed, error:" + hcEHomeAlarm.NET_EALARM_GetLastError());
            hcEHomeAlarm.NET_EALARM_Fini();
            return;
        } else {
            String AlarmListenInfo = new String(net_ehome_alarm_listen_param.struAddress.szIP).trim() + "_" + net_ehome_alarm_listen_param.struAddress.wPort;
            System.out.println("报警服务器：" + AlarmListenInfo + ",NET_EALARM_StartListen succeed");
        }
    }

    /**
     * 报警回调函数实现，设备上传事件通过回调函数上传进行解析
     */
    class EHomeMsgCallBack implements HCISUPAlarm.EHomeMsgCallBack {
        @Override
        public boolean invoke(int iHandle, HCISUPAlarm.NET_EHOME_ALARM_MSG pAlarmMsg, Pointer pUser) {
            if ("console".equals(propertiesUtil.readValue("EventInfoPrintType"))) {
                // 输出事件信息到控制台上
                System.out.println("AlarmType: " + pAlarmMsg.dwAlarmType + ",dwAlarmInfoLen:" + pAlarmMsg.dwAlarmInfoLen + ",dwXmlBufLen:" + pAlarmMsg.dwXmlBufLen + "\n");
            }

            if (pAlarmMsg.dwXmlBufLen != 0) {
                HCISUPAlarm.BYTE_ARRAY strXMLData = new HCISUPAlarm.BYTE_ARRAY(pAlarmMsg.dwXmlBufLen);
                strXMLData.write();
                Pointer pPlateInfo = strXMLData.getPointer();
                pPlateInfo.write(0, pAlarmMsg.pXmlBuf.getByteArray(0, strXMLData.size()), 0, strXMLData.size());
                strXMLData.read();
                String strXML = new String(strXMLData.byValue).trim();
                // 告警接收信息输出到文件中
                if ("file".equals(propertiesUtil.readValue("EventInfoPrintType"))) {
                    // 输出事件信息到文件中
                    CommonMethod.outputToFile("dwAlarmType_pXmlBuf_"+ pAlarmMsg.dwAlarmType, ".xml", strXML);
                } else {
                    // 输出事件信息到控制台上
                    System.out.println(strXML + "\n");
                }
            }

            AlarmEventHandle.processAlarmData(pAlarmMsg.dwAlarmType,
                    pAlarmMsg.pAlarmInfo, pAlarmMsg.dwAlarmInfoLen,
                    pAlarmMsg.pXmlBuf, pAlarmMsg.dwXmlBufLen,
                    pAlarmMsg.pHttpUrl, pAlarmMsg.dwHttpUrlLen);
//            switch (pAlarmMsg.dwAlarmType) {
//                case 13:
//                    if (pAlarmMsg.pAlarmInfo != null) {
//                        HCISUPAlarm.NET_EHOME_ALARM_ISAPI_INFO strISAPIData = new HCISUPAlarm.NET_EHOME_ALARM_ISAPI_INFO();
//                        strISAPIData.write();
//                        Pointer pISAPIInfo = strISAPIData.getPointer();
//                        pISAPIInfo.write(0, pAlarmMsg.pAlarmInfo.getByteArray(0, strISAPIData.size()), 0, strISAPIData.size());
//                        strISAPIData.read();
//                        if (strISAPIData.dwAlarmDataLen != 0)//Json或者XML数据
//                        {
//                            HCISUPAlarm.BYTE_ARRAY m_strISAPIData = new HCISUPAlarm.BYTE_ARRAY(strISAPIData.dwAlarmDataLen);
//                            m_strISAPIData.write();
//                            Pointer pPlateInfo = m_strISAPIData.getPointer();
//                            pPlateInfo.write(0, strISAPIData.pAlarmData.getByteArray(0, m_strISAPIData.size()), 0, m_strISAPIData.size());
//                            m_strISAPIData.read();
//                            System.out.println(new String(m_strISAPIData.byValue).trim() + "\n");
//                        }
//                    }
//                    break;
//                default:
//                    break;
//            }
            return true;
        }
    }

}
