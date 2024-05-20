package com.oldwei.hikisup.sdk.service;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;

public interface DEVICE_REGISTER_CB extends Callback {
    boolean invoke(int lUserID, int dwDataType, Pointer pOutBuffer, int dwOutLen, Pointer pInBuffer, int dwInLen, Pointer pUser);
}
