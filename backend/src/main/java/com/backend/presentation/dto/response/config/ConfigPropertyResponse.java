package com.backend.presentation.dto.response.config;

import java.time.LocalDateTime;

import com.backend.application.dto.config.ConfigPropertyResult;

public record ConfigPropertyResponse(
        String key,
        String value,
        boolean secret,
        LocalDateTime updatedAt
) {
    public static ConfigPropertyResponse from(ConfigPropertyResult result) {
        return new ConfigPropertyResponse(
                result.key(),
                result.secret() ? null : result.value(),
                result.secret(),
                result.updatedAt());
    }
}
