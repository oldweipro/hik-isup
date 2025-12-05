package com.oldwei.isup.sdk.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oldwei.isup.config.HikPlatformProperties;
import com.oldwei.isup.model.AlarmType;
import com.oldwei.isup.model.cb.*;
import com.oldwei.isup.model.vo.UploadData;
import com.oldwei.isup.model.xml.EventNotificationAlert;
import com.oldwei.isup.sdk.AlarmEventHandle;
import com.oldwei.isup.sdk.service.EHomeMsgCallBack;
import com.oldwei.isup.sdk.structure.BYTE_ARRAY;
import com.oldwei.isup.sdk.structure.NET_EHOME_ALARM_ISAPI_INFO;
import com.oldwei.isup.sdk.structure.NET_EHOME_ALARM_MSG;
import com.oldwei.isup.util.CommonMethod;
import com.oldwei.isup.util.WebFluxHttpUtil;
import com.oldwei.isup.util.XmlUtil;
import com.sun.jna.Pointer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service("alarmMsgCallBack")
@RequiredArgsConstructor
public class AlarmMsgCallBack implements EHomeMsgCallBack {
    private final HikPlatformProperties hikPlatformProperties;

    @Override
    public boolean invoke(int iHandle, NET_EHOME_ALARM_MSG pAlarmMsg, Pointer pUser) {
//        log.info("--------报警回调函数被调用--------");
        // 输出事件信息到控制台上
//        log.info("AlarmType: {}, dwAlarmInfoLen: {}, dwXmlBufLen: {}, iHandle: {}", pAlarmMsg.dwAlarmType, pAlarmMsg.dwAlarmInfoLen, pAlarmMsg.dwXmlBufLen, iHandle);

        ObjectMapper mapper = new ObjectMapper();
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
//            log.info("XML事件信息: {}", strXML);
        } else {
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
                        // 0-invalid,1-xml,2-json
                        String dataType = String.valueOf(strISAPIData.byDataType).trim();
//                        log.info("ISAPI报警数据类型: {}", dataType);
                        String data = new String(m_strISAPIData.byValue).trim();

                        UploadData uploadData = new UploadData();
                        if (StringUtils.equals("1", dataType)) {
                            try {
                                log.info("ISAPI报警XML数据: {}", data);
                                EventNotificationAlert eventNotificationAlert = XmlUtil.fromXml(data, EventNotificationAlert.class);
                                if (eventNotificationAlert != null) {
                                    String jsonString = mapper.writeValueAsString(eventNotificationAlert);
                                    log.info("eventNotificationAlert事件消息：{}", jsonString);
                                    uploadData.setDataType("ANPR");
                                    uploadData.setData(jsonString);
                                    String pushPath = hikPlatformProperties.getPushAddress();
                                    WebFluxHttpUtil.postAsync(pushPath, uploadData, String.class).subscribe(resp -> {
//                                        log.info("推送到 {} 返回结果：{}", pushPath, resp);
                                    }, error -> {
                                        log.error("推送到 {} 失败：{}", pushPath, error.getMessage());
                                    });
                                }
                            } catch (Exception e) {
                                // 或者使用日志记录
                                log.error("解析设备事件失败: {}\n 消息内容： {}", e.getMessage(), data);
                            }
                        } else if (StringUtils.equals("2", dataType)) {
                            try {
                                DeviceEventBase event = DeviceEventParser.parse(data);
                                if (event instanceof FaceCaptureEvent faceEvent) {
                                    uploadData.setDataType("FaceCapture");
                                    // 序列化为 JSON 字符串
                                    String jsonString = mapper.writeValueAsString(faceEvent);
//                                    log.info("检测到人脸抓拍：{}", jsonString);
                                    uploadData.setData(jsonString);
                                } else if (event instanceof GPSUploadEvent gpsEvent) {
                                    // log.info("GPS 上报：{}", gpsEvent.getGps());
                                    uploadData.setDataType("GPS");
                                    // 序列化为 JSON 字符串
                                    String jsonString = mapper.writeValueAsString(gpsEvent);
                                    uploadData.setData(jsonString);
                                } else if (event instanceof AlarmResultEvent alarmResultEvent) {
                                    uploadData.setDataType("AlarmResult");
                                    // 序列化为 JSON 字符串
                                    String jsonString = mapper.writeValueAsString(alarmResultEvent);
//                                    log.info("AlarmResultEvent 上报：{}", jsonString);
                                    uploadData.setData(jsonString);
                                } else {
                                    log.info("未知事件类型：{}", event.getEventType());
                                    String jsonString = mapper.writeValueAsString(event);
                                    uploadData.setDataType(jsonString);
                                }

                                String pushPath = hikPlatformProperties.getPushAddress();
                                WebFluxHttpUtil.postAsync(pushPath, uploadData, String.class).subscribe(resp -> {
//                                    log.info("推送到 {} 返回结果：{}", pushPath, resp);
                                }, error -> {
                                    log.error("推送到 {} 失败：{}", pushPath, error.getMessage());
                                });

                            } catch (Exception e) {
                                // 或者使用日志记录
                                log.error("解析设备事件失败: {}\n 消息内容： {}", e.getMessage(), data);
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
}

//{
//        "ipAddress":  "172.6.64.7",
//        /*ro, req, string, 报警设备IPv4地址*/
//        "ipv6Address":  "1080:0:0:0:8:800:200C:417A",
//        /*ro, opt, string, 报警设备IPv6地址*/
//        "portNo":  80,
//        /*ro, opt, int, 报警设备端口号*/
//        "protocol":  "HTTP",
//        /*ro, opt, string, 协议类型, range:[0,64], desc:[HTTP#HTTP,HTTPS#HTTPS,EHome#EHome,OTAP#OTAP], 该字段无实际意义*/
//        "macAddress":  "01:17:24:45:D9:F4",
//        /*ro, opt, string, MAC地址*/
//        "channelID":  1,
//        /*ro, opt, int, 触发报警的设备通道号, desc:触发的视频通道号（1、    在SDK透传ISAPI协议的时候，上传的是 私有协议对应的视频通道号；（2、    在萤石透传ISAPI协议的时候，上传的是 萤石协议对应的视频通道号；（3、    在ISUP透传ISAPI协议的时候，上传的是 ISUP协议对应的视频通道号；*/
//        "dateTime":  "2004-05-03T17:30:08+08:00",
//        /*ro, req, datetime, 报警触发时间*/
//        "activePostCount":  1,
//        /*ro, opt, int, 同一个报警已经上传的次数, desc:事件触发频次脉冲事件 定义：事件持续触发（按照设备的检测频率），例如：移动侦测。瞬时事件 定义：区分目标，一个目标触发一次，例如：人脸抓拍activePostCount  在脉冲事件 类型触发的时候，用于区分是否是同一触发源触发的事件。例如：移动侦测，按照设备检测频率会一直上传；如果触发源发生了变化，这个时候计数就可以重新开始了。这个可以作为事件触发频次的方式来集成；*/
//        "eventType":  "GPSUpload",
//        /*ro, req, enum, 事件类型, subType:string, [GPSUpload#GPS信息上传]*/
//        "eventState":  "active",
//        /*ro, req, enum, 事件状态, subType:string, [active#有效事件,inactive#无效事件], desc:针对持续性事件active – 表示有效事件（开始 或者 无过程状态也使用该字段）；inactive – 表示无效事件（结束）；remark:在心跳类型下，该字段赋值（表示心跳数据,10s上传一次）；*/
//        "eventDescription":  "GPS information",
//        /*ro, req, enum, 事件描述, subType:string, [GPS information#GPS信息上传]*/
//        "devIndex":  "test",
//        /*ro, opt, string, 报警设备唯一标示 string类型*/
//        "channelName":  "test",
//        /*ro, req, string, 设备通道名称, range:[0,64]*/
//        "deviceID":  "test",
//        /*ro, opt, string, 设备ID, range:[0,256]*/
//        "isDataRetransmission":  true,
//        /*ro, opt, bool, 重传数据标记, desc:由于网络异常等因素;导致的实时检测的数据上传失败;后设备异常因素恢复后重新上传当时的采集分析数据*/
//        "deviceUUID":  "12345",
//        /*ro, opt, string, 设备编号, range:[1,32], desc:设备唯一标识，默认值为设备出厂序列号，可通过协议/ISAPI/System/deviceInfo中的<deviceID>字段进行编辑修改*/
//        "GPS": {
//        /*ro, req, object, GPS信息*/
//        "divisionEW":  "E",
//        /*ro, req, enum, 东西半球, subType:string, [E#东半球,W#西半球]*/
//        "longitude":  100,
//        /*ro, req, int, 经度, desc:经度=实际度*3600*100+实际分*60*100+实际秒*100*/
//        "divisionNS":  "N",
//        /*ro, req, enum, 南北半球, subType:string, [N#北半球,S#南半球]*/
//        "latitude":  100,
//        /*ro, req, int, 纬度, desc:纬度=实际度*3600*100+实际分*60*100+实际秒*100 integer32类型*/
//        "direction":  100,
//        /*ro, opt, int, 车辆方向, desc:车辆方向=实际方向（以度为单位，正北方向为0，顺时针方向计算）*100*/
//        "speed":  100,
//        /*ro, opt, int, 速度, desc:速度：厘米/小时*/
//        "satellites":  1,
//        /*ro, opt, int, 卫星数量*/
//        "precision":  1,
//        /*ro, opt, int, 精度因子*/
//        "height":  1,
//        /*ro, opt, int, 高度, unit:cm, unitType:长度*/
//        "retransFlag":  1,
//        /*ro, opt, enum, 重传标记, subType:int, [0#本条GPS为实时包,1#本条GPS为重传包]*/
//        "timeZone":  "HH::MM",
//        /*ro, opt, string, 时区, desc:时区，在标准时区基础上加减一段时间，跟TimeZoneIdx 含义冲突，优先使用TimeZoneIdx*/
//        "timeZoneIdx":  1,
//        /*ro, opt, enum, 时区, subType:int, [0#无效,1#(GMT-12:00)日界线西,2#(GMT-11:00) 萨摩亚群岛,3#(GMT-10:00) 夏威夷,4#(GMT-09:00) 阿拉斯加,5#(GMT-08:00) 太平洋时间(美国和加拿大),6#(GMT-07:00) 山地时间 (美国和加拿大),7#(GMT-06:00) 中部时间 (美国和加拿大),8#(GMT-05:00) 东部时间 (美国和加拿大),9#(GMT-04:30) 加拉加斯,10#(GMT-04:00) 大西洋时间 (加拿大),11#(GMT-03:30) 纽芬兰,12#(GMT-03:00) 巴西利亚,13#(GMT-02:00) 中大西洋,14#(GMT-01:00) 佛得角群岛,15#(GMT) 都柏林,16#(GMT+01:00) 阿姆斯特丹,17#(GMT+02:00) 哈拉雷,18#(GMT+03:00) 巴格达,19#(GMT+03:30) 德黑兰,20#(GMT+04:00) 阿布扎比,21#(GMT+04:30) 喀布尔,22#(GMT+05:00) 叶卡捷琳堡,23#(GMT+05:30) 马德拉斯,24#(GMT+05:45) 加德满都,25#(GMT+06:00) 阿斯塔纳,26#(GMT+06:30) 仰光,27#(GMT+07:00) 曼谷,28#(GMT+08:00) 北京,29#(GMT+09:00) 大阪,30#(GMT+09:30) 达尔文,31#(GMT+10:00) 关岛,32#(GMT+11:00) 马加丹,33#(GMT+12:00) 奥克兰,34#(GMT+13:00) 努库阿洛法,35#(GMT+14:00) 圣诞岛], desc:时区*/
//        "HDOP":  1.1,
//        /*ro, opt, float, 水平精度因子, desc:精确到0.1，参考/ISAPI/Mobile/location/status中的HDOP*/
//        "locateStatus":  "success",
//		/*ro, opt, enum, 定位状态, subType:string, [success#成功,fail#失败],
//		desc:1、参考/ISAPI/Mobile/location/status中的locateStatus，默认为success。
//		2、当为fail，设备对于divisionEW、longitude、divisionNS、latitude上传示例值即可，上层平台可不关注。*/
//        "passengerMeterStatus":  "pricing"
//        /*ro, opt, enum, 载客咪表状态, subType:string, [pricing#计价中,unopened#未开启,fault#故障], desc:当设备部署在运营车上时，则会上报载客咪表状态*/
//        }
//        }