package com.oldwei.hikisup.util;

import com.oldwei.hikisup.sdk.SdkService.CmsService.HCISUPCMS;

import java.io.UnsupportedEncodingException;

import static com.oldwei.hikisup.sdk.SdkService.CmsService.CmsDemo.hCEhomeCMS;

/**
 * @author zhengxiaohui
 * @date 2023/9/6 15:05
 * @desc SDK 常见功能接口的封装服务
 * 注意： 该工具类非sdk自身逻辑，属于demo业务逻辑部分，为了演示方便创建
 */
public class SdkFunctionWrapUtil {

    /**
     * CMS服务的协议透传接口，较为通用，为了避免重复定义，提升代码可读性，这里demo代码演示时封装为通用的工具类
     *
     * @param loginID    cms 设置回调中（demo实现类为 FRegisterCallBack）获取到的登录句柄
     * @param reqUrl     透传的接口url，格式为 [POST|GET|PUT|DELETE /ISAPI/***]
     * @param reqContent 透传的报文内容，完整的报文内容字段定义以及业务字段说明，请您自行申请产品型号的ISAPI协议文档查看
     */
    public static void isapiPassThrough(int loginID, String reqUrl, String reqContent) {
        if (reqUrl == null) {
            throw new RuntimeException("示例代码中修改的透传的请求地址为null");
        }

        HCISUPCMS.NET_EHOME_PTXML_PARAM m_struParam = new HCISUPCMS.NET_EHOME_PTXML_PARAM();
        m_struParam.read();
        //透传URL，不同功能对应不同的URL，完整协议报文说明需要参考ISAPI协议文档
        String urlInBuffer = reqUrl;
        HCISUPCMS.BYTE_ARRAY ptrurlInBuffer = new HCISUPCMS.BYTE_ARRAY(urlInBuffer.length() + 1);
        System.arraycopy(urlInBuffer.getBytes(), 0, ptrurlInBuffer.byValue, 0, urlInBuffer.length());
        ptrurlInBuffer.write();
        m_struParam.pRequestUrl = ptrurlInBuffer.getPointer();
        m_struParam.dwRequestUrlLen = urlInBuffer.length();

        if (reqContent != null) {
            byte[] byInbuffer = new byte[0];
            try {
                byInbuffer = reqContent.getBytes("utf-8");
            } catch (UnsupportedEncodingException e) {
                System.out.println("isapiPassThrough错误, " + e.getMessage());
                throw new RuntimeException(e);
            }
            int iInBufLen = byInbuffer.length;
            HCISUPCMS.BYTE_ARRAY ptrInBuffer = new HCISUPCMS.BYTE_ARRAY(iInBufLen);
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
        HCISUPCMS.BYTE_ARRAY ptrOutByte2 = new HCISUPCMS.BYTE_ARRAY(iOutSize2);
        m_struParam.pOutBuffer = ptrOutByte2.getPointer();
        m_struParam.dwOutSize = iOutSize2;
        m_struParam.dwRecvTimeOut = 5000; // 接收超时时间，单位毫秒
        m_struParam.write();
        if (!hCEhomeCMS.NET_ECMS_ISAPIPassThrough(loginID, m_struParam)) {
            System.out.println("NET_ECMS_ISAPIPassThrough failed, error：" + hCEhomeCMS.NET_ECMS_GetLastError());
            return;
        } else {
            m_struParam.read();
            ptrOutByte2.read();
            System.out.println("\nNET_ECMS_ISAPIPassThrough succeed\n" + "输出报文:\n" + new String(ptrOutByte2.byValue).trim());
        }
        return;
    }

}
