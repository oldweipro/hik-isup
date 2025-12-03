package com.oldwei.isup.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "hik.stream")
public class HikStreamProperties {

    private Boolean isSSL;
    private String domain;
    private Http http;

    @Data
    public static class Http {
        private String ip;
        private String port;
    }
}
