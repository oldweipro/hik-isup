package com.oldwei.hikisup.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "hik.isup")
public class HikIsupProperties {

    private CmsServer cmsServer;
    private DasServer dasServer;
    private SmsServer smsServer;
    private SmsBackServer smsBackServer;
    private VoiceSmsServer voiceSmsServer;
    private AlarmServer alarmServer;
    private PicServer picServer;

    private String isupKey;
    private String eventInfoPrintType;

    @Data
    public static class CmsServer {
        private String ip;
        private String port;
    }

    @Data
    public static class DasServer {
        private String ip;
        private String port;
    }

    @Data
    public static class SmsServer {
        private String ip;
        private String port;
        private String listenIp;
        private String listenPort;
    }

    @Data
    public static class SmsBackServer {
        private String ip;
        private String port;
        private String listenIp;
        private String listenPort;
    }

    @Data
    public static class VoiceSmsServer {
        private String ip;
        private String port;
        private String listenIp;
        private String listenPort;
    }

    @Data
    public static class AlarmServer {
        private String ip;
        private String tcpPort;
        private String udpPort;
        private String type;
        private String listenIp;
        private String listenTcpPort;
        private String listenUdpPort;
    }

    @Data
    public static class PicServer {
        private String ip;
        private String port;
        private String type;
        private String listenIp;
        private String listenPort;
    }
}
