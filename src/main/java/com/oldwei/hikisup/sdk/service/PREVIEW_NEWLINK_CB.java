package com.oldwei.hikisup.sdk.service;

import com.oldwei.hikisup.sdk.structure.NET_EHOME_NEWLINK_CB_MSG;
import com.sun.jna.Callback;
import com.sun.jna.Pointer;

public interface PREVIEW_NEWLINK_CB extends Callback {
    boolean invoke(int lLinkHandle, NET_EHOME_NEWLINK_CB_MSG pNewLinkCBMsg, Pointer pUserData);
}