package com.oldwei.isup.sdk.service;

import com.oldwei.isup.sdk.structure.NET_EHOME_VOICETALK_DATA_CB_INFO;
import com.sun.jna.Callback;
import com.sun.jna.Pointer;

public interface VOICETALK_DATA_CB extends Callback {
    public boolean invoke(int lHandle, NET_EHOME_VOICETALK_DATA_CB_INFO pNewLinkCBInfo, Pointer pUserData);
}