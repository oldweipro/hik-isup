package com.oldwei.isup.sdk.service;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;

public interface EHomeSSStorageCallBack extends Callback {
    boolean invoke(int iHandle, String pFileName, Pointer pFileBuf, int dwOutLen, Pointer pFilePath,
                   Pointer pUser);
}
