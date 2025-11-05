package com.oldwei.isup.model.cb;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceEventBase {
    private String ipAddress;
    private String ipv6Address;
    private Integer portNo;
    private String protocol;
    private String macAddress;
    private Integer channelID;
    private String channelName;
    private String deviceID;
    private String devIndex;
    private String dateTime;
    private String eventType;
    private String eventState;
    private String eventDescription;
    private Integer activePostCount;
}

