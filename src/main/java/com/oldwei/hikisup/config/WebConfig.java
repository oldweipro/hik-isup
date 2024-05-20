package com.oldwei.hikisup.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置反向代理的静态资源目录
        registry.addResourceHandler("/video/**")
                .addResourceLocations("file:C:\\Users\\klf\\IdeaProjects\\hik-isup\\");
    }
}
