package com.backend.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.backend.application.dto.request.CreateSiteRequest;
import com.backend.application.dto.request.UpdateSiteRequest;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.response.SiteResponse;

public interface SiteService {

    SiteResponse createSite(CreateSiteRequest request, Long userId, Language displayLanguage);

    Optional<SiteResponse> getSiteById(Long id, Language displayLanguage);

    SiteResponse updateSite(Long id, UpdateSiteRequest request, Long userId, Language displayLanguage);

    void deleteSite(Long id);

    List<SiteResponse> getAllSites(Language displayLanguage);

    List<SiteResponse> getSites(Language displayLanguage);

    long countSites();

    List<SiteResponse> getActiveSites(Language displayLanguage);

    Optional<SiteResponse> getSiteByDomain(String domain, Language displayLanguage);

    Optional<SiteResponse> getSiteByCustomDomain(String customDomain, Language displayLanguage);

    boolean isDomainAvailable(String domain);

    boolean isCustomDomainAvailable(String customDomain);

    SiteResponse updateDomain(Long id, String domain, Language displayLanguage);

    SiteResponse updateCustomDomain(Long id, String customDomain, Language displayLanguage);

    SiteResponse publishSite(Long id, Language displayLanguage);

    SiteResponse unpublishSite(Long id, Language displayLanguage);

    List<SiteResponse> getPublishedSites(Language displayLanguage);

    List<SiteResponse> getUnpublishedSites(Language displayLanguage);

    SiteResponse enableMaintenanceMode(Long id, String message, Language displayLanguage);

    SiteResponse disableMaintenanceMode(Long id, Language displayLanguage);

    List<SiteResponse> getSitesInMaintenanceMode(Language displayLanguage);

    SiteResponse addEnabledLanguage(Long id, Language language, Language displayLanguage);

    SiteResponse removeEnabledLanguage(Long id, Language language, Language displayLanguage);

    SiteResponse setDefaultLanguage(Long id, Language defaultLanguage, Language displayLanguage);

    List<SiteResponse> getSitesByLanguage(Language language, Language displayLanguage);

    List<SiteResponse> getSitesByDefaultLanguage(Language defaultLanguage, Language displayLanguage);

    SiteResponse updateTheme(Long id, String themeName, Language displayLanguage);

    List<SiteResponse> getSitesByTheme(String themeName, Language displayLanguage);

    List<String> getAvailableThemes();

    SiteResponse enableSSL(Long id, Language displayLanguage);

    SiteResponse disableSSL(Long id, Language displayLanguage);

    List<SiteResponse> getSSLEnabledSites(Language displayLanguage);

    List<SiteResponse> getSSLDisabledSites(Language displayLanguage);

    SiteResponse updateSEOSettings(Long id, String siteTitle, String siteDescription,
            String siteKeywords, Language displayLanguage);

    SiteResponse updateOGSettings(Long id, String ogImageUrl, Language displayLanguage);

    SiteResponse updateSocialSettings(Long id, String twitterHandle, String facebookPageUrl,
            Language displayLanguage);

    SiteResponse updateAnalyticsSettings(Long id, String googleAnalyticsId,
            String googleTagManagerId, Language displayLanguage);

    List<SiteResponse> getSitesWithAnalytics(Language displayLanguage);

    List<SiteResponse> searchSitesByName(String searchTerm, Language displayLanguage);

    List<SiteResponse> searchSitesByDescription(String searchTerm, Language displayLanguage);

    List<SiteResponse> getSitesCreatedBetween(LocalDateTime startDate, LocalDateTime endDate,
            Language displayLanguage);

    List<SiteResponse> getSitesPublishedBetween(LocalDateTime startDate, LocalDateTime endDate,
            Language displayLanguage);

    List<SiteResponse> getRecentlyPublishedSites(Language displayLanguage);

    boolean canSiteBePublished(Long id);

    boolean isSiteAccessible(Long id);

    boolean isSiteNameAvailable(String siteName);

    long getTotalSitesCount();

    long getPublishedSitesCount();

    long getMaintenanceSitesCount();

    long getSSLEnabledSitesCount();

    void bulkPublish(List<Long> siteIds);

    void bulkUnpublish(List<Long> siteIds);

    void bulkEnableSSL(List<Long> siteIds);

    void bulkDisableSSL(List<Long> siteIds);

    void bulkDelete(List<Long> siteIds);

    void deleteSites();

    SiteResponse getSiteConfiguration(Long id, Language displayLanguage);

    SiteResponse updateSiteConfiguration(Long id, UpdateSiteRequest request, Language displayLanguage);

    String getSiteUrl(Long id);

    String getFullDomainUrl(Long id);

    List<String> getAllSiteUrls();
}
