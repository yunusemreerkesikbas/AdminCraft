package com.backend.application.dto.request;

public record ResponsiveMediaRequest(
        String code,
        Long desktopMediaId,
        Long mobileMediaId) {
    public ResponsiveMediaRequest {
        if (code != null) {
            code = code.trim();
        }
    }
}
