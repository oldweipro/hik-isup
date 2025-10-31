package com.oldwei.isup.sdk.service.impl;

import com.oldwei.isup.sdk.service.IHikISUPStream;
import com.oldwei.isup.sdk.service.VOICETALK_DATA_CB;
import com.oldwei.isup.sdk.service.VOICETALK_NEWLINK_CB;
import com.oldwei.isup.sdk.structure.NET_EHOME_VOICETALK_DATA_CB_PARAM;
import com.oldwei.isup.sdk.structure.NET_EHOME_VOICETALK_NEWLINK_CB_INFO;
import com.sun.jna.Pointer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("voiceCallBack")
@RequiredArgsConstructor
public class VoiceCallBackImpl implements VOICETALK_NEWLINK_CB {
    private final VOICETALK_DATA_CB voiceTalkDataCallBack;
    private final IHikISUPStream hikISUPStream;

    public boolean invoke(int lHandle, NET_EHOME_VOICETALK_NEWLINK_CB_INFO pNewLinkCBInfo, Pointer pUserData) {
        System.out.println("fVOICE_NEWLINK_CB callback");
        int lVoiceLinkHandle = -1;
        lVoiceLinkHandle = lHandle;
        NET_EHOME_VOICETALK_DATA_CB_PARAM net_ehome_voicetalk_data_cb_param = new NET_EHOME_VOICETALK_DATA_CB_PARAM();

        net_ehome_voicetalk_data_cb_param.fnVoiceTalkDataCB = voiceTalkDataCallBack;
        net_ehome_voicetalk_data_cb_param.write();
        if (!hikISUPStream.NET_ESTREAM_SetVoiceTalkDataCB(lHandle, net_ehome_voicetalk_data_cb_param)) {
            System.out.println("NET_ESTREAM_SetVoiceTalkDataCB()错误代码号：" + hikISUPStream.NET_ESTREAM_GetLastError());
            return false;
        }
        return true;
    }

}