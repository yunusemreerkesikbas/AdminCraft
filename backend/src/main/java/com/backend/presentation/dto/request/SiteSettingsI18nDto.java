package com.backend.presentation.dto.request;

import jakarta.validation.constraints.Size;

public record SiteSettingsI18nDto(
        @Size(max = 100, message = "validation.length") String siteName,

        @Size(max = 160, message = "validation.length") String tagline,

        @jakarta.validation.Valid SeoDto seo) {

    public SiteSettingsI18nDto {
        siteName = sanitize(siteName);
        tagline = sanitize(tagline);
    }

    private static String sanitize(String value) {
        if (value == null)
            return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public record SeoDto(
            @Size(max = 200) String title,
            @Size(max = 500) String description,
            java.util.List<String> keywords,
            @Size(max = 200) String ogTitle,
            @Size(max = 500) String ogDescription,
            @Size(max = 50) String twitterCard) {
    }
}
