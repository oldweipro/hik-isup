package com.oldwei.isup.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "hik.platform")
public class HikPlatformProperties {

    private String pushAddress;

}
