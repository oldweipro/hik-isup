package com.oldwei.isup.sdk.service.impl;

import com.oldwei.isup.domain.DeviceRemoteControl;
import com.oldwei.isup.sdk.service.HCISUPCMS;
import com.oldwei.isup.sdk.structure.BYTE_ARRAY;
import com.oldwei.isup.sdk.structure.NET_EHOME_XML_REMOTE_CTRL_PARAM;
import com.oldwei.isup.util.XmlUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CmsUtil {
    private final HCISUPCMS hcisupcms;

    /**
     * NET_ECMS_XMLRemoteControl接口示例 查找并获取设备工作状态。
     * 包含多个参数的字符串，包括通道号，录像状态（0-停止，1-开始），视频信号状态（0-正常，1-视频丢
     * 失），通道编码状态（0-正常，1-异常），实际码率（单位：Kbps），关联客户端数量，相机关联状态（0-未关联
     * 相机，1-已关联相机），和相机状态（0-离线，1-上线）；每个参数由“-”分隔
     * <CH>1-----0----------------------0----------------------------0-------------------------5382----------------0-----------缺失---------------------------------缺失</CH>
     * <CH>通道号-录像状态（0-停止，1-开始）-视频信号状态（0-正常，1-视频丢失）-通道编码状态（0-正常，1-异常）-实际码率（单位：Kbps）-关联客户端数量-相机关联状态（0-未关联相机，1-已关联相机）-和相机状态（0-离线，1-上线）</CH>
     */
    public DeviceRemoteControl CMS_XMLRemoteControl(int lLoginID) {
        //远程控制输入参数
        NET_EHOME_XML_REMOTE_CTRL_PARAM struRemoteCtrl = new NET_EHOME_XML_REMOTE_CTRL_PARAM();
        struRemoteCtrl.read();
        struRemoteCtrl.dwSize = struRemoteCtrl.size();
        struRemoteCtrl.dwRecvTimeOut = 5000; //接收超时时间
        struRemoteCtrl.dwSendTimeOut = 5000; //发送超时时间

        //获取设备工作状态(比如NVR设备，可以获取IP通道状态)
        String inputCfg = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<PPVSPMessage>\n" +
                "<Version>4.0</Version>\n" +
                "<Sequence>1</Sequence>\n" +
                "<CommandType>REQUEST</CommandType>\n" +
                "<Method>QUERY</Method>\n" +
                "<Command>GETDEVICEWORKSTATUS</Command>\n" +
                "<Params>\n" +
                "</Params>\n" +
                "</PPVSPMessage>";
//        //远程升级的报文
//        String inputCfg = "<?xml version=\"1.0\" encoding=\"GB2312\" ?>\n" +
//                "<PPVSPMessage>\n" +
//                "    <Version>4.0</Version>\n" +
//                "    <Sequence>1</Sequence>\n" +
//                "    <CommandType>REQUEST</CommandType>\n" +
//                "    <Method>CONTROL</Method>\n" +
//                "    <Command>UPDATE</Command>\n" +
//                "    <Params>\n" +
//                "        <FTPServerIP>10.17.34.106</FTPServerIP>\n" +
//                "        <FTPServerPort>8089</FTPServerPort>\n" +
//                "        <Account>hik</Account>\n" +
//                "        <Password>hik12345</Password>\n" +
//                "        <File>digicap.dav</File>\n" +
//                "    </Params>\n" +
//                "</PPVSPMessage>";

        BYTE_ARRAY m_struInbuffer = new BYTE_ARRAY(inputCfg.length() + 1);
        System.arraycopy(inputCfg.getBytes(), 0, m_struInbuffer.byValue, 0, inputCfg.length());
        m_struInbuffer.write();
        struRemoteCtrl.lpInbuffer = m_struInbuffer.getPointer();
        struRemoteCtrl.dwInBufferSize = m_struInbuffer.size();

        //输出参数
        BYTE_ARRAY m_struOutbuffer = new BYTE_ARRAY(5 * 1024);
        struRemoteCtrl.lpOutBuffer = m_struOutbuffer.getPointer();
        struRemoteCtrl.dwOutBufferSize = m_struOutbuffer.size();
        //输出状态信息
        BYTE_ARRAY m_struStatusBuffer = new BYTE_ARRAY(2 * 1024);
        struRemoteCtrl.lpStatusBuffer = m_struStatusBuffer.getPointer();
        struRemoteCtrl.dwStatusBufferSize = m_struStatusBuffer.size();

        struRemoteCtrl.write();

        if (!hcisupcms.NET_ECMS_XMLRemoteControl(lLoginID, struRemoteCtrl, struRemoteCtrl.size())) {
            int iErr = hcisupcms.NET_ECMS_GetLastError();
            //NET_ECMS_XMLRemoteControl failed,error：10
            log.error("NET_ECMS_XMLRemoteControl failed,error：{}", iErr);
            return new DeviceRemoteControl();
        } else {
            struRemoteCtrl.read();
            m_struOutbuffer.read();
            m_struStatusBuffer.read();
//            log.info("NET_ECMS_XMLRemoteControl succeed lpOutBuffer ：{}", new String(m_struOutbuffer.byValue).trim());
            String statusBuffer = new String(m_struStatusBuffer.byValue).trim();
//            log.info("lpStatusBuffer: {}", statusBuffer);
            DeviceRemoteControl deviceRemoteControl = new DeviceRemoteControl();
            deviceRemoteControl.setIsOnline(1);
            int xmlChannel = XmlUtil.findXmlChannel(statusBuffer);
            deviceRemoteControl.setLChannel(xmlChannel);
            return deviceRemoteControl;
//            //输出示例报文
//            "<PPVSPMessage>\n" +
//                    "<Version>2.0</Version>\n" +
//                    "<Sequence>422</Sequence>\n" +
//                    "<CommandType>RESPONSE</CommandType>\n" +
//                    "<WhichCommand>GETDEVICEWORKSTATUS</WhichCommand>\n" +
//                    "<Status>200</Status>\n" +
//                    "<Description>OK</Description>\n" +
//                    "<Params>\n" +
//                    "<DeviceStatusXML>\n" +
//                    "<Run>0</Run>\n" +
//                    "<CPU>14</CPU>\n" +
//                    "<Mem>19</Mem>\n" +
//                    "<DSKStatus>\n" +
//                    "<DSK>1-5723166-1381376-0</DSK>\n" +
//                    "<DSK>3-0-0-2</DSK>\n" +
//                    "</DSKStatus>\n" +
//                    "<CHStatus>\n" +
//                    "<CH>1-1-0-1-2048-0-1-1</CH>\n" +
//                    "<CH>2-0-1-0-0-0-1-0</CH>\n" +
//                    "</CHStatus>\n" +
//                    "<AlarmInStatus/>\n" +
//                    "<AlarmOutStatus/>\n" +
//                    "<LocalDisplayStatus>0</LocalDisplayStatus>\n" +
//                    "<Remark>test/debug</Remark>\n" +
//                    "</DeviceStatusXML>\n" +
//                    "</Params>\n" +
//                    "</PPVSPMessage>"
//  其中CHStatus的CH通道状态用一串字符串表示多个信息，用“-”分割：
// 通道号-录像状态（0：不录像；1：录像）-视频连接信号状态（0：正常；1：视频信号丢失）-通道编码状态（0：正常；1：异常，例如DSP死掉）-实际码率(kbps)-客户端连接数目
        }
    }
}
