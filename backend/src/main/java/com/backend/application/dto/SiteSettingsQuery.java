package com.backend.application.dto;

import com.backend.domain.enums.Language;
import java.util.Map;

/**
 * Application layer DTO for site settings queries
 * This prevents Application layer from depending on Presentation layer DTOs
 */
public record SiteSettingsQuery(
    GlobalSettings global,
    Map<Language, I18nSettings> languages
) {
    
    public record GlobalSettings(
        String contactEmail,
        String contactPhone,
        String whatsappPhone,
        String canonicalBaseUrl,
        String robots
    ) {}
    
    public record I18nSettings(
        String siteName,
        String tagline,
        String seoTitle,
        String seoDescription,
        String footerText,
        String headerTopbarText
    ) {}
}