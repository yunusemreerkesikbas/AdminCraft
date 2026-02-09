package com.backend.presentation.dto.response;

import com.backend.application.dto.response.PlatformSettingsData;

public record PlatformSettingsResponse(
    String platformName,
    String defaultLanguage,
    String defaultCurrency,
    String emailFromAddress,
    String emailFromName
) {
    public static PlatformSettingsResponse from(PlatformSettingsData entity) {
        return new PlatformSettingsResponse(
            entity.platformName(),
            entity.defaultLanguage(),
            entity.defaultCurrency(),
            entity.emailFromAddress(),
            entity.emailFromName()
        );
    }
}
