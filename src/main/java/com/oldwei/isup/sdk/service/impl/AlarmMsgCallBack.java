package com.oldwei.isup.sdk.service.impl;

import com.oldwei.isup.sdk.AlarmEventHandle;
import com.oldwei.isup.sdk.service.EHomeMsgCallBack;
import com.oldwei.isup.sdk.structure.BYTE_ARRAY;
import com.oldwei.isup.sdk.structure.NET_EHOME_ALARM_ISAPI_INFO;
import com.oldwei.isup.sdk.structure.NET_EHOME_ALARM_MSG;
import com.oldwei.isup.util.CommonMethod;
import com.sun.jna.Pointer;

//@Service("alarmMsgCallBack")
public class AlarmMsgCallBack implements EHomeMsgCallBack {
    @Override
    public boolean invoke(int iHandle, NET_EHOME_ALARM_MSG pAlarmMsg, Pointer pUser) {
        System.out.println("--------报警回调函数被调用--------");
        // 输出事件信息到控制台上
        System.out.println("AlarmType: " + pAlarmMsg.dwAlarmType + ",dwAlarmInfoLen:" + pAlarmMsg.dwAlarmInfoLen + ",dwXmlBufLen:" + pAlarmMsg.dwXmlBufLen + "\n");

        if (pAlarmMsg.dwXmlBufLen != 0) {
            BYTE_ARRAY strXMLData = new BYTE_ARRAY(pAlarmMsg.dwXmlBufLen);
            strXMLData.write();
            Pointer pPlateInfo = strXMLData.getPointer();
            pPlateInfo.write(0, pAlarmMsg.pXmlBuf.getByteArray(0, strXMLData.size()), 0, strXMLData.size());
            strXMLData.read();
            String strXML = new String(strXMLData.byValue).trim();
            // 输出事件信息到文件中
            CommonMethod.outputToFile("dwAlarmType_pXmlBuf_" + pAlarmMsg.dwAlarmType, ".xml", strXML);
            // 输出事件信息到控制台上
            System.out.println("事件信息\n" + strXML + "\n");
        }

        AlarmEventHandle.processAlarmData(pAlarmMsg.dwAlarmType,
                pAlarmMsg.pAlarmInfo, pAlarmMsg.dwAlarmInfoLen,
                pAlarmMsg.pXmlBuf, pAlarmMsg.dwXmlBufLen,
                pAlarmMsg.pHttpUrl, pAlarmMsg.dwHttpUrlLen);
        if (pAlarmMsg.dwAlarmType == 13) {
            if (pAlarmMsg.pAlarmInfo != null) {
                NET_EHOME_ALARM_ISAPI_INFO strISAPIData = new NET_EHOME_ALARM_ISAPI_INFO();
                strISAPIData.write();
                Pointer pISAPIInfo = strISAPIData.getPointer();
                pISAPIInfo.write(0, pAlarmMsg.pAlarmInfo.getByteArray(0, strISAPIData.size()), 0, strISAPIData.size());
                strISAPIData.read();
                if (strISAPIData.dwAlarmDataLen != 0) {
                    //Json或者XML数据
                    BYTE_ARRAY m_strISAPIData = new BYTE_ARRAY(strISAPIData.dwAlarmDataLen);
                    m_strISAPIData.write();
                    Pointer pPlateInfo = m_strISAPIData.getPointer();
                    pPlateInfo.write(0, strISAPIData.pAlarmData.getByteArray(0, m_strISAPIData.size()), 0, m_strISAPIData.size());
                    m_strISAPIData.read();
                    System.out.println(new String(m_strISAPIData.byValue).trim() + "\n");
                }
            }
        }
        return true;
    }
}

