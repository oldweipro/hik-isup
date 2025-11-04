package com.oldwei.isup.sdk.isapi;

import com.oldwei.isup.model.xml.DeviceInfo;
import com.oldwei.isup.model.xml.InputProxyChannelStatusList;
import com.oldwei.isup.sdk.service.impl.CmsUtil;
import com.oldwei.isup.util.XmlUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ISAPIService {
    private final CmsUtil cmsUtil;


    /**
     * 获取设备信息（型号、版本、序列号等）
     *
     * @param lUserID
     */
    public DeviceInfo GetDevInfo(int lUserID) {
        String getDevInfoURL = "GET /ISAPI/System/deviceInfo";
        String contextXML = cmsUtil.passThrough(lUserID, getDevInfoURL, "");
        return XmlUtil.fromXml(contextXML, DeviceInfo.class);
    }

    /**
     * 获取所有数字通道状态
     *
     * @param lUserID
     */
    public InputProxyChannelStatusList GetAllDigitalChannelStatus(int lUserID) {
        String GetAllDigitalChannelStatusURL = "GET /ISAPI/ContentMgmt/InputProxy/channels/status";
        String contextXML = cmsUtil.passThrough(lUserID, GetAllDigitalChannelStatusURL, "");
//        log.info("GetAllDigitalChannelStatusURL: {}", contextXML);
        return XmlUtil.fromXml(contextXML, InputProxyChannelStatusList.class);
    }

    /**
     * 云台控制 ISUP5.0透传接口
     *
     * @param lUserID
     */
    public void PTZCtrl(int lUserID, int channel) {
        String PTZCtrlUrl = "PUT /ISAPI/PTZCtrl/channels/" + channel + "/continuous";
        /**
         * pan 表示水平运行速度 +60表示右移，其他方向运行参考类似方法
         * tilt 表示垂直运行速度
         */
        String PTZCtrlInput = """
                <?xml version: "1.0" encoding="UTF-8"?>
                <PTZData>
                    <pan>60</pan>
                    <tilt>0</tilt>
                </PTZData>""";
        //接口调用成功后，云台会一直按照设置速度进行右转
        cmsUtil.passThrough(lUserID, PTZCtrlUrl, PTZCtrlInput);
        try {
            Thread.sleep(5000); //云台运动持续时间1s后调用停止云台运动接口
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //发送云台运动停止请求
        String PTZCtrlStopInput = """
                <?xml version: "1.0" encoding="UTF-8"?>
                <PTZData>
                    <pan>0</pan>
                    <tilt>0</tilt>
                </PTZData>""";

        cmsUtil.passThrough(lUserID, PTZCtrlUrl, PTZCtrlStopInput);
    }


    /**
     * 远程抓图
     * URL中的<ID>，格式A0B，A表示实际通道号，B传1表示主码流，传2表示子码流。
     * 示例：GET /ISAPI/Streaming/channels/101/picture/async?format=json&imageType=JPEG&URLType=cloudURL表示获取通道1的主码流分辨率的抓图。
     *
     * @param lUserID
     */
    public void GetPicByCloud(int lUserID) {
        String getPicURL = "GET /ISAPI/Streaming/channels/101/picture/async?format=json&imageType=JPEG&URLType=cloudURL";
        cmsUtil.passThrough(lUserID, getPicURL, "");

    }
}
