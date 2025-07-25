package com.backend.infrastructure.security;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Slf4j
@Data
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    private String secret;
    private long expiration = 86400000L; // 24 hours
    private long refreshExpiration = 604800000L; // 7 days
    
    @PostConstruct
    public void validate() {
        log.debug("Validating JWT properties - secret length: {}, expiration: {}, refreshExpiration: {}", 
                 secret != null ? secret.length() : "null", expiration, refreshExpiration);
        
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException("JWT secret must be configured in application properties. " +
                    "Please set app.jwt.secret in application.yml or JWT_SECRET environment variable.");
        }
        if (secret.length() < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 characters long for security");
        }
        if (secret.matches(".*[dD]efault.*|.*[eE]xample.*|.*[tT]est.*")) {
            throw new IllegalStateException("JWT secret appears to be a default/example value");
        }
        if (expiration < 300000) { // 5 minutes minimum
            throw new IllegalStateException("Access token expiration too short (minimum 5 minutes)");
        }
        if (refreshExpiration < expiration) {
            throw new IllegalStateException("Refresh token expiration must be longer than access token");
        }
        
        log.info("JWT properties validated successfully - using secret with {} characters", secret.length());
    }
}