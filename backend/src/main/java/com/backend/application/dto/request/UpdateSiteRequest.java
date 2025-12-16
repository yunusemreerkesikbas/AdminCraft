package com.backend.application.dto.request;

import com.backend.domain.enums.Language;
import jakarta.validation.constraints.*;

import java.util.Set;

public record UpdateSiteRequest(
    @Size(min = 3, max = 100, message = "validation.site.name.size")
    String siteName,
    
    @Size(max = 500, message = "validation.site.description.size")
    String description,
    
    @Size(max = 100, message = "validation.domain.size")
    @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*\\.[a-z]{2,}$", 
             message = "validation.domain.pattern")
    String domain,
    
    @Size(max = 100, message = "validation.custom.domain.size")
    @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*\\.[a-z]{2,}$", 
             message = "validation.custom.domain.pattern")
    String customDomain,
    
    Language defaultLanguage,
    
    Set<Language> enabledLanguages,
    
    @Size(max = 50, message = "validation.theme.name.size")
    String themeName,
    
    Boolean sslEnabled,
    
    Boolean published,
    
    Boolean maintenanceMode,
    
    @Size(max = 500, message = "validation.maintenance.message.size")
    String maintenanceMessage,
    
    // SEO Fields
    @Size(max = 60, message = "validation.site.title.size")
    String siteTitle,
    
    @Size(max = 160, message = "validation.site.description.meta.size")
    String siteDescription,
    
    @Size(max = 200, message = "validation.site.keywords.size")
    String siteKeywords,
    
    // Social Media Fields
    String ogImageUrl,
    
    @Size(max = 50, message = "validation.twitter.handle.size")
    String twitterHandle,
    
    String facebookPageUrl,
    
    // Analytics Fields
    @Size(max = 50, message = "validation.google.analytics.id.size")
    String googleAnalyticsId,
    
    @Size(max = 50, message = "validation.google.tag.manager.id.size")
    String googleTagManagerId
) {
    
    public UpdateSiteRequest(
        String siteName,
        String description,
        String domain,
        String customDomain,
        Language defaultLanguage,
        Set<Language> enabledLanguages,
        String themeName,
        Boolean sslEnabled,
        Boolean published,
        Boolean maintenanceMode,
        String maintenanceMessage,
        String siteTitle,
        String siteDescription,
        String siteKeywords,
        String ogImageUrl,
        String twitterHandle,
        String facebookPageUrl,
        String googleAnalyticsId,
        String googleTagManagerId
    ) {
        // Normalize and validate inputs
        this.siteName = normalizeString(siteName);
        this.description = normalizeString(description);
        this.domain = normalizeDomain(domain);
        this.customDomain = normalizeDomain(customDomain);
        this.defaultLanguage = defaultLanguage;
        this.enabledLanguages = enabledLanguages;
        this.themeName = normalizeString(themeName);
        this.sslEnabled = sslEnabled;
        this.published = published;
        this.maintenanceMode = maintenanceMode;
        this.maintenanceMessage = normalizeString(maintenanceMessage);
        this.siteTitle = normalizeString(siteTitle);
        this.siteDescription = normalizeString(siteDescription);
        this.siteKeywords = normalizeString(siteKeywords);
        this.ogImageUrl = normalizeString(ogImageUrl);
        this.twitterHandle = normalizeTwitterHandle(twitterHandle);
        this.facebookPageUrl = normalizeString(facebookPageUrl);
        this.googleAnalyticsId = normalizeString(googleAnalyticsId);
        this.googleTagManagerId = normalizeString(googleTagManagerId);
    }
    
    private static String normalizeString(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
    
    private static String normalizeDomain(String domain) {
        if (domain == null) return null;
        String trimmed = domain.trim().toLowerCase();
        return trimmed.isEmpty() ? null : trimmed;
    }
    
    private static String normalizeTwitterHandle(String handle) {
        if (handle == null) return null;
        String trimmed = handle.trim();
        // Remove @ if present
        if (trimmed.startsWith("@")) {
            trimmed = trimmed.substring(1);
        }
        return trimmed.isEmpty() ? null : trimmed;
    }
}
