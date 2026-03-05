package com.backend.presentation.dto.response.config;

import java.time.LocalDateTime;

import com.backend.application.dto.config.ConfigRecaptchaResult;

public record ConfigRecaptchaResponse(
        boolean enabled,
        String siteKeyMasked,
        boolean secretConfigured,
        LocalDateTime updatedAt
) {
    public static ConfigRecaptchaResponse from(ConfigRecaptchaResult result) {
        return new ConfigRecaptchaResponse(
                result.enabled(),
                result.siteKeyMasked(),
                result.secretConfigured(),
                result.updatedAt());
    }
}
