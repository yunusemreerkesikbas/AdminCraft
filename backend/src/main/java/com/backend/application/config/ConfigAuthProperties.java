package com.backend.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.config-auth")
public record ConfigAuthProperties(
        boolean otpEnabled
) {
}
