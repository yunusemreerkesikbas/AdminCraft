package com.backend.presentation.dto.request;

import com.backend.domain.enums.Language;
import jakarta.validation.constraints.*;

import java.util.Set;

public record CreateSiteRequest(
    @NotBlank(message = "validation.site.name.required")
    @Size(min = 3, max = 100, message = "validation.site.name.size")
    String siteName,
    
    @Size(max = 500, message = "validation.site.description.size")
    String description,
    
    @NotNull(message = "validation.tenant.id.required")
    Long tenantId,
    
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
    
    public CreateSiteRequest(
        String siteName,
        String description,
        Long tenantId,
        String domain,
        String customDomain,
        Language defaultLanguage,
        Set<Language> enabledLanguages,
        String themeName,
        Boolean sslEnabled,
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
        this.description = description != null ? description.trim() : null;
        this.tenantId = tenantId;
        this.domain = normalizeDomain(domain);
        this.customDomain = normalizeDomain(customDomain);
        this.themeName = normalizeStringWithDefault(themeName, "default");
        this.sslEnabled = sslEnabled != null ? sslEnabled : true;
        this.siteTitle = siteTitle != null ? siteTitle.trim() : null;
        this.siteDescription = siteDescription != null ? siteDescription.trim() : null;
        this.siteKeywords = siteKeywords != null ? siteKeywords.trim() : null;
        this.ogImageUrl = ogImageUrl != null ? ogImageUrl.trim() : null;
        this.twitterHandle = normalizeTwitterHandle(twitterHandle);
        this.facebookPageUrl = facebookPageUrl != null ? facebookPageUrl.trim() : null;
        this.googleAnalyticsId = googleAnalyticsId != null ? googleAnalyticsId.trim() : null;
        this.googleTagManagerId = googleTagManagerId != null ? googleTagManagerId.trim() : null;
        
        // Handle language logic
        var languageResult = normalizeLanguages(defaultLanguage, enabledLanguages);
        this.defaultLanguage = languageResult.defaultLang();
        this.enabledLanguages = languageResult.enabledLangs();
    }
    
    private static String normalizeString(String value) {
        return value != null ? value.trim() : null;
    }
    
    private static String normalizeStringWithDefault(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }
    
    private static String normalizeDomain(String domain) {
        if (domain == null) return null;
        return domain.trim().toLowerCase();
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
    
    private static LanguageResult normalizeLanguages(Language defaultLanguage, Set<Language> enabledLanguages) {
        // If no default language provided, use TR as fallback
        Language finalDefault = (defaultLanguage != null) ? defaultLanguage : Language.TR;
        
        // If no enabled languages provided, use default language
        if (enabledLanguages == null || enabledLanguages.isEmpty()) {
            return new LanguageResult(finalDefault, Set.of(finalDefault));
        }
        
        // Ensure default language is in enabled languages
        if (!enabledLanguages.contains(finalDefault)) {
            var mutableLanguages = new java.util.HashSet<>(enabledLanguages);
            mutableLanguages.add(finalDefault);
            return new LanguageResult(finalDefault, Set.copyOf(mutableLanguages));
        }
        
        return new LanguageResult(finalDefault, enabledLanguages);
    }
    
    private record LanguageResult(Language defaultLang, Set<Language> enabledLangs) {}
}