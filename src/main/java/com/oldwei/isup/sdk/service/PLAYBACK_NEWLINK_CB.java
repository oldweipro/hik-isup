package com.oldwei.isup.sdk.service;

import com.oldwei.isup.sdk.structure.NET_EHOME_PLAYBACK_NEWLINK_CB_INFO;
import com.sun.jna.Callback;
import com.sun.jna.Pointer;

public interface PLAYBACK_NEWLINK_CB extends Callback {
    boolean invoke(int lPlayBackLinkHandle, NET_EHOME_PLAYBACK_NEWLINK_CB_INFO pNewLinkCBMsg, Pointer pUserData);
}
