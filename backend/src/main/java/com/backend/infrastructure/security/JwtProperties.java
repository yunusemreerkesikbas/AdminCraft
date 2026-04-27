package com.backend.infrastructure.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    private String secret;
    private long expiration = 86400000L; // 24 hours
    private long refreshExpiration = 604800000L; // 7 days
    private long rememberMeExpiration = 2592000000L; // 30 days
    private Cookie cookie = new Cookie();

    @Data
    public static class Cookie {
        private String name = "craftive_rt";
        private String path = "/api/auth";
        private boolean secure = false;
        private String sameSite = "Strict";
    }
}