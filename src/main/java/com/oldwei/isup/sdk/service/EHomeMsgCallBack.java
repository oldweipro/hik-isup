package com.oldwei.isup.sdk.service;

import com.oldwei.isup.sdk.structure.NET_EHOME_ALARM_MSG;
import com.sun.jna.Callback;
import com.sun.jna.Pointer;

public interface EHomeMsgCallBack extends Callback {
    boolean invoke(int iHandle, NET_EHOME_ALARM_MSG pAlarmMsg, Pointer pUser);
}
