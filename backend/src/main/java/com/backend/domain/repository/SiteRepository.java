package com.backend.domain.repository;

import com.backend.domain.entity.Site;
import com.backend.domain.enums.Language;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SiteRepository {
    
    // Basic CRUD operations
    Site save(Site site);
    List<Site> saveAll(Iterable<Site> sites);
    Optional<Site> findById(Long id);
    List<Site> findAll();
    void deleteById(Long id);
    boolean existsById(Long id);
    long count();
    void deleteAllById(Iterable<Long> ids);
    
    // Basic queries
    List<Site> findAllByOrderByCreatedAtDesc();
    
    // Basic tenant-specific queries
    List<Site> findByTenantId(Long tenantId);
    List<Site> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
    long countByTenantId(Long tenantId);
    
    // Domain queries
    Optional<Site> findByDomain(String domain);
    Optional<Site> findByDomainIgnoreCase(String domain);
    Optional<Site> findByCustomDomain(String customDomain);
    boolean existsByDomain(String domain);
    boolean existsByDomainIgnoreCase(String domain);
    boolean existsByCustomDomain(String customDomain);
    boolean existsByCustomDomainIgnoreCase(String customDomain);
    
    // Site name validation
    boolean existsBySiteNameIgnoreCaseAndTenantId(String siteName, Long tenantId);
    
    // Status queries
    List<Site> findByPublishedTrue();
    List<Site> findByTenantIdAndPublishedTrue(Long tenantId);
    List<Site> findByTenantIdAndPublishedFalse(Long tenantId);
    List<Site> findByMaintenanceModeTrue();
    List<Site> findByTenantIdAndMaintenanceModeTrue(Long tenantId);
    
    // Language queries
    List<Site> findByTenantIdAndEnabledLanguagesContaining(Long tenantId, Language language);
    
    List<Site> findByDefaultLanguage(Language language);
    List<Site> findByTenantIdAndDefaultLanguage(Long tenantId, Language language);
    
    // Theme queries
    List<Site> findByThemeName(String themeName);
    List<Site> findByTenantIdAndThemeName(Long tenantId, String themeName);
    
    // SSL queries
    List<Site> findBySslEnabledTrue();
    List<Site> findByTenantIdAndSslEnabledTrue(Long tenantId);
    List<Site> findByTenantIdAndSslEnabledFalse(Long tenantId);
    
    // Search queries
    List<Site> findByTenantIdAndSiteNameContainingIgnoreCase(Long tenantId, String siteName);
    List<Site> findByTenantIdAndDescriptionContainingIgnoreCase(Long tenantId, String description);
    
    // Date range queries
    List<Site> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<Site> findByTenantIdAndCreatedAtBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate);
    List<Site> findByPublishedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<Site> findByTenantIdAndPublishedAtBetween(Long tenantId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Analytics queries
    List<Site> findByTenantIdWithAnalytics(Long tenantId);
    
    List<Site> findByTenantIdWithTagManager(Long tenantId);
    
    // Custom business queries
    List<Site> findActiveSitesByTenantId(Long tenantId);
    
    List<Site> findAllActiveSites();
    
    long countPublishedSitesByTenantId(Long tenantId);
    
    List<Site> findRecentlyPublishedSitesByTenantId(Long tenantId);
    
    // Bulk operations
    void deleteByTenantId(Long tenantId);
    List<Site> findByIdIn(List<Long> ids);
}