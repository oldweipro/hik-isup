package com.oldwei.hikisup.config;

import com.oldwei.hikisup.sdk.SdkService.CmsService.CmsDemo;
import com.oldwei.hikisup.sdk.SdkService.StreamService.StreamDemo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.io.IOException;

@Configuration
public class ISUPServiceConfig {

    @Order(0)
    @Bean
    public CmsDemo cmsDemo() throws IOException {
        CmsDemo cmsDemo = new CmsDemo();
        cmsDemo.cMS_Init();
        cmsDemo.startCmsListen();
        return cmsDemo;
    }
    @Bean
    public StreamDemo streamDemo() {
        StreamDemo streamDemo = new StreamDemo();
        streamDemo.eStream_Init();
        return streamDemo;
    }
}
