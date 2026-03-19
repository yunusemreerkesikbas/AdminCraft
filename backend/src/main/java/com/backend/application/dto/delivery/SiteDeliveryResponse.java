package com.backend.application.dto.delivery;

import java.util.List;
import java.util.stream.Collectors;

import com.backend.domain.entity.Site;
import com.backend.domain.enums.Language;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SiteDeliveryResponse(
    String siteName,
    String ogImageUrl,
    SeoInfo seo,
    String canonicalBaseUrl,
    String defaultLanguage,
    List<LanguageInfo> enabledLanguages,
    SearchEngineInfo searchEngine,
    String themeName,
    Boolean maintenanceMode,
    String maintenanceMessage,
    String logoUrl,
    String logoDarkUrl,
    ContactInfo contact,
    AddressInfo address,
    SocialLinksInfo social,
    I18nInfo i18n) {

    public static SiteDeliveryResponse from(
            Site site,
            String logoUrl,
            String logoDarkUrl,
            SeoInfo seo,
            String canonicalBaseUrl,
            SearchEngineInfo searchEngine,
            ContactInfo contact,
            AddressInfo address,
            SocialLinksInfo social,
            I18nInfo i18n) {
        List<LanguageInfo> enabledLanguages = site.getEnabledLanguages().stream()
                .map(LanguageInfo::from)
                .collect(Collectors.toList());
        return new SiteDeliveryResponse(
                site.getSiteName(),
                site.getOgImageUrl(),
                seo,
                canonicalBaseUrl,
                site.getDefaultLanguage().name(),
                enabledLanguages,
                searchEngine,
                site.getThemeName(),
                site.getMaintenanceMode(),
                site.getMaintenanceMessage(),
                logoUrl,
                logoDarkUrl,
                contact,
                address,
                social,
                i18n);
    }

    public record SeoInfo(
            String title,
            String description,
            List<String> keywords,
            String ogTitle,
            String ogDescription,
            String twitterCard,
            String titleSeparator) {
    }

    public record SearchEngineInfo(
            Boolean sitemapEnabled,
            Boolean indexingEnabled,
            String defaultRobots) {
    }

    public record LanguageInfo(String code, String nativeName, Boolean isRtl) {

        public static LanguageInfo from(Language lang) {
            return new LanguageInfo(lang.name(), lang.getNativeName(), lang.isRightToLeft());
        }
    }

    public record ContactInfo(String email, String phone, String whatsapp) {}

    public record AddressInfo(
            String line1, String line2, String city, String state,
            String postalCode, String country, String mapEmbedUrl) {}

    public record SocialLinksInfo(
            String facebook, String instagram, String x,
            String linkedin, String youtube, String tiktok) {}

    public record I18nInfo(String siteName, String tagline) {}
}
