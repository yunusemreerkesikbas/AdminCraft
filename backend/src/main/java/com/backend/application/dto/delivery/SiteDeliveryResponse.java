package com.backend.application.dto.delivery;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SiteDeliveryResponse(
    String siteName,
    String siteTitle,
    String siteDescription,
    String siteKeywords,
    String ogImageUrl,
    String defaultLanguage,
    List<LanguageInfo> enabledLanguages,
    String themeName,
    Boolean maintenanceMode,
    String maintenanceMessage,
    String googleAnalyticsId,
    String googleTagManagerId,
    String twitterHandle,
    String facebookPageUrl,
    String domain,
    String customDomain,
    Boolean sslEnabled) {

    public record LanguageInfo(String code, String nativeName, Boolean isRtl) {
    }
}
