package com.oldwei.isup.sdk.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oldwei.isup.model.AlarmType;
import com.oldwei.isup.model.PushDataConfig;
import com.oldwei.isup.model.cb.DeviceEventBase;
import com.oldwei.isup.model.cb.DeviceEventParser;
import com.oldwei.isup.model.cb.FaceCaptureEvent;
import com.oldwei.isup.model.cb.GPSUploadEvent;
import com.oldwei.isup.model.vo.UploadData;
import com.oldwei.isup.sdk.AlarmEventHandle;
import com.oldwei.isup.sdk.service.EHomeMsgCallBack;
import com.oldwei.isup.sdk.structure.BYTE_ARRAY;
import com.oldwei.isup.sdk.structure.NET_EHOME_ALARM_ISAPI_INFO;
import com.oldwei.isup.sdk.structure.NET_EHOME_ALARM_MSG;
import com.oldwei.isup.service.IPushDataConfigService;
import com.oldwei.isup.util.CommonMethod;
import com.oldwei.isup.util.WebFluxHttpUtil;
import com.sun.jna.Pointer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service("alarmMsgCallBack")
@RequiredArgsConstructor
public class AlarmMsgCallBack implements EHomeMsgCallBack {

    private final IPushDataConfigService pushDataConfigService;

    @Override
    public boolean invoke(int iHandle, NET_EHOME_ALARM_MSG pAlarmMsg, Pointer pUser) {
        log.info("--------报警回调函数被调用--------");
        // 输出事件信息到控制台上
        log.info("AlarmType: {}, dwAlarmInfoLen: {}, dwXmlBufLen: {}, iHandle: {}", pAlarmMsg.dwAlarmType, pAlarmMsg.dwAlarmInfoLen, pAlarmMsg.dwXmlBufLen, iHandle);

        if (pAlarmMsg.dwXmlBufLen > 0) {
            BYTE_ARRAY strXMLData = new BYTE_ARRAY(pAlarmMsg.dwXmlBufLen);
            strXMLData.write();
            Pointer pPlateInfo = strXMLData.getPointer();
            pPlateInfo.write(0, pAlarmMsg.pXmlBuf.getByteArray(0, strXMLData.size()), 0, strXMLData.size());
            strXMLData.read();
            String strXML = new String(strXMLData.byValue).trim();
            // 输出事件信息到文件中
            CommonMethod.outputToFile("dwAlarmType_pXmlBuf_" + pAlarmMsg.dwAlarmType, ".xml", strXML);
            // 输出事件信息到控制台上
            log.info("XML事件信息: {}", strXML);
        }

        AlarmEventHandle.processAlarmData(pAlarmMsg.dwAlarmType,
                pAlarmMsg.pAlarmInfo, pAlarmMsg.dwAlarmInfoLen,
                pAlarmMsg.pXmlBuf, pAlarmMsg.dwXmlBufLen,
                pAlarmMsg.pHttpUrl, pAlarmMsg.dwHttpUrlLen);
        if (AlarmType.fromCode(pAlarmMsg.dwAlarmType) == AlarmType.ALARM_TYPE_CALL_HELP) {
            if (pAlarmMsg.pAlarmInfo != null) {
                NET_EHOME_ALARM_ISAPI_INFO strISAPIData = new NET_EHOME_ALARM_ISAPI_INFO();
                strISAPIData.write();
                Pointer pISAPIInfo = strISAPIData.getPointer();
                pISAPIInfo.write(0, pAlarmMsg.pAlarmInfo.getByteArray(0, strISAPIData.size()), 0, strISAPIData.size());
                strISAPIData.read();
                if (strISAPIData.dwAlarmDataLen > 0) {
                    //Json或者XML数据
                    BYTE_ARRAY m_strISAPIData = new BYTE_ARRAY(strISAPIData.dwAlarmDataLen);
                    m_strISAPIData.write();
                    Pointer pPlateInfo = m_strISAPIData.getPointer();
                    pPlateInfo.write(0, strISAPIData.pAlarmData.getByteArray(0, m_strISAPIData.size()), 0, m_strISAPIData.size());
                    m_strISAPIData.read();
                    String data = new String(m_strISAPIData.byValue).trim();
                    try {
                        DeviceEventBase event = DeviceEventParser.parse(data);

                        List<PushDataConfig> list = pushDataConfigService.list(new LambdaQueryWrapper<PushDataConfig>().eq(PushDataConfig::getEnable, 1));
                        if (list.isEmpty()) {
                            log.warn("未配置数据推送，请先配置推送参数");
                            return true;
                        }
                        ObjectMapper mapper = new ObjectMapper();
                        UploadData uploadData = new UploadData();
                        if (event instanceof FaceCaptureEvent faceEvent) {
                            log.info("检测到人脸抓拍：{}", faceEvent.getFaceCapture().size());
                            uploadData.setDataType("FaceCapture");
                            // 序列化为 JSON 字符串
                            String jsonString = mapper.writeValueAsString(faceEvent);
                            uploadData.setData(jsonString);
                        } else if (event instanceof GPSUploadEvent gpsEvent) {
                            log.info("GPS 上报：{}", gpsEvent.getGps());
                            uploadData.setDataType("GPS");
                            // 序列化为 JSON 字符串
                            String jsonString = mapper.writeValueAsString(gpsEvent);
                            uploadData.setData(jsonString);
                        } else {
                            log.info("未知事件类型：{}", event.getEventType());
                        }
                        list.forEach(config -> {
                            String pushPath = config.getPushPath();
                            WebFluxHttpUtil.postAsync(pushPath, uploadData, String.class).subscribe(resp -> {
                                log.info("推送到 {} 返回结果：{}", pushPath, resp);
                            }, error -> {
                                log.error("推送到 {} 失败：{}", pushPath, error.getMessage());
                            });
                        });
                    } catch (Exception e) {
                        // 或者使用日志记录
                        log.error("解析设备事件失败: {}", e.getMessage(), e);
                    }
                }
            }
        }
        return true;
    }
}

