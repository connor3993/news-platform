package com.connor.newsplatform.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@MapperScan("com.connor.newsplatform.dao")
@ConfigurationPropertiesScan(basePackages = "com.connor.newsplatform.common.properties")
@SpringBootApplication(scanBasePackages = "com.connor.newsplatform")
public class MediaNewsApplication {
    public static void main(String[] args) {
        SpringApplication.run(MediaNewsApplication.class, args);
    }
}
