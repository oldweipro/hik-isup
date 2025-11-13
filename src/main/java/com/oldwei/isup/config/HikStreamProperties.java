package com.oldwei.isup.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "hik.stream")
public class HikStreamProperties {

    private Rtmp rtmp;
    private Http http;

    @Data
    public static class Rtmp {
        private String ip;
        private String port;
        private String listenIp;
    }

    @Data
    public static class Http {
        private String ip;
        private String port;
    }
}
