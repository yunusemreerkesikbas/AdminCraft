package com.backend.domain.repository;

import com.backend.domain.entity.SiteTechnicalSettings;

import java.util.Optional;

/**
 * Repository interface for SiteTechnicalSettings entity.
 * Provides methods for managing technical settings for sites.
 */
public interface SiteTechnicalSettingsRepository {

    SiteTechnicalSettings save(SiteTechnicalSettings settings);

    Optional<SiteTechnicalSettings> findById(Long siteId);

    Optional<SiteTechnicalSettings> findBySiteId(Long siteId);

    boolean existsBySiteId(Long siteId);

    void deleteById(Long siteId);

    void deleteBySiteId(Long siteId);

    /**
     * Find settings with indexing enabled.
     */
    java.util.List<SiteTechnicalSettings> findByIndexingEnabledTrue();

    /**
     * Find settings with sitemap enabled.
     */
    java.util.List<SiteTechnicalSettings> findBySitemapEnabledTrue();

    /**
     * Find settings with cookie consent enabled.
     */
    java.util.List<SiteTechnicalSettings> findByCookieConsentEnabledTrue();
}
