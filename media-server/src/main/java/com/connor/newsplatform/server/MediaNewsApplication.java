package com.connor.newsplatform.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@MapperScan("com.connor.newsplatform.server.mapper")
@ConfigurationPropertiesScan(basePackages = "com.connor.newsplatform.common.properties")
@SpringBootApplication(scanBasePackages = "com.connor.newsplatform")
public class MediaNewsApplication {
    public static void main(String[] args) {
        SpringApplication.run(MediaNewsApplication.class, args);
    }
}
