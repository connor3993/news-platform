package com.connor.newsplatform.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "media.jwt")
public class JwtProperties {
    private String adminSecretKey = "media-admin-secret";
    private String userSecretKey = "media-user-secret";
    private long ttl = 7200000L;
}
