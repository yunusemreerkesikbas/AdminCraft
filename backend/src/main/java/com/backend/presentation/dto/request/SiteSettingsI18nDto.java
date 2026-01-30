package com.backend.presentation.dto.request;

import jakarta.validation.constraints.Size;

public record SiteSettingsI18nDto(
                @Size(max = 100, message = "validation.length") String siteName,

                @Size(max = 160, message = "validation.length") String tagline,

                SeoDto seo,

                @Size(max = 500, message = "validation.length") String footerText,

                @Size(max = 200, message = "validation.length") String headerTopbarText,

                String addressLocalized // JSON string
) {
        public record SeoDto(
                        @Size(max = 200) String title,
                        @Size(max = 500) String description,
                        java.util.List<String> keywords,
                        @Size(max = 200) String ogTitle,
                        @Size(max = 500) String ogDescription,
                        @Size(max = 50) String twitterCard) {
        }
}
