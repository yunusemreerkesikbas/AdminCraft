package com.backend.application.dto.config;

import java.time.LocalDateTime;

public record ConfigPropertyResult(
        String key,
        String value,
        boolean secret,
        LocalDateTime updatedAt,
        Long updatedBy
) {
}
