package com.backend.infrastructure.email;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "app.email-verification")
public class EmailVerificationProperties {

    private int expirySeconds = 86400; // 24 hours
}
