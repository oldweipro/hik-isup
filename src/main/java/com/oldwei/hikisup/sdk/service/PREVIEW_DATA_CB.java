package com.oldwei.hikisup.sdk.service;

import com.oldwei.hikisup.sdk.structure.NET_EHOME_PREVIEW_CB_MSG;
import com.sun.jna.Callback;
import com.sun.jna.Pointer;

public interface PREVIEW_DATA_CB extends Callback {
    void invoke(int iPreviewHandle, NET_EHOME_PREVIEW_CB_MSG pPreviewCBMsg, Pointer pUserData);
}