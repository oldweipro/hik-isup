package com.oldwei.hikisup.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class DeviceRemoteControl implements Serializable {
    @Serial
    private static final long serialVersionUID = -4118835736556424637L;

    private int isOnline;// 0:离线 1:在线
    private int lChannel;// 通道号
}
