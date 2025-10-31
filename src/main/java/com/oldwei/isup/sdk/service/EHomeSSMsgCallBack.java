package com.oldwei.isup.sdk.service;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;

public interface EHomeSSMsgCallBack extends Callback {
    boolean invoke(int iHandle, int enumType, Pointer pOutBuffer, int dwOutLen, Pointer pInBuffer,
                   int dwInLen, Pointer pUser);
}
