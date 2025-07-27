package com.backend.application.service;

import com.backend.domain.entity.Site;
import com.backend.domain.entity.Menu;
import com.backend.domain.enums.Language;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SiteService {
    
    // Basic CRUD operations
    Site createSite(Site site);
    Optional<Site> getSiteById(Long id);
    Site updateSite(Site site);
    void deleteSite(Long id);
    List<Site> getAllSites();
    
    // Tenant-specific operations
    List<Site> getSitesByTenantId(Long tenantId);
    Optional<Site> getDefaultSiteByTenantId(Long tenantId);
    long countSitesByTenantId(Long tenantId);
    
    // Domain operations
    Optional<Site> getSiteByDomain(String domain);
    boolean isDomainAvailable(String domain);
    Site updateDomain(Long siteId, String domain);
    List<Site> getSitesWithCustomDomain(Long tenantId);
    
    // Language operations
    Site addLanguage(Long siteId, Language language);
    Site removeLanguage(Long siteId, Language language);
    Site setDefaultLanguage(Long siteId, Language language);
    Set<Language> getSupportedLanguages(Long siteId);
    boolean isLanguageSupported(Long siteId, Language language);
    
    // Publishing operations
    Site publishSite(Long siteId);
    Site unpublishSite(Long siteId);
    Site activateSite(Long siteId);
    Site deactivateSite(Long siteId);
    List<Site> getPublishedSites(Long tenantId);
    List<Site> getActiveSites(Long tenantId);
    
    // Theme and branding operations
    Site updateTheme(Long siteId, String theme);
    Site updateLogo(Long siteId, String logoUrl);
    Site updateFavicon(Long siteId, String faviconUrl);
    Site updateBranding(Long siteId, String primaryColor, String secondaryColor, String fontFamily);
    List<String> getAvailableThemes();
    
    // Menu operations
    List<Menu> getMenusBySiteId(Long siteId);
    List<Menu> getMenusBySiteIdAndLanguage(Long siteId, Language language);
    Menu createMenu(Menu menu);
    Menu updateMenu(Menu menu);
    void deleteMenu(Long menuId);
    
    // SEO operations
    Site updateSeoSettings(Long siteId, String metaTitle, String metaDescription, String metaKeywords);
    Site updateAnalyticsCode(Long siteId, String googleAnalyticsId, String customCode);
    
    // Statistics and analytics
    long getTotalSitesCount();
    long getActiveSitesCount(Long tenantId);
    long getPublishedSitesCount(Long tenantId);
    List<Site> getRecentSites(Long tenantId, int limit);
    
    // Validation
    List<String> validateSite(Site site);
    boolean canPublish(Long siteId);
    boolean canActivate(Long siteId);
    
    // Bulk operations
    void bulkPublish(List<Long> siteIds);
    void bulkUnpublish(List<Long> siteIds);
    void bulkActivate(List<Long> siteIds);
    void bulkDeactivate(List<Long> siteIds);
    void deleteSitesByTenantId(Long tenantId);
}