package com.oldwei.isup.sdk.service;

import com.oldwei.isup.sdk.structure.NET_EHOME_SS_EX_PARAM;
import com.oldwei.isup.sdk.structure.NET_EHOME_SS_RW_PARAM;
import com.sun.jna.Callback;

public interface EHomeSSRWCallBackEx extends Callback {
    public boolean invoke(int iHandle, NET_EHOME_SS_RW_PARAM pRwParam, NET_EHOME_SS_EX_PARAM pExStruct);
}
