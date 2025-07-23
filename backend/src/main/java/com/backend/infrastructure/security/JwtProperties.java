package com.backend.infrastructure.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    private String secret = "AdminCraftSecretKeyForJWTTokenGeneration2024!@#$%^&*()";
    private long expiration = 86400000L; // 24 hours
    private long refreshExpiration = 604800000L; // 7 days
}