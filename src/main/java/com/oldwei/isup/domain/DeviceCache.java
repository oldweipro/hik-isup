package com.oldwei.isup.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class DeviceCache implements Serializable {
    @Serial
    private static final long serialVersionUID = 3650463480897594897L;
    /**
     * 设备id
     */
    private String deviceId;
    /**
     * 是否推流
     */
    private int isPushed;
    /**
     * 是否在线
     */
    private int isOnline;
    /**
     * 登录句柄
     */
    private int lLoginID;
    /**
     * 通道号
     */
    private int lChannel;
    /**
     * sessionId
     */
    private int sessionId;
}
