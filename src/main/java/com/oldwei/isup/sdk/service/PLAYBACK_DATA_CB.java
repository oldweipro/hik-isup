package com.oldwei.isup.sdk.service;

import com.oldwei.isup.sdk.structure.NET_EHOME_PLAYBACK_DATA_CB_INFO;
import com.sun.jna.Callback;
import com.sun.jna.Pointer;

public interface PLAYBACK_DATA_CB extends Callback {
    boolean invoke(int iPlayBackLinkHandle, NET_EHOME_PLAYBACK_DATA_CB_INFO pDataCBInfo, Pointer pUserData);
}
