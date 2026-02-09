package com.backend.application.dto.response;

public record PlatformSettingsData(
        String platformName,
        String defaultLanguage,
        String defaultCurrency,
        String emailFromAddress,
        String emailFromName) {
}
