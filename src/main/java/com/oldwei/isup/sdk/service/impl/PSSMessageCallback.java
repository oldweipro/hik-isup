package com.oldwei.isup.sdk.service.impl;

import com.oldwei.isup.sdk.service.EHomeSSMsgCallBack;
import com.oldwei.isup.sdk.structure.NET_EHOME_SS_TOMCAT_MSG;
import com.sun.jna.Pointer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service("pssMessageCallback")
public class PSSMessageCallback implements EHomeSSMsgCallBack {

    public boolean invoke(int iHandle, int enumType, Pointer pOutBuffer, int dwOutLen, Pointer pInBuffer,
                          int dwInLen, Pointer pUser) {
        log.info("进入信息回调函数");
        if (1 == enumType) {
            log.info("处理回调类型1");
            NET_EHOME_SS_TOMCAT_MSG pTomcatMsg = new NET_EHOME_SS_TOMCAT_MSG();
            String szDevUri = new String(pTomcatMsg.szDevUri).trim();
            int dwPicNum = pTomcatMsg.dwPicNum;
            String pPicURLs = pTomcatMsg.pPicURLs;
            log.info("szDevUri= {}, dwPicNum= {},  pPicURLs= {}", szDevUri, dwPicNum, pPicURLs);
        } else if (2 == enumType) {
            log.info("待处理回调类型2");
        } else if (3 == enumType) {
            log.info("待处理回调类型3");
        }
        return true;
    }
}