package com.backend.application.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.backend.application.dto.response.SiteOverviewAppDto;

/**
 * Service interface for Site Dashboard Overview operations.
 * Provides site status, statistics, recent activity, and available actions.
 */
public interface SiteOverviewService {

    /**
     * Get the complete overview data for the Site Dashboard.
     * Includes status, stats, recent activity, and available actions.
     *
     * @return SiteOverviewAppDto containing all dashboard data
     */
    SiteOverviewAppDto getOverview();

    /**
     * Get only the site stats (pages, components, media, products counts).
     *
     * @return SiteStatsAppDto with entity counts
     */
    SiteOverviewAppDto.SiteStatsAppDto getStats();

    /**
     * Get recent activity list.
     *
     * @param limit maximum number of activities to return
     * @return List of recent activities
     */
    java.util.List<SiteOverviewAppDto.ActivityAppDto> getRecentActivity(int limit);

    Page<SiteOverviewAppDto.ActivityAppDto> getRecentActivityPage(Pageable pageable);

    /**
     * Get available actions for the site.
     *
     * @return ActionsAppDto with available action flags and URLs
     */
    SiteOverviewAppDto.ActionsAppDto getAvailableActions();

    Page<SiteOverviewAppDto.ActivityTrendDayAppDto> getActivityTrend(Pageable pageable, int days);
}
