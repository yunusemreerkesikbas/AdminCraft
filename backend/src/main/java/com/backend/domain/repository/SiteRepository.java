package com.backend.domain.repository;

import com.backend.domain.entity.Site;
import com.backend.domain.enums.Language;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SiteRepository {

    Site save(Site site);
    List<Site> saveAll(Iterable<Site> sites);
    Optional<Site> findById(Long id);
    List<Site> findAll();
    void deleteById(Long id);
    void deleteAll();
    boolean existsById(Long id);
    long count();
    void deleteAllById(Iterable<Long> ids);

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

    List<Site> findByEnabledLanguagesContaining(Language language);

    List<Site> findByDefaultLanguage(Language language);

    List<Site> findByThemeName(String themeName);

    List<Site> findBySslEnabledTrue();
    List<Site> findBySslEnabledFalse();

    List<Site> findBySiteNameContainingIgnoreCase(String siteName);
    List<Site> findByDescriptionContainingIgnoreCase(String description);

    List<Site> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<Site> findByPublishedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Site> findWithAnalytics();

    List<Site> findWithTagManager();

    List<Site> findActiveSites();

    List<Site> findAllActiveSites();

    long countPublishedSites();

    List<Site> findRecentlyPublishedSites();

    List<Site> findByIdIn(List<Long> ids);
}