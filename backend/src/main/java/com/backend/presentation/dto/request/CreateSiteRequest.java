package com.backend.presentation.dto.request;

import java.util.Set;

import com.backend.domain.enums.Language;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateSiteRequest(
        @NotBlank(message = "validation.site.name.required") @Size(min = 3, max = 100, message = "validation.site.name.size") String siteName,

        @Size(max = 500, message = "validation.site.description.size") String description,

        @Size(max = 100, message = "validation.domain.size") @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*\\.[a-z]{2,}$", message = "validation.domain.pattern") String domain,

        @Size(max = 100, message = "validation.custom.domain.size") @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*\\.[a-z]{2,}$", message = "validation.custom.domain.pattern") String customDomain,

        Language defaultLanguage,

        Set<Language> enabledLanguages,

        @Size(max = 50, message = "validation.theme.name.size") String themeName,

        Boolean sslEnabled,

        // SEO Fields
        @Size(max = 60, message = "validation.site.title.size") String siteTitle,

        @Size(max = 160, message = "validation.site.description.meta.size") String siteDescription,

        @Size(max = 200, message = "validation.site.keywords.size") String siteKeywords,

        // Social Media Fields
        String ogImageUrl,

        @Size(max = 50, message = "validation.twitter.handle.size") String twitterHandle,

        String facebookPageUrl,

        // Analytics Fields
        @Size(max = 50, message = "validation.google.analytics.id.size") String googleAnalyticsId,

        @Size(max = 50, message = "validation.google.tag.manager.id.size") String googleTagManagerId) {
}