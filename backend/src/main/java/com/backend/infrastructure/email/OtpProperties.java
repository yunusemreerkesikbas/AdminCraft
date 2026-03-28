package com.backend.infrastructure.email;

import java.util.Arrays;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "app.otp")
public class OtpProperties {

    private final Environment environment;

    private int length = 6;
    private int expirySeconds = 300;
    private int maxAttempts = 5;
    private String bypassCode = null;

    public OtpProperties(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void validateBypassCode() {
        if (bypassCode != null && !bypassCode.isBlank()) {
            boolean isAllowedProfile = Arrays.stream(environment.getActiveProfiles())
                    .anyMatch(profile -> "dev".equals(profile) || "stage".equals(profile));
            if (!isAllowedProfile) {
                log.error("SECURITY ALERT: OTP bypass code configured outside dev/stage profiles! Disabling.");
                bypassCode = null;
            } else {
                log.warn("SECURITY WARNING: OTP bypass code is enabled in dev/stage profile.");
            }
        }
    }
}
