package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.Site;
import com.backend.domain.enums.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SiteJpaRepository extends JpaRepository<Site, Long> {
    
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
    @Query("SELECT s FROM Site s WHERE s.tenantId = :tenantId AND :language MEMBER OF s.enabledLanguages")
    List<Site> findByTenantIdAndEnabledLanguagesContaining(@Param("tenantId") Long tenantId, @Param("language") Language language);
    
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
    @Query("SELECT s FROM Site s WHERE s.tenantId = :tenantId AND s.googleAnalyticsId IS NOT NULL")
    List<Site> findByTenantIdWithAnalytics(@Param("tenantId") Long tenantId);
    
    @Query("SELECT s FROM Site s WHERE s.tenantId = :tenantId AND s.googleTagManagerId IS NOT NULL")
    List<Site> findByTenantIdWithTagManager(@Param("tenantId") Long tenantId);
    
    // Custom business queries
    @Query("SELECT s FROM Site s WHERE s.tenantId = :tenantId AND s.published = true AND s.maintenanceMode = false")
    List<Site> findActiveSitesByTenantId(@Param("tenantId") Long tenantId);
    
    @Query("SELECT s FROM Site s WHERE s.published = true AND s.maintenanceMode = false")
    List<Site> findAllActiveSites();
    
    @Query("SELECT COUNT(s) FROM Site s WHERE s.tenantId = :tenantId AND s.published = true")
    long countPublishedSitesByTenantId(@Param("tenantId") Long tenantId);
    
    @Query("SELECT s FROM Site s WHERE s.tenantId = :tenantId AND s.publishedAt IS NOT NULL ORDER BY s.publishedAt DESC")
    List<Site> findRecentlyPublishedSitesByTenantId(@Param("tenantId") Long tenantId);
    
    // Bulk operations
    void deleteByTenantId(Long tenantId);
    List<Site> findByIdIn(List<Long> ids);
}