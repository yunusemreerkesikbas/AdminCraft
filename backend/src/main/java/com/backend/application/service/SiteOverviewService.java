package com.backend.application.service;

import com.backend.presentation.dto.response.SiteOverviewResponse;

/**
 * Service interface for Site Dashboard Overview operations.
 * Provides site status, statistics, recent activity, and available actions.
 */
public interface SiteOverviewService {

    /**
     * Get the complete overview data for the Site Dashboard.
     * Includes status, stats, recent activity, and available actions.
     *
     * @return SiteOverviewResponse containing all dashboard data
     */
    SiteOverviewResponse getOverview();

    /**
     * Get only the site stats (pages, components, media, products counts).
     *
     * @return SiteStatsDto with entity counts
     */
    SiteOverviewResponse.SiteStatsDto getStats();

    /**
     * Get recent activity list.
     *
     * @param limit maximum number of activities to return
     * @return List of recent activities
     */
    java.util.List<SiteOverviewResponse.ActivityDto> getRecentActivity(int limit);

    /**
     * Get available actions for the site.
     *
     * @return ActionsDto with available action flags and URLs
     */
    SiteOverviewResponse.ActionsDto getAvailableActions();
}
