package com.oldwei.hikisup.util.Alarm;

import com.oldwei.hikisup.util.CommonMethod;
import com.oldwei.hikisup.util.PropertiesUtil;
import com.oldwei.hikisup.sdk.SdkService.AlarmService.HCISUPAlarm;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import java.io.IOException;

/**
 * @author zhengxiaohui
 * @date 2023/8/14 11:45
 * @desc 告警事件处理函数
 */
public class AlarmEventHandle {


    public static void processAlarmData(int dwAlarmType, Pointer pStru, int dwStruLen, Pointer pXml, int dwXmlLen, Pointer pUrl, int dwUrlLen) {
        // 初始化读取外部配置文件的工具类
        initProperty();

        if (pUrl != Pointer.NULL) {
            dwAlarmType = HCISUPAlarm.EHOME_ISAPI_ALARM;
        }

        switch (dwAlarmType) {
            // Ehome基本报警
            case HCISUPAlarm.EHOME_ALARM: {
                processEhomeAlarm(dwAlarmType, pStru, dwStruLen, pXml, dwXmlLen);
                break;
            }
            // 热度图报告
            case HCISUPAlarm.EHOME_ALARM_HEATMAP_REPORT: {
                processEhomeAlarmHeatMapReport(pStru, dwStruLen, pXml, dwXmlLen);
                break;
            }
            // 人脸抓拍报告
            case HCISUPAlarm.EHOME_ALARM_FACESNAP_REPORT: {
                processEhomeFaceSnapReport(pStru, dwStruLen, pXml, dwXmlLen);
                break;
            }
            // GPS信息上传
            case HCISUPAlarm.EHOME_ALARM_GPS: {
                processEhomeGps(pStru, dwStruLen, pXml, dwXmlLen);
                break;
            }
            // 报警主机CID告警上传
            case HCISUPAlarm.EHOME_ALARM_CID_REPORT: {
                processEhomeCIDReport(pStru, dwStruLen, pXml, dwXmlLen);
                break;
            }
            // 图片URL上报
            case HCISUPAlarm.EHOME_ALARM_NOTICE_PICURL: {
                processEhomeAlarmNoticPicUrl(pStru, dwStruLen, pXml, dwXmlLen);
                break;
            }
            // 异步失败通知
            case HCISUPAlarm.EHOME_ALARM_NOTIFY_FAIL: {
                processEhomeNotifyFail(pStru, dwStruLen, pXml, dwXmlLen);
                break;
            }
            // 门禁事件上报
            case HCISUPAlarm.EHOME_ALARM_ACS: {
                processEhomeAlarmAcs(pStru, dwStruLen);
                break;
            }
            // 无线网络信息上传
            case HCISUPAlarm.EHOME_ALARM_WIRELESS_INFO: {
                processAlarmWirelessInfo(pStru, dwStruLen, pXml, dwXmlLen);
                break;
            }
            // ISAPI报警上传
            case HCISUPAlarm.EHOME_ISAPI_ALARM: {
                processEhomeIsapiAlarm(pStru, dwStruLen, pUrl, dwUrlLen);
                break;
            }
            // 车载设备的客流数据
            case HCISUPAlarm.EHOME_ALARM_MPDCDATA: {
                processEhomeAlarmMpdcData(pStru, dwStruLen, pXml, dwXmlLen);
                break;
            }
            // 二维码报警上传
            case HCISUPAlarm.EHOME_ALARM_QRCODE: {
                processEhomeAlarmQrcode(pXml, dwXmlLen);
                break;
            }
            // 人脸测温报警上传
            case HCISUPAlarm.EHOME_ALARM_FACETEMP: {
                processEhomeAlarmFaceTemp(pXml, dwXmlLen);
                break;
            }
            default: {
                System.out.println("unknown_Alarm_type: " + dwAlarmType);
            }
        }
    }

    /**
     * Ehome基本报警
     *
     * @param pStru
     * @param dwStruLen
     * @param pXml
     * @param dwXmlLen
     */
    public static void processEhomeAlarm(int alarmType, Pointer pStru, int dwStruLen, Pointer pXml, int dwXmlLen) {
        HCISUPAlarm.NET_EHOME_ALARM_INFO ehomeAlarmInfo = new HCISUPAlarm.NET_EHOME_ALARM_INFO();
        ehomeAlarmInfo.write();
        Pointer pEhomeAlarmInfo = ehomeAlarmInfo.getPointer();
        pEhomeAlarmInfo.write(0, pStru.getByteArray(0, ehomeAlarmInfo.size()), 0, ehomeAlarmInfo.size());
        ehomeAlarmInfo.read();

        StringBuffer bf = new StringBuffer();
        bf.append("[ALARM]DeviceID:" + Native.toString(ehomeAlarmInfo.szDeviceID)
                + ",\nTime:" + Native.toString(ehomeAlarmInfo.szAlarmTime)
                + ",\nType:" + ehomeAlarmInfo.dwAlarmType
                + ",\nAction:" + ehomeAlarmInfo.dwAlarmAction
                + ",\nChannel:" + ehomeAlarmInfo.dwVideoChannel
                + ",\nAlarmIn:" + ehomeAlarmInfo.dwAlarmInChannel
                + ",\nDiskNo:" + ehomeAlarmInfo.dwDiskNumber);

        switch (alarmType) {
            case HCISUPAlarm.ALARM_TYPE_DEV_CHANGED_STATUS: {
                bf.append("\n[ALARM_TYPE_DEV_CHANGED_STATUS]byDeviceStatus:" + ehomeAlarmInfo.uStatusUnion.struDevStatusChanged.byDeviceStatus);
                break;
            }
            case HCISUPAlarm.ALARM_TYPE_CHAN_CHANGED_STATUS: {
                bf.append("\n[ALARM_TYPE_CHAN_CHANGED_STATUS]byChanStatus:" + ehomeAlarmInfo.uStatusUnion.struChanStatusChanged.byChanStatus
                        + ",wChanNO:" + ehomeAlarmInfo.uStatusUnion.struChanStatusChanged.wChanNO);
                break;
            }
            case HCISUPAlarm.ALARM_TYPE_HD_CHANGED_STATUS: {
                bf.append("\n[ALARM_TYPE_HD_CHANGED_STATUS]byHDStatus:" + ehomeAlarmInfo.uStatusUnion.struHdStatusChanged.byHDStatus
                        + ",wHDNo:" + ehomeAlarmInfo.uStatusUnion.struHdStatusChanged.wHDNo
                        + ",dwVolume:" + ehomeAlarmInfo.uStatusUnion.struHdStatusChanged.dwVolume);
                break;
            }
            case HCISUPAlarm.ALARM_TYPE_DEV_TIMING_STATUS: {
                bf.append("\n[ALARM_TYPE_DEV_TIMING_STATUS]byCPUUsage:%d" + ehomeAlarmInfo.uStatusUnion.struDevTimeStatus.byCPUUsage +
                        ",byMainFrameTemp:%d" + ehomeAlarmInfo.uStatusUnion.struDevTimeStatus.byMainFrameTemp +
                        ",byBackPanelTemp:%d" + ehomeAlarmInfo.uStatusUnion.struDevTimeStatus.byBackPanelTemp +
                        ",dwMemoryTotal:%d" + ehomeAlarmInfo.uStatusUnion.struDevTimeStatus.dwMemoryTotal +
                        ",dwMemoryUsage:%d" + ehomeAlarmInfo.uStatusUnion.struDevTimeStatus.dwMemoryUsage);
                break;
            }
            case HCISUPAlarm.ALARM_TYPE_CHAN_TIMING_STATUS: {
                bf.append("\n[ALARM_TYPE_CHAN_TIMING_STATUS]byLinkNum:%d" + ehomeAlarmInfo.uStatusUnion.struChanTimeStatus.byLinkNum +
                        ",wChanNO:%d" + ehomeAlarmInfo.uStatusUnion.struChanTimeStatus.wChanNO +
                        ",dwBitRate:%d" + ehomeAlarmInfo.uStatusUnion.struChanTimeStatus.dwBitRate);
                break;
            }
            case HCISUPAlarm.ALARM_TYPE_HD_TIMING_STATUS: {
                bf.append("\n[ALARM_TYPE_HD_TIMING_STATUS]wHDNo:%d" + ehomeAlarmInfo.uStatusUnion.struHdTimeStatus.wHDNo +
                        ",dwHDFreeSpace:%d" + ehomeAlarmInfo.uStatusUnion.struHdTimeStatus.dwHDFreeSpace);
                break;
            }
            default: {
                break;
            }
        }

        handleAlarmInfo(HCISUPAlarm.EHOME_ALARM, bf.toString());
    }

    /**
     * 热度图报告
     *
     * @param pStru
     * @param dwStruLen
     * @param pXml
     * @param dwXmlLen
     */
    public static void processEhomeAlarmHeatMapReport(Pointer pStru, int dwStruLen, Pointer pXml, int dwXmlLen) {
        if (pStru == Pointer.NULL) {
            return;
        }
        HCISUPAlarm.NET_EHOME_HEATMAP_REPORT struHeatMapReport = new HCISUPAlarm.NET_EHOME_HEATMAP_REPORT();
        struHeatMapReport.write();
        Pointer pStruHeatMapReport = struHeatMapReport.getPointer();
        pStruHeatMapReport.write(0, pStru.getByteArray(0, struHeatMapReport.size()), 0, struHeatMapReport.size());
        struHeatMapReport.read();

        String info = "[HEATMAPREPORT]DeviceID: " + Native.toString(struHeatMapReport.byDeviceID) +
                ",\nChannel: " + struHeatMapReport.dwVideoChannel +
                ",\nStart: " + Native.toString(struHeatMapReport.byStartTime) +
                ",\nStop: " + Native.toString(struHeatMapReport.byStopTime) +
                ",\nHeatMapValue: " + struHeatMapReport.struHeatmapValue.dwMaxHeatMapValue
                + "  " + struHeatMapReport.struHeatmapValue.dwMinHeatMapValue
                + "  " + struHeatMapReport.struHeatmapValue.dwTimeHeatMapValue +
                ",\nSize: " + struHeatMapReport.struPixelArraySize.dwLineValue
                + "  " + struHeatMapReport.struPixelArraySize.dwColumnValue;

        handleAlarmInfo(HCISUPAlarm.EHOME_ALARM_HEATMAP_REPORT, info);
    }


    /**
     * 人脸抓拍报告
     *
     * @param pStru
     * @param dwStruLen
     * @param pXml
     * @param dwXmlLen
     */
    public static void processEhomeFaceSnapReport(Pointer pStru, int dwStruLen, Pointer pXml, int dwXmlLen) {
        if (pStru == Pointer.NULL) {
            return;
        }
        HCISUPAlarm.NET_EHOME_FACESNAP_REPORT struFaceSnapReport = new HCISUPAlarm.NET_EHOME_FACESNAP_REPORT();
        struFaceSnapReport.write();
        Pointer pStruFaceSnapReport = struFaceSnapReport.getPointer();
        pStruFaceSnapReport.write(0, pStru.getByteArray(0, struFaceSnapReport.size()), 0, struFaceSnapReport.size());
        struFaceSnapReport.read();

        StringBuffer bf = new StringBuffer();

        bf.append("[FACESNAPREPORT]DeviceID: " + Native.toString(struFaceSnapReport.byDeviceID) +
                ",\nChannel:" + struFaceSnapReport.dwVideoChannel +
                ",\nTime:" + Native.toString(struFaceSnapReport.byAlarmTime) +
                ",\nPicID:" + struFaceSnapReport.dwFacePicID +
                ",\nScore:" + struFaceSnapReport.dwFaceScore +
                ",\nTargetID:" + struFaceSnapReport.dwTargetID +
                ",\nTarget Zone[" +
                " " + struFaceSnapReport.struTarketZone.dwX +
                " " + struFaceSnapReport.struTarketZone.dwY +
                " " + struFaceSnapReport.struTarketZone.dwWidth +
                " " + struFaceSnapReport.struTarketZone.dwHeight +
                "]" +
                ",\nFacePicZone[" +
                " " + struFaceSnapReport.struFacePicZone.dwX +
                " " + struFaceSnapReport.struFacePicZone.dwY +
                " " + struFaceSnapReport.struFacePicZone.dwWidth +
                " " + struFaceSnapReport.struFacePicZone.dwHeight +
                "]" +
                ",\nHumanFeature:[" +
                " " + struFaceSnapReport.struHumanFeature.byAgeGroup +
                " " + struFaceSnapReport.struHumanFeature.bySex +
                " " + struFaceSnapReport.struHumanFeature.byEyeGlass +
                " " + struFaceSnapReport.struHumanFeature.byMask +
                " " +
                "]" +
                ",\nDuration:" + struFaceSnapReport.dwStayDuration +
                ",\nFacePicLen:" + struFaceSnapReport.dwFacePicLen +
                ",\nBackGroundPicLen:" + struFaceSnapReport.dwBackgroudPicLen
        );

        handleAlarmInfo(HCISUPAlarm.EHOME_ALARM_FACESNAP_REPORT, bf.toString());
    }

    /**
     * GPS信息上传
     *
     * @param pStru
     * @param dwStruLen
     * @param pXml
     * @param dwXmlLen
     */
    public static void processEhomeGps(Pointer pStru, int dwStruLen, Pointer pXml, int dwXmlLen) {
        if (pStru == Pointer.NULL) {
            return;
        }
        HCISUPAlarm.NET_EHOME_GPS_INFO struGps = new HCISUPAlarm.NET_EHOME_GPS_INFO();
        struGps.write();
        Pointer pStr = struGps.getPointer();
        pStr.write(0, pStru.getByteArray(0, struGps.size()), 0, struGps.size());
        struGps.read();

        StringBuffer bf = new StringBuffer();
        bf.append("[GPS]DeviceID:" + Native.toString(struGps.byDeviceID) +
                ",\nSampleTime:" + Native.toString(struGps.bySampleTime) +
                ",\nDivision:[" +
                "" + struGps.byDivision[0] +
                " " + struGps.byDivision[1] +
                "]" +
                ",\nSatelites:" + struGps.bySatelites +
                ",\nPrecision:" + struGps.byPrecision +
                ",\nLongitude:" + struGps.dwLongitude +
                ",\nLatitude:" + struGps.dwLatitude +
                ",\nDirection:" + struGps.dwDirection +
                ",\nSpeed:" + struGps.dwSpeed +
                ",\nHeight:" + struGps.dwHeight);
        handleAlarmInfo(HCISUPAlarm.EHOME_ALARM_GPS, bf.toString());
    }

    /**
     * 报警主机CID告警上传
     *
     * @param pStru
     * @param dwStruLen
     * @param pXml
     * @param dwXmlLen
     */
    public static void processEhomeCIDReport(Pointer pStru, int dwStruLen, Pointer pXml, int dwXmlLen) {
        if (pStru == Pointer.NULL) {
            return;
        }
        HCISUPAlarm.NET_EHOME_CID_INFO strCidInfo = new HCISUPAlarm.NET_EHOME_CID_INFO();
        HCISUPAlarm.NET_EHOME_CID_INFO_INTERNAL_EX strCidInfoEx = new HCISUPAlarm.NET_EHOME_CID_INFO_INTERNAL_EX();
        HCISUPAlarm.NET_EHOME_CID_INFO_PICTUREINFO_EX strPicInfoEx = new HCISUPAlarm.NET_EHOME_CID_INFO_PICTUREINFO_EX();

        strCidInfo.write();
        Pointer pStrCidInfo = strCidInfo.getPointer();
        pStrCidInfo.write(0, pStru.getByteArray(0, strCidInfo.size()), 0, strCidInfo.size());
        strCidInfo.read();

        strCidInfoEx.write();
        Pointer pStrCidInfoEx = strCidInfoEx.getPointer();
        pStrCidInfoEx.write(0, strCidInfo.pCidInfoEx.getByteArray(0, strCidInfoEx.size()), 0, strCidInfoEx.size());
        strCidInfoEx.read();

        strPicInfoEx.write();
        Pointer pStrPicInfoEx = strPicInfoEx.getPointer();
        pStrPicInfoEx.write(0, strCidInfo.pPicInfoEx.getByteArray(0, strPicInfoEx.size()), 0, strPicInfoEx.size());
        strPicInfoEx.read();

        String cDescribe = Native.toString(strCidInfo.byCIDDescribe);
        String uuid = null;
        StringBuffer bf = new StringBuffer();
        bf.append("\ncDescribe: " + cDescribe);

        // 有拓展字段则处理拓展字段信息
        if (strCidInfo.byExtend == 1) {
            cDescribe = Native.toString(strCidInfoEx.byCIDDescribeEx);
            uuid = Native.toString(strCidInfoEx.byUUID);
            bf.append("\n[CID_EX]uuid[" + Native.toString(strCidInfoEx.byUUID) + "]" +
                    ",\nrecheck[" + strCidInfoEx.byRecheck + "]" +
                    ",\nRecheck URL[" + Native.toString(strCidInfoEx.byVideoURL) + "]" +
                    ",\nvideoType[" + Native.toString(strCidInfoEx.byVideoType) + "]");
            for (int i = 0; i < HCISUPAlarm.MAX_PICTURE_NUM; i++) {
                if ((strCidInfoEx.byRecheck == 1) && (strPicInfoEx.byPictureURL[i][0]) != '\0') {
                    bf.append("\n[CID_EX]uuid[" + Native.toString(strCidInfoEx.byUUID) + "]" +
                            ",\nrecheck[" + strCidInfoEx.byRecheck + "]" +
                            ",\nPicURL[" + Native.toString(strPicInfoEx.byPictureURL[i]) + "]");
                }
            }

            StringBuilder LinkedSubSystem = new StringBuilder();
            for (int i = 0; i < HCISUPAlarm.MAX_SUBSYSTEM_LEN; i++) {
                if (strCidInfoEx.byLinkageSubSystem[i] > 0)//关联子系统最小值为1
                {
                    LinkedSubSystem.append(strCidInfoEx.byLinkageSubSystem[i]);
                }
            }
            bf.append("\n[CID_EX] All Linked SubSystem: " + LinkedSubSystem);
        }

        bf.append("\n[CID]uuid[%s]" + uuid +
                ",\nDeviceID:%s" + Native.toString(strCidInfo.byDeviceID) +
                ",\nCID code:%d" + strCidInfo.dwCIDCode +
                ",\nCID type:%d" + strCidInfo.dwCIDType +
                ",\nSubsys No:%d" + strCidInfo.dwSubSysNo +
                ",\nDescribe:%s" + cDescribe +
                ",\nTriggerTime:%s" + Native.toString(strCidInfo.byTriggerTime) +
                ",\nUploadTime:%s" + Native.toString(strCidInfo.byUploadTime) +
                ",\nCID param[" +
                "  " + strCidInfo.struCIDParam.dwUserType +
                "  " + strCidInfo.struCIDParam.lUserNo +
                "  " + strCidInfo.struCIDParam.lZoneNo +
                "  " + strCidInfo.struCIDParam.lKeyboardNo +
                "  " + strCidInfo.struCIDParam.lVideoChanNo +
                "  " + strCidInfo.struCIDParam.lDiskNo +
                "  " + strCidInfo.struCIDParam.lModuleAddr +
                //UTF-8转GBK
                "  " + CommonMethod.UTF8toGBKStr(strCidInfo.struCIDParam.byUserName) +
                "]");

        handleAlarmInfo(HCISUPAlarm.EHOME_ALARM_CID_REPORT, bf.toString());
    }

    /**
     * 图片URL上报
     *
     * @param pStru
     * @param dwStruLen
     * @param pXml
     * @param dwXmlLen
     */
    public static void processEhomeAlarmNoticPicUrl(Pointer pStru, int dwStruLen, Pointer pXml, int dwXmlLen) {
        if (pStru == Pointer.NULL) {
            return;
        }

        HCISUPAlarm.NET_EHOME_NOTICE_PICURL pStruNoticePicUrl = new HCISUPAlarm.NET_EHOME_NOTICE_PICURL();
        pStruNoticePicUrl.write();
        Pointer pointer = pStruNoticePicUrl.getPointer();
        pointer.write(0, pStru.getByteArray(0, pStruNoticePicUrl.size()), 0, pStruNoticePicUrl.size());
        pStruNoticePicUrl.read();

        StringBuffer bf = new StringBuffer();
        bf.append("[NOTICEPICURL]DeviceID: " + Native.toString(pStruNoticePicUrl.byDeviceID) +
                ",\nPicType: " + pStruNoticePicUrl.wPicType +
                ",\nAlarmType: " + pStruNoticePicUrl.wAlarmType +
                ",\nAlarmChan: " + pStruNoticePicUrl.dwAlarmChan +
                ",\nAlarmTime: " + Native.toString(pStruNoticePicUrl.byAlarmTime) +
                ",\nCaptureChan: " + pStruNoticePicUrl.dwCaptureChan +
                ",\nPicTime: " + Native.toString(pStruNoticePicUrl.byPicTime) +
                ",\nURL: " + Native.toString(pStruNoticePicUrl.byPicUrl) +
                ",\nManualSeq: " + pStruNoticePicUrl.dwManualSnapSeq);

        handleAlarmInfo(HCISUPAlarm.EHOME_ALARM_NOTICE_PICURL, bf.toString());
    }

    /**
     * 异步失败通知
     *
     * @param pStru
     * @param dwStruLen
     * @param pXml
     * @param dwXmlLen
     */
    public static void processEhomeNotifyFail(Pointer pStru, int dwStruLen, Pointer pXml, int dwXmlLen) {
        if (pStru == Pointer.NULL) {
            return;
        }
        HCISUPAlarm.NET_EHOME_NOTIFY_FAIL_INFO strucInfo = new HCISUPAlarm.NET_EHOME_NOTIFY_FAIL_INFO();
        strucInfo.write();
        Pointer pointer = strucInfo.getPointer();
        pointer.write(0, pStru.getByteArray(0, strucInfo.size()), 0, strucInfo.size());
        strucInfo.read();

        StringBuffer sb = new StringBuffer();
        sb.append("[NOTIFYFAIL]DeviceID: " + Native.toString(strucInfo.byDeviceID) +
                ",\nFailedCommand: " + strucInfo.wFailedCommand +
                ",\nPicType: " + strucInfo.wPicType +
                ",\nManualSeq: " + strucInfo.dwManualSnapSeq);

        handleAlarmInfo(HCISUPAlarm.EHOME_ALARM_NOTIFY_FAIL, sb.toString());
    }

    /**
     * 门禁事件上报
     *
     * @param pStru
     * @param dwStruLen
     */
    public static void processEhomeAlarmAcs(Pointer pStru, int dwStruLen) {
        if (pStru == Pointer.NULL || dwStruLen == 0) {
            return;
        }

        HCISUPAlarm.BYTE_ARRAY strXMLData = new HCISUPAlarm.BYTE_ARRAY(dwStruLen);
        strXMLData.write();
        Pointer pPlateInfo = strXMLData.getPointer();
        pPlateInfo.write(0, pStru.getByteArray(0, strXMLData.size()), 0, strXMLData.size());
        strXMLData.read();
        String strXML = new String(strXMLData.byValue).trim();

        handleAlarmInfo(HCISUPAlarm.EHOME_ALARM_ACS, strXML);
    }

    /**
     * 无线网络信息上传
     *
     * @param pStru
     * @param dwStruLen
     * @param pXml
     * @param dwXmlLen
     */
    public static void processAlarmWirelessInfo(Pointer pStru, int dwStruLen, Pointer pXml, int dwXmlLen) {
        HCISUPAlarm.NET_EHOME_ALARMWIRELESSINFO strucInfo = new HCISUPAlarm.NET_EHOME_ALARMWIRELESSINFO();
        strucInfo.write();
        Pointer pointer = strucInfo.getPointer();
        pointer.write(0, pStru.getByteArray(0, strucInfo.size()), 0, strucInfo.size());
        strucInfo.read();

        StringBuffer sb = new StringBuffer();
        sb.append("[Wireless]DeviceID: " + Native.toString(strucInfo.byDeviceID) +
                ",\nDataTraffic: " + ((float) strucInfo.dwDataTraffic) / 100 +
                ",\nSignalIntensity:" + strucInfo.bySignalIntensity);

        handleAlarmInfo(HCISUPAlarm.EHOME_ALARM_WIRELESS_INFO, sb.toString());
    }

    /**
     * ISAPI报警上传
     *
     * @param pStru
     * @param dwStruLen
     * @param pUrl
     * @param dwUrlLen
     */
    public static void processEhomeIsapiAlarm(Pointer pStru, int dwStruLen, Pointer pUrl, int dwUrlLen) {
        if (pUrl != Pointer.NULL && dwUrlLen > 0) {
            // ISUP4.0事件上报，默认报警与图片数据不分离，此时返回的url不为空，这种情况增加下demo打印
            // 分离后的数据，url字段不为空
            System.out.println("ISAPI报警上传, ISAPI Alarm");
            return;
        }

        if (pStru == Pointer.NULL) {
            return;
        }

        HCISUPAlarm.NET_EHOME_ALARM_ISAPI_INFO strISAPIAlarm = new HCISUPAlarm.NET_EHOME_ALARM_ISAPI_INFO();
        strISAPIAlarm.write();
        Pointer pISAPIAlarm = strISAPIAlarm.getPointer();
        pISAPIAlarm.write(0, pStru.getByteArray(0, strISAPIAlarm.size()), 0, strISAPIAlarm.size());
        strISAPIAlarm.read();

        if (strISAPIAlarm.pAlarmData != Pointer.NULL) {
            String alarmData = null;
            // 判断报警数据的格式
            if (strISAPIAlarm.byDataType != 0) { // 1: xml格式数据 2：json格式数据
                HCISUPAlarm.BYTE_ARRAY m_strISAPIData = new HCISUPAlarm.BYTE_ARRAY(strISAPIAlarm.dwAlarmDataLen);
                m_strISAPIData.write();
                Pointer pPlateInfo = m_strISAPIData.getPointer();
                pPlateInfo.write(0, strISAPIAlarm.pAlarmData.getByteArray(0, m_strISAPIData.size()), 0, m_strISAPIData.size());
                m_strISAPIData.read();

                alarmData = new String(m_strISAPIData.byValue).trim();

                handleAlarmInfo(HCISUPAlarm.EHOME_ISAPI_ALARM, alarmData);
            }
        }
    }

    /**
     * 车载设备的客流数据
     *
     * @param pStru
     * @param dwStruLen
     * @param pXml
     * @param dwXmlLen
     */
    public static void processEhomeAlarmMpdcData(Pointer pStru, int dwStruLen, Pointer pXml, int dwXmlLen) {
        HCISUPAlarm.NET_EHOME_ALARM_MPDCDATA structure = new HCISUPAlarm.NET_EHOME_ALARM_MPDCDATA();
        structure.write();
        Pointer pointer = structure.getPointer();
        pointer.write(0, pStru.getByteArray(0, structure.size()), 0, structure.size());
        structure.read();

        StringBuffer sb = new StringBuffer();
        sb.append("[MPDCData]DeviceID:" + Native.toString(structure.byDeviceID) +
                ",\nSampleTime: " + Native.toString(structure.bySampleTime) +
                ",\nRetranseFlag: " + structure.byRetranseFlag +
                ",\nCount: " + structure.struMPData.dwCount
        );
        handleAlarmInfo(HCISUPAlarm.EHOME_ALARM_MPDCDATA, sb.toString());
    }

    /**
     * 二维码报警上传
     *
     * @param pXml
     * @param dwXmlLen
     */
    public static void processEhomeAlarmQrcode(Pointer pXml, int dwXmlLen) {
        if (pXml == Pointer.NULL) {
            return;
        }
        HCISUPAlarm.BYTE_ARRAY strXMLData = new HCISUPAlarm.BYTE_ARRAY(dwXmlLen);
        strXMLData.write();
        Pointer pPlateInfo = strXMLData.getPointer();
        pPlateInfo.write(0, pXml.getByteArray(0, strXMLData.size()), 0, strXMLData.size());
        strXMLData.read();

        String strXML = new String(strXMLData.byValue).trim();
        handleAlarmInfo(HCISUPAlarm.EHOME_ALARM_QRCODE, strXML);
    }

    /**
     * 人脸测温报警上传
     *
     * @param pXml
     * @param dwXmlLen
     */
    public static void processEhomeAlarmFaceTemp(Pointer pXml, int dwXmlLen) {
        if (pXml == Pointer.NULL || dwXmlLen == 0) {
            return;
        }

        HCISUPAlarm.BYTE_ARRAY strXMLData = new HCISUPAlarm.BYTE_ARRAY(dwXmlLen);
        strXMLData.write();
        Pointer pPlateInfo = strXMLData.getPointer();
        pPlateInfo.write(0, pXml.getByteArray(0, strXMLData.size()), 0, strXMLData.size());
        strXMLData.read();

        String strXML = new String(strXMLData.byValue).trim();
        handleAlarmInfo(HCISUPAlarm.EHOME_ALARM_FACETEMP, strXML);
    }

    static PropertiesUtil propertiesUtil = null;

    private static void initProperty() {
        if (propertiesUtil == null) {
            try {
                propertiesUtil = new PropertiesUtil("./config.properties");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 处理告警信息（输出到文件或者是输出到控制台）
     *
     * @param alarmType
     * @param info
     */
    private static void handleAlarmInfo(int alarmType, String info) {
        if ("file".equals(propertiesUtil.readValue("EventInfoPrintType"))) {
            // 输出事件信息到文件中
            CommonMethod.outputToFile("dwAlarmType_" + alarmType, ".txt", info);
        } else {
            // 输出事件信息到控制台上
            System.out.println(info);
        }
    }
}
