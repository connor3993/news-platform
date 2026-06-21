package com.connor.newsplatform.server.config;

import com.connor.newsplatform.common.properties.OssProperties;
import com.connor.newsplatform.common.utils.OssUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OssConfig {
    @Bean
    public OssUtil ossUtil(OssProperties ossProperties) {
        return new OssUtil(ossProperties);
    }
}
