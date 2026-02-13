package com.backend.application.dto.delivery;

import java.util.List;
import java.util.stream.Collectors;

import com.backend.domain.entity.Site;
import com.backend.domain.enums.Language;
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

    public static SiteDeliveryResponse from(Site site) {
        List<LanguageInfo> enabledLanguages = site.getEnabledLanguages().stream()
                .map(LanguageInfo::from)
                .collect(Collectors.toList());
        return new SiteDeliveryResponse(
                site.getSiteName(),
                site.getSiteTitle(),
                site.getSiteDescription(),
                site.getSiteKeywords(),
                site.getOgImageUrl(),
                site.getDefaultLanguage().name(),
                enabledLanguages,
                site.getThemeName(),
                site.getMaintenanceMode(),
                site.getMaintenanceMessage(),
                site.getGoogleAnalyticsId(),
                site.getGoogleTagManagerId(),
                site.getTwitterHandle(),
                site.getFacebookPageUrl(),
                site.getDomain(),
                site.getCustomDomain(),
                site.getSslEnabled());
    }

    public record LanguageInfo(String code, String nativeName, Boolean isRtl) {

        public static LanguageInfo from(Language lang) {
            return new LanguageInfo(lang.name(), lang.getNativeName(), lang.isRightToLeft());
        }
    }
}
