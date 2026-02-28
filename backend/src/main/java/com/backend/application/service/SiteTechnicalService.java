package com.backend.application.service;

import com.backend.application.dto.request.SiteTechnicalPatchRequest;
import com.backend.application.dto.response.SiteTechnicalAppDto;

/**
 * Service interface for Site Technical Settings operations.
 * Handles robots.txt, verification codes, and cookie consent.
 */
public interface SiteTechnicalService {

    /**
     * Get technical settings for the current site.
     *
     * @return SiteTechnicalAppDto containing all technical settings
     */
    SiteTechnicalAppDto getTechnicalSettings();

    /**
     * Update technical settings for the current site.
     * Only non-null fields in the request will be updated (PATCH semantics).
     *
     * @param request the patch request with fields to update
     * @return updated SiteTechnicalAppDto
     */
    SiteTechnicalAppDto patchTechnicalSettings(SiteTechnicalPatchRequest request);

    /**
     * Get the effective robots.txt content for the site.
     * Takes into account the indexing enabled flag.
     *
     * @return the robots.txt content as string
     */
    String getRobotsTxt();

    /**
     * Check if sitemap generation is enabled for the site.
     *
     * @return true if sitemap is enabled
     */
    boolean isSitemapEnabled();

    /**
     * Check if search engine indexing is enabled for the site.
     *
     * @return true if indexing is enabled
     */
    boolean isIndexingEnabled();
}
