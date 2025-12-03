package com.oldwei.isup.sdk.service.impl;

import com.oldwei.isup.config.HikIsupProperties;
import com.oldwei.isup.model.xml.PpvspMessage;
import com.oldwei.isup.sdk.service.HCISUPCMS;
import com.oldwei.isup.sdk.structure.*;
import com.oldwei.isup.util.CommonMethod;
import com.oldwei.isup.util.ConfigFileUtil;
import com.oldwei.isup.util.XmlUtil;
import com.sun.jna.ptr.IntByReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CmsUtil {
    private final HCISUPCMS hcisupcms;
    private final HikIsupProperties hikIsupProperties;
    //云台相关结构体
    public static final int NET_EHOME_PTZ_CTRL = 1000;   //云镜控制

    /**
     * NET_ECMS_XMLRemoteControl接口示例 查找并获取设备工作状态。
     * 包含多个参数的字符串，包括通道号，录像状态（0-停止，1-开始），视频信号状态（0-正常，1-视频丢
     * 失），通道编码状态（0-正常，1-异常），实际码率（单位：Kbps），关联客户端数量，相机关联状态（0-未关联
     * 相机，1-已关联相机），和相机状态（0-离线，1-上线）；每个参数由“-”分隔
     * <CH>1-----0----------------------0----------------------------0-------------------------5382----------------0-----------缺失---------------------------------缺失</CH>
     * <CH>通道号-录像状态（0-停止，1-开始）-视频信号状态（0-正常，1-视频丢失）-通道编码状态（0-正常，1-异常）-实际码率（单位：Kbps）-关联客户端数量-相机关联状态（0-未关联相机，1-已关联相机）-和相机状态（0-离线，1-上线）</CH>
     */
    public PpvspMessage CMS_XMLRemoteControl(int lLoginID) {
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
        PpvspMessage msg = new PpvspMessage();
        if (!hcisupcms.NET_ECMS_XMLRemoteControl(lLoginID, struRemoteCtrl, struRemoteCtrl.size())) {
            int iErr = hcisupcms.NET_ECMS_GetLastError();
            //NET_ECMS_XMLRemoteControl failed,error：10
            log.error("NET_ECMS_XMLRemoteControl failed,error：{}", iErr);
        } else {
            struRemoteCtrl.read();
            m_struOutbuffer.read();
            m_struStatusBuffer.read();
            String statusBuffer = new String(m_struStatusBuffer.byValue).trim();
//            log.info("lpStatusBuffer: {}", statusBuffer);
            msg = XmlUtil.fromXml(statusBuffer, PpvspMessage.class);
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
        return msg;
    }

    /**
     * CMS服务的协议透传接口，较为通用，为了避免重复定义，提升代码可读性，这里demo代码演示时封装为通用的工具类
     *
     * @param loginID    cms 设置回调中（demo实现类为 FRegisterCallBack）获取到的登录句柄
     * @param reqUrl     透传的接口url，格式为 [POST|GET|PUT|DELETE /ISAPI/***]
     * @param reqContent 透传的报文内容，完整的报文内容字段定义以及业务字段说明，请您自行申请产品型号的ISAPI协议文档查看
     */
    public String passThrough(int loginID, String reqUrl, String reqContent) {
        if (reqUrl == null) {
            throw new RuntimeException("示例代码中修改的透传的请求地址为null");
        }

        NET_EHOME_PTXML_PARAM m_struParam = new NET_EHOME_PTXML_PARAM();
        m_struParam.read();
        //透传URL，不同功能对应不同的URL，完整协议报文说明需要参考ISAPI协议文档
        BYTE_ARRAY ptrurlInBuffer = new BYTE_ARRAY(reqUrl.length() + 1);
        System.arraycopy(reqUrl.getBytes(), 0, ptrurlInBuffer.byValue, 0, reqUrl.length());
        ptrurlInBuffer.write();
        m_struParam.pRequestUrl = ptrurlInBuffer.getPointer();
        m_struParam.dwRequestUrlLen = reqUrl.length();

        if (reqContent != null && !reqContent.trim().isEmpty()) {
            byte[] byInbuffer;
            byInbuffer = reqContent.getBytes(StandardCharsets.UTF_8);
            int iInBufLen = byInbuffer.length;
            BYTE_ARRAY ptrInBuffer = new BYTE_ARRAY(iInBufLen);
            ptrInBuffer.read();
            System.arraycopy(byInbuffer, 0, ptrInBuffer.byValue, 0, iInBufLen);
            ptrInBuffer.write();
            m_struParam.dwInSize = iInBufLen;
            m_struParam.pInBuffer = ptrInBuffer.getPointer();
        } else {
            m_struParam.dwInSize = 0;
            m_struParam.pInBuffer = null; // GET获取时不需要输入参数，输入为null
        }

        // 输出参数，分配的内存用于存储返回的数据，需要大于等于实际内容大小
        int iOutSize2 = 2 * 1024 * 1024;
        BYTE_ARRAY ptrOutByte2 = new BYTE_ARRAY(iOutSize2);
        m_struParam.pOutBuffer = ptrOutByte2.getPointer();
        m_struParam.dwOutSize = iOutSize2;
        m_struParam.dwRecvTimeOut = 5000; // 接收超时时间，单位毫秒
        m_struParam.write();
        if (!hcisupcms.NET_ECMS_ISAPIPassThrough(loginID, m_struParam)) {
            System.out.println("NET_ECMS_ISAPIPassThrough failed, error：" + hcisupcms.NET_ECMS_GetLastError());
        } else {
            m_struParam.read();
            ptrOutByte2.read();
        }
        return new String(ptrOutByte2.byValue).trim();
    }

    /**
     * ISUP透传接口
     */
    public void CMS_ISAPIPassThrough(int loginID) throws UnsupportedEncodingException {
        /**************************************************************
         * GET方法示例（以获取云存储配置为例）
         */
        NET_EHOME_PTXML_PARAM m_struParam = new NET_EHOME_PTXML_PARAM();
        m_struParam.read();

        //透传URL，不同功能对应不同的URL，完整协议报文说明需要参考ISAPI协议文档
        String url = "GET /ISAPI/System/PictureServer?format=json"; //获取云存储配置
        BYTE_ARRAY ptrUrl = new BYTE_ARRAY(url.length() + 1);
        System.arraycopy(url.getBytes(), 0, ptrUrl.byValue, 0, url.length());
        ptrUrl.write();
        m_struParam.pRequestUrl = ptrUrl.getPointer();
        m_struParam.dwRequestUrlLen = url.length();

        //输入参数，XML或者JSON数据
        m_struParam.pInBuffer = null;//GET获取时不需要输入参数，输入为null
        m_struParam.dwInSize = 0;

        //输出参数，分配的内存用于存储返回的数据，需要大于等于实际内容大小
        int iOutSize = 2 * 1024 * 1024;
        BYTE_ARRAY ptrOutByte = new BYTE_ARRAY(iOutSize);
        m_struParam.pOutBuffer = ptrOutByte.getPointer();
        m_struParam.dwOutSize = iOutSize;

        m_struParam.dwRecvTimeOut = 5000; //接收超时时间，单位毫秒
        m_struParam.write();

        if (!hcisupcms.NET_ECMS_ISAPIPassThrough(loginID, m_struParam)) {
            System.out.println("NET_ECMS_ISAPIPassThrough failed,error：" + hcisupcms.NET_ECMS_GetLastError());
            return;
        } else {
            m_struParam.read();
            ptrOutByte.read();
            System.out.println("NET_ECMS_ISAPIPassThrough succeed\n" + "ptrOutByte:" + new String(ptrOutByte.byValue).trim());
        }

        /**************************************************************
         * PUT方法示例（以设置云存储配置为例）
         */
        NET_EHOME_PTXML_PARAM m_struParam2 = new NET_EHOME_PTXML_PARAM();
        m_struParam2.read();

        //透传URL，不同功能对应不同的URL，完整协议报文说明需要参考ISAPI协议文档
        String url2 = "PUT /ISAPI/System/PictureServer?format=json"; //获取云存储配置
        BYTE_ARRAY ptrUrl2 = new BYTE_ARRAY(url2.length() + 1);
        System.arraycopy(url2.getBytes(), 0, ptrUrl2.byValue, 0, url2.length());
        ptrUrl2.write();
        m_struParam2.pRequestUrl = ptrUrl2.getPointer();
        m_struParam2.dwRequestUrlLen = url2.length();

        //输入参数，XML或者JSON数据
        Map<String, Object> parameter = new HashMap<>();
        parameter.put("ipv4Address", hikIsupProperties.getPicServer().getIp());
        parameter.put("portNo", Short.parseShort(hikIsupProperties.getPicServer().getPort()));
        String input = ConfigFileUtil.getReqBodyFromTemplate("conf/SdkCms/PutPicServerParam.json", parameter);
        System.out.println(input);

        int iInSize = input.getBytes("utf-8").length;
        BYTE_ARRAY ptrInParam = new BYTE_ARRAY(iInSize + 1);
        System.arraycopy(input.getBytes("utf-8"), 0, ptrInParam.byValue, 0, iInSize);
        ptrInParam.write();
        m_struParam2.pInBuffer = ptrInParam.getPointer();//GET获取时不需要输入参数，输入为null
        m_struParam2.dwInSize = iInSize;

        //输出参数，分配的内存用于存储返回的数据，需要大于等于实际内容大小
        int iOutSize2 = 2 * 1024 * 1024;
        BYTE_ARRAY ptrOutByte2 = new BYTE_ARRAY(iOutSize2);
        m_struParam2.pOutBuffer = ptrOutByte2.getPointer();
        m_struParam2.dwOutSize = iOutSize2;

        m_struParam2.dwRecvTimeOut = 5000; //接收超时时间，单位毫秒
        m_struParam2.write();

        if (!hcisupcms.NET_ECMS_ISAPIPassThrough(loginID, m_struParam2)) {
            System.out.println("NET_ECMS_ISAPIPassThrough failed,error：" + hcisupcms.NET_ECMS_GetLastError());
            return;
        } else {
            m_struParam2.read();
            ptrOutByte2.read();
            System.out.println("NET_ECMS_ISAPIPassThrough succeed\n" + "ptrOutByte:" + new String(ptrOutByte2.byValue).trim());
        }
        return;
    }

    /**
     * 通过透传接口下发人脸图片数据进行分析建模(超脑NVR等设备支持)
     */
    public void CMS_ISAPIPassThroughByFile(int loginID) {
        NET_EHOME_PTXML_PARAM m_struParam = new NET_EHOME_PTXML_PARAM();
        m_struParam.read();

        String url = "POST /ISAPI/Intelligent/analysisImage/face";
        BYTE_ARRAY ptrUrl1 = new BYTE_ARRAY(url.length());
        System.arraycopy(url.getBytes(), 0, ptrUrl1.byValue, 0, url.length());
        ptrUrl1.write();
        m_struParam.pRequestUrl = ptrUrl1.getPointer();
        m_struParam.dwRequestUrlLen = url.length();

        FileInputStream picfile = null;
        int picdataLength = 0;
        try {
            picfile = new FileInputStream(CommonMethod.getResFileAbsPath("container/resources/pics/FDLib.jpg"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            picdataLength = picfile.available();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        if (picdataLength < 0) {
            System.out.println("input file dataSize < 0");
            return;
        }

        BYTE_ARRAY ptrpicByte = new BYTE_ARRAY(picdataLength);
        try {
            picfile.read(ptrpicByte.byValue);
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        ptrpicByte.write();
        m_struParam.pInBuffer = ptrpicByte.getPointer();
        m_struParam.dwInSize = picdataLength;
        BYTE_ARRAY ptrOutByte = new BYTE_ARRAY(20 * 1024);
        m_struParam.pOutBuffer = ptrOutByte.getPointer();
        m_struParam.dwOutSize = 20 * 1024;
        m_struParam.write();

        if (!hcisupcms.NET_ECMS_ISAPIPassThrough(loginID, m_struParam)) {
            int iErr = hcisupcms.NET_ECMS_GetLastError();
            System.out.println("NET_ECMS_ISAPIPassThrough failed,error：" + iErr);
            return;
        } else {
            m_struParam.read();
            ptrOutByte.read();
            System.out.println("NET_ECMS_ISAPIPassThrough succeed\n" + "ptrOutByte:" + new String(ptrOutByte.byValue).trim());
        }
    }


    /**
     * NET_ECMS_GetDevConfig接口示例
     *
     * @return
     */
    public boolean getDevConfig(int loginID) {
        boolean bRet;
        //  关于报警输入配置条件参数的结构体
        NET_EHOME_ALARMIN_COND netEhomeAlarminCond = new NET_EHOME_ALARMIN_COND();
        netEhomeAlarminCond.read();
        netEhomeAlarminCond.dwSize = netEhomeAlarminCond.size();
        netEhomeAlarminCond.dwAlarmInNum = 1;
        netEhomeAlarminCond.dwPTZChan = 1;
        netEhomeAlarminCond.write();
        NET_EHOME_ALARMIN_CFG netEhomeAlarminCfg = new NET_EHOME_ALARMIN_CFG();
        netEhomeAlarminCfg.read();
        netEhomeAlarminCfg.dwSize = netEhomeAlarminCfg.size();
        netEhomeAlarminCfg.write();

        NET_EHOME_CONFIG strEhomeCfd = new NET_EHOME_CONFIG();
        strEhomeCfd.pCondBuf = netEhomeAlarminCond.getPointer();
        strEhomeCfd.dwCondSize = netEhomeAlarminCond.size();
        strEhomeCfd.pOutBuf = netEhomeAlarminCfg.getPointer();
        strEhomeCfd.dwOutSize = netEhomeAlarminCfg.size();
        strEhomeCfd.pInBuf = null;
        strEhomeCfd.dwInSize = 0;
        strEhomeCfd.write();


        bRet = hcisupcms.NET_ECMS_GetDevConfig(loginID, 11, strEhomeCfd.getPointer(), strEhomeCfd.size());
        if (!bRet) {
            int dwErr = hcisupcms.NET_ECMS_GetLastError();
            System.out.println("获取报警输入参数失败，Error:" + dwErr);
        }
        //  读取返回的数据
        netEhomeAlarminCfg.read();
        System.out.println("获取报警输入参数成功" + netEhomeAlarminCfg.byAlarmInType);
        return bRet;
    }

    /**
     * 获取设备信息的命令：NET_EHOME_GET_DEVICE_INFO
     *
     * @return
     */
    public NET_EHOME_DEVICE_INFO getDevInfo(int loginID) {
        boolean bRet;

        NET_EHOME_DEVICE_INFO ehomeDeviceInfo = new NET_EHOME_DEVICE_INFO();
        ehomeDeviceInfo.read();
        ehomeDeviceInfo.dwSize = ehomeDeviceInfo.size();
        ehomeDeviceInfo.write();

        NET_EHOME_CONFIG strEhomeCfd = new NET_EHOME_CONFIG();
        strEhomeCfd.pCondBuf = null;
        strEhomeCfd.dwCondSize = 0;
        strEhomeCfd.pOutBuf = ehomeDeviceInfo.getPointer();
        strEhomeCfd.dwOutSize = ehomeDeviceInfo.size();
        strEhomeCfd.pInBuf = null;
        strEhomeCfd.dwInSize = 0;
        strEhomeCfd.write();


        bRet = hcisupcms.NET_ECMS_GetDevConfig(loginID, 1, strEhomeCfd.getPointer(), strEhomeCfd.size());
        if (!bRet) {
            int dwErr = hcisupcms.NET_ECMS_GetLastError();
            System.out.println("获取报警输入参数失败，Error:" + dwErr);
        } else {
            //  读取返回的数据
            ehomeDeviceInfo.read();
            System.out.println("语音对讲的音频格式:" + ehomeDeviceInfo.dwAudioEncType);
            System.out.println("起始数字对讲通道号:" + ehomeDeviceInfo.byStartDTalkChan);
        }
        return ehomeDeviceInfo;
    }

    /**
     * NET_ECMS_RemoteControl接口示例
     */
    public void RemoteControl(int loginID) {
        //云台控制
        NET_EHOME_REMOTE_CTRL_PARAM net_ehome_remote_ctrl_param = new NET_EHOME_REMOTE_CTRL_PARAM();
        NET_EHOME_PTZ_PARAM net_ehome_ptz_param = new NET_EHOME_PTZ_PARAM();
        net_ehome_ptz_param.read();
        net_ehome_ptz_param.dwSize = net_ehome_ptz_param.size();
        net_ehome_ptz_param.byPTZCmd = 2;//0-向上,1-向下,2-向左,3-向右，更多取值参考接口文档
        net_ehome_ptz_param.byAction = 0;//云台动作：0- 开始云台动作，1- 停止云台动作
        net_ehome_ptz_param.bySpeed = 5;//云台速度，取值范围：0~7，数值越大速度越快
        net_ehome_ptz_param.write();
        net_ehome_remote_ctrl_param.read();
        net_ehome_remote_ctrl_param.dwSize = net_ehome_remote_ctrl_param.size();
        net_ehome_remote_ctrl_param.lpInbuffer = net_ehome_ptz_param.getPointer();//输入控制参数
        net_ehome_remote_ctrl_param.dwInBufferSize = net_ehome_ptz_param.size();

        //条件参数输入通道号
        int iChannel = 1; //视频通道号
        IntByReference channle = new IntByReference(iChannel);
        net_ehome_remote_ctrl_param.lpCondBuffer = channle.getPointer();
        net_ehome_remote_ctrl_param.dwCondBufferSize = 4;

        net_ehome_remote_ctrl_param.write();

        boolean b_ptz = hcisupcms.NET_ECMS_RemoteControl(loginID, NET_EHOME_PTZ_CTRL, net_ehome_remote_ctrl_param);
        if (!b_ptz) {
            int iErr = hcisupcms.NET_ECMS_GetLastError();
            System.out.println("NET_ECMS_XMLConfig failed,error：" + iErr);
            return;
        }
        System.out.println("云台控制调用成功");
    }
}
