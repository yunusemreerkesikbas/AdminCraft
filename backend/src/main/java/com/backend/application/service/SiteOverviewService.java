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

    /**
     * Get a paginated page of recent activity.
     *
     * @param pageable pagination and sort parameters
     * @return Page of ActivityAppDto ordered by creation date
     */
    Page<SiteOverviewAppDto.ActivityAppDto> getRecentActivityPage(Pageable pageable);

    /**
     * Get available actions for the site.
     *
     * @return ActionsAppDto with available action flags and URLs
     */
    SiteOverviewAppDto.ActionsAppDto getAvailableActions();

    /**
     * Get aggregated daily activity trend data.
     *
     * @param pageable pagination and sort parameters (sort by "date")
     * @param days     number of past days to include (e.g. 7 or 30)
     * @return Page of ActivityTrendDayAppDto, one entry per day
     */
    Page<SiteOverviewAppDto.ActivityTrendDayAppDto> getActivityTrend(Pageable pageable, int days);
}
