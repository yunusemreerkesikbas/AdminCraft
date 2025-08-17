package com.backend.application.service;

import com.backend.domain.entity.Site;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.request.CreateSiteRequest;
import com.backend.presentation.dto.request.UpdateSiteRequest;
import com.backend.presentation.dto.response.SiteResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SiteService {
    
    // Basic CRUD operations
    SiteResponse createSite(CreateSiteRequest request, Language displayLanguage);
    Optional<SiteResponse> getSiteById(Long id, Language displayLanguage);
    SiteResponse updateSite(Long id, UpdateSiteRequest request, Language displayLanguage);
    void deleteSite(Long id);
    List<SiteResponse> getAllSites(Language displayLanguage);
    
    // Tenant-specific operations
    List<SiteResponse> getSitesByTenantId(Long tenantId, Language displayLanguage);
    long countSitesByTenantId(Long tenantId);
    List<SiteResponse> getActiveSitesByTenantId(Long tenantId, Language displayLanguage);
    
    // Domain operations
    Optional<SiteResponse> getSiteByDomain(String domain, Language displayLanguage);
    Optional<SiteResponse> getSiteByCustomDomain(String customDomain, Language displayLanguage);
    boolean isDomainAvailable(String domain);
    boolean isCustomDomainAvailable(String customDomain);
    SiteResponse updateDomain(Long id, String domain, Language displayLanguage);
    SiteResponse updateCustomDomain(Long id, String customDomain, Language displayLanguage);
    
    // Publishing operations
    SiteResponse publishSite(Long id, Language displayLanguage);
    SiteResponse unpublishSite(Long id, Language displayLanguage);
    List<SiteResponse> getPublishedSites(Language displayLanguage);
    List<SiteResponse> getUnpublishedSitesByTenantId(Long tenantId, Language displayLanguage);
    
    // Maintenance operations
    SiteResponse enableMaintenanceMode(Long id, String message, Language displayLanguage);
    SiteResponse disableMaintenanceMode(Long id, Language displayLanguage);
    List<SiteResponse> getSitesInMaintenanceMode(Language displayLanguage);
    List<SiteResponse> getSitesInMaintenanceModeByTenantId(Long tenantId, Language displayLanguage);
    
    // Language operations
    SiteResponse addEnabledLanguage(Long id, Language language, Language displayLanguage);
    SiteResponse removeEnabledLanguage(Long id, Language language, Language displayLanguage);
    SiteResponse setDefaultLanguage(Long id, Language defaultLanguage, Language displayLanguage);
    List<SiteResponse> getSitesByLanguage(Language language, Language displayLanguage);
    List<SiteResponse> getSitesByDefaultLanguage(Language defaultLanguage, Language displayLanguage);
    
    // Theme operations
    SiteResponse updateTheme(Long id, String themeName, Language displayLanguage);
    List<SiteResponse> getSitesByTheme(String themeName, Language displayLanguage);
    List<String> getAvailableThemes();
    
    // SSL operations
    SiteResponse enableSSL(Long id, Language displayLanguage);
    SiteResponse disableSSL(Long id, Language displayLanguage);
    List<SiteResponse> getSSLEnabledSites(Language displayLanguage);
    List<SiteResponse> getSSLDisabledSitesByTenantId(Long tenantId, Language displayLanguage);
    
    // SEO operations
    SiteResponse updateSEOSettings(Long id, String siteTitle, String siteDescription, 
                                  String siteKeywords, Language displayLanguage);
    SiteResponse updateOGSettings(Long id, String ogImageUrl, Language displayLanguage);
    SiteResponse updateSocialSettings(Long id, String twitterHandle, String facebookPageUrl, 
                                     Language displayLanguage);
    
    // Analytics operations
    SiteResponse updateAnalyticsSettings(Long id, String googleAnalyticsId, 
                                        String googleTagManagerId, Language displayLanguage);
    List<SiteResponse> getSitesWithAnalytics(Language displayLanguage);
    List<SiteResponse> getSitesWithAnalyticsByTenantId(Long tenantId, Language displayLanguage);
    
    // Search operations
    List<SiteResponse> searchSitesByName(Long tenantId, String searchTerm, Language displayLanguage);
    List<SiteResponse> searchSitesByDescription(Long tenantId, String searchTerm, Language displayLanguage);
    
    // Date range operations
    List<SiteResponse> getSitesCreatedBetween(LocalDateTime startDate, LocalDateTime endDate, 
                                             Language displayLanguage);
    List<SiteResponse> getSitesPublishedBetween(LocalDateTime startDate, LocalDateTime endDate, 
                                               Language displayLanguage);
    List<SiteResponse> getRecentlyPublishedSites(Long tenantId, Language displayLanguage);
    
    // Validation operations
    boolean canSiteBePublished(Long id);
    boolean isSiteAccessible(Long id);
    boolean isSiteNameAvailable(String siteName, Long tenantId);
    
    // Statistics operations
    long getTotalSitesCount();
    long getPublishedSitesCount();
    long getMaintenanceSitesCount();
    long getSSLEnabledSitesCount();
    
    // Bulk operations
    void bulkPublish(List<Long> siteIds);
    void bulkUnpublish(List<Long> siteIds);
    void bulkEnableSSL(List<Long> siteIds);
    void bulkDisableSSL(List<Long> siteIds);
    void bulkDelete(List<Long> siteIds);
    void deleteSitesByTenantId(Long tenantId);
    
    // Configuration operations
    SiteResponse getSiteConfiguration(Long id, Language displayLanguage);
    SiteResponse updateSiteConfiguration(Long id, UpdateSiteRequest request, Language displayLanguage);
    
    // URL operations
    String getSiteUrl(Long id);
    String getFullDomainUrl(Long id);
    List<String> getAllSiteUrls(Long tenantId);
}