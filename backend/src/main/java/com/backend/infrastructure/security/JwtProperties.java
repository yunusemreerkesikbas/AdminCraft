package com.backend.infrastructure.security;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.io.FileWriter;
import java.io.IOException;

@Slf4j
@Data
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    private String secret;
    private long expiration = 86400000L; // 24 hours
    private long refreshExpiration = 604800000L; // 7 days
    
    @PostConstruct
    public void validate() {
        // #region agent log
        try (FileWriter fw = new FileWriter("debug-fc33e6.log", true)) {
            fw.write("{\"sessionId\":\"fc33e6\",\"runId\":\"pre-fix\",\"hypothesisId\":\"H1\",\"location\":\"JwtProperties.java:validate:entry\",\"message\":\"JwtProperties validate entry\",\"data\":{\"secretPresent\":" 
                    + (secret != null) 
                    + ",\"secretLength\":" 
                    + (secret != null ? secret.length() : 0) 
                    + "},\"timestamp\":" 
                    + System.currentTimeMillis() 
                    + "}\n");
        } catch (IOException ignored) {}
        // #endregion

        log.debug("Validating JWT properties - secret length: {}, expiration: {}, refreshExpiration: {}", 
                 secret != null ? secret.length() : "null", expiration, refreshExpiration);
        
        if (secret == null || secret.trim().isEmpty()) {
            // #region agent log
            try (FileWriter fw = new FileWriter("debug-fc33e6.log", true)) {
                fw.write("{\"sessionId\":\"fc33e6\",\"runId\":\"pre-fix\",\"hypothesisId\":\"H1\",\"location\":\"JwtProperties.java:validate:secret-null\",\"message\":\"JWT secret null or empty\",\"data\":{},\"timestamp\":" 
                        + System.currentTimeMillis() 
                        + "}\n");
            } catch (IOException ignored) {}
            // #endregion
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