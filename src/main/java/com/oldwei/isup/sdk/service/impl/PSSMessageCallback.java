package com.oldwei.isup.sdk.service.impl;

import com.oldwei.isup.sdk.service.EHomeSSMsgCallBack;
import com.oldwei.isup.sdk.structure.NET_EHOME_SS_TOMCAT_MSG;
import com.sun.jna.Pointer;

//@Service("pssMessageCallback")
public class PSSMessageCallback implements EHomeSSMsgCallBack {

    public boolean invoke(int iHandle, int enumType, Pointer pOutBuffer, int dwOutLen, Pointer pInBuffer,
                          int dwInLen, Pointer pUser) {
        System.out.println("进入信息回调函数");
        if (1 == enumType) {
            NET_EHOME_SS_TOMCAT_MSG pTomcatMsg = new NET_EHOME_SS_TOMCAT_MSG();
            String szDevUri = new String(pTomcatMsg.szDevUri).trim();
            int dwPicNum = pTomcatMsg.dwPicNum;
            String pPicURLs = pTomcatMsg.pPicURLs;
            System.out.println("szDevUri = " + szDevUri + "   dwPicNum= " + dwPicNum + "   pPicURLs=" + pPicURLs);
        } else if (2 == enumType) {


        } else if (3 == enumType) {
        }
        return true;
    }
}