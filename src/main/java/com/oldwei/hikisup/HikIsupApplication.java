package com.oldwei.hikisup;

import org.bytedeco.ffmpeg.global.avutil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication
public class HikIsupApplication {

    public static void main(String[] args) {
        avutil.av_log_set_level(avutil.AV_LOG_QUIET);
        SpringApplication.run(HikIsupApplication.class, args);
    }

}
