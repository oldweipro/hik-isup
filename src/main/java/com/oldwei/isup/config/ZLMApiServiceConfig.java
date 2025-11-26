package com.oldwei.isup.config;

//import com.aizuda.zlm4j.core.ZLMApi;

import com.aizuda.zlm4j.core.ZLMApi;
import com.sun.jna.Native;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ZLMApiServiceConfig {


    @Bean
    public ZLMApi zlmApi() {
        ZLMApi zlmApi = Native.load("mk_api", ZLMApi.class);
        //初始化zmk服务器
        zlmApi.mk_env_init2(1, 1, 1, null, 0, 0, null, 0, null, null);
        //创建http服务器 0:失败,非0:端口号
        short httpPort = zlmApi.mk_http_server_start((short) 7788, 0);
        //创建rtsp服务器 0:失败,非0:端口号
        short rtspPort = zlmApi.mk_rtsp_server_start((short) 7554, 0);
        //创建rtmp服务器 0:失败,非0:端口号
        short rtmpPort = zlmApi.mk_rtmp_server_start((short) 7935, 0);
        if (httpPort > 0 && rtspPort > 0 && rtmpPort > 0) {
            log.info("ZLM服务启动成功 - HTTP端口:{}, RTSP端口:{}, RTMP端口:{}",
                    httpPort, rtspPort, rtmpPort);
        } else {
            throw new RuntimeException("ZLM服务端口启动失败");
        }
        log.info("ZLM API initialized.");
        return zlmApi;
    }
}
