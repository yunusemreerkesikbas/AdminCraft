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

    List<Site> findAllByOrderByCreatedAtDesc();

    Optional<Site> findByDomain(String domain);
    Optional<Site> findByDomainIgnoreCase(String domain);
    Optional<Site> findByCustomDomain(String customDomain);
    boolean existsByDomain(String domain);
    boolean existsByDomainIgnoreCase(String domain);
    boolean existsByCustomDomain(String customDomain);
    boolean existsByCustomDomainIgnoreCase(String customDomain);

    boolean existsBySiteNameIgnoreCase(String siteName);

    List<Site> findByPublishedTrue();
    List<Site> findByPublishedFalse();
    List<Site> findByMaintenanceModeTrue();

    @Query("SELECT s FROM Site s WHERE :language MEMBER OF s.enabledLanguages")
    List<Site> findByEnabledLanguagesContaining(@Param("language") Language language);

    List<Site> findByDefaultLanguage(Language language);

    List<Site> findByThemeName(String themeName);

    List<Site> findBySslEnabledTrue();
    List<Site> findBySslEnabledFalse();

    List<Site> findBySiteNameContainingIgnoreCase(String siteName);
    List<Site> findByDescriptionContainingIgnoreCase(String description);

    List<Site> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<Site> findByPublishedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT s FROM Site s WHERE s.googleAnalyticsId IS NOT NULL")
    List<Site> findWithAnalytics();

    @Query("SELECT s FROM Site s WHERE s.googleTagManagerId IS NOT NULL")
    List<Site> findWithTagManager();

    @Query("SELECT s FROM Site s WHERE s.published = true AND s.maintenanceMode = false")
    List<Site> findActiveSites();

    @Query("SELECT s FROM Site s WHERE s.published = true AND s.maintenanceMode = false")
    List<Site> findAllActiveSites();

    @Query("SELECT COUNT(s) FROM Site s WHERE s.published = true")
    long countPublishedSites();

    @Query("SELECT s FROM Site s WHERE s.publishedAt IS NOT NULL ORDER BY s.publishedAt DESC")
    List<Site> findRecentlyPublishedSites();

    List<Site> findByIdIn(List<Long> ids);
}