package com.backend.application.dto.config;

import java.time.LocalDateTime;

public record ConfigRecaptchaResult(
        boolean enabled,
        String siteKeyMasked,
        boolean secretConfigured,
        LocalDateTime updatedAt
) {
}
