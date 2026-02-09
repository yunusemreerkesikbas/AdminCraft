package com.backend.presentation.dto.response;

import com.backend.infrastructure.persistence.platform.entity.PlatformSettings;

public record PlatformSettingsResponse(
    String platformName,
    String defaultLanguage,
    String defaultCurrency,
    String emailFromAddress,
    String emailFromName
) {
    public static PlatformSettingsResponse from(PlatformSettings entity) {
        return new PlatformSettingsResponse(
            entity.getPlatformName(),
            entity.getDefaultLanguage(),
            entity.getDefaultCurrency(),
            entity.getEmailFromAddress(),
            entity.getEmailFromName()
        );
    }
}
