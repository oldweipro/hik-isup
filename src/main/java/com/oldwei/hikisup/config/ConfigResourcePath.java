package com.oldwei.hikisup.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ConfigResourcePath {

    @Value("classpath:config.properties")
    private Resource configResource;

}
