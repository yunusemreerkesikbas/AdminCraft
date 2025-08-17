package com.backend.presentation.dto.response;

import com.backend.domain.enums.Language;

import java.time.LocalDateTime;
import java.util.Set;

public record SiteResponse(
    Long id,
    String siteName,
    String description,
    Long tenantId,
    String tenantName,
    String domain,
    String customDomain,
    Set<Language> enabledLanguages,
    Language defaultLanguage,
    Boolean sslEnabled,
    Boolean published,
    String themeName,
    Boolean maintenanceMode,
    String maintenanceMessage,
    
    // SEO Information
    String siteTitle,
    String siteDescription,
    String siteKeywords,
    
    // Social Media Information
    String ogImageUrl,
    String twitterHandle,
    String facebookPageUrl,
    
    // Analytics Information
    String googleAnalyticsId,
    String googleTagManagerId,
    
    // URL Information
    String siteUrl,
    String fullDomain,
    
    // Status Information
    Boolean isAccessible,
    Boolean canBePublished,
    
    // Timestamps
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime publishedAt
) {
    
    public static SiteResponse of(
        Long id,
        String siteName,
        String description,
        Long tenantId,
        String tenantName,
        String domain,
        String customDomain,
        Set<Language> enabledLanguages,
        Language defaultLanguage,
        Boolean sslEnabled,
        Boolean published,
        String themeName,
        Boolean maintenanceMode,
        String maintenanceMessage,
        String siteTitle,
        String siteDescription,
        String siteKeywords,
        String ogImageUrl,
        String twitterHandle,
        String facebookPageUrl,
        String googleAnalyticsId,
        String googleTagManagerId,
        String siteUrl,
        String fullDomain,
        Boolean isAccessible,
        Boolean canBePublished,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime publishedAt
    ) {
        return new SiteResponse(
            id,
            siteName,
            description,
            tenantId,
            tenantName,
            domain,
            customDomain,
            enabledLanguages,
            defaultLanguage,
            sslEnabled,
            published,
            themeName,
            maintenanceMode,
            maintenanceMessage,
            siteTitle,
            siteDescription,
            siteKeywords,
            ogImageUrl,
            twitterHandle,
            facebookPageUrl,
            googleAnalyticsId,
            googleTagManagerId,
            siteUrl,
            fullDomain,
            isAccessible,
            canBePublished,
            createdAt,
            updatedAt,
            publishedAt
        );
    }
}