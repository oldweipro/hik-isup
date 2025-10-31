package com.oldwei.isup;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication
@MapperScan("${mybatis-plus.mapperPackage}")
public class HikIsupApplication {

    public static void main(String[] args) {
        SpringApplication.run(HikIsupApplication.class, args);
    }

}
