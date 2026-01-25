package com.backend.application.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Implementation of SiteOverviewService.
 * Provides site dashboard overview data including status, stats, and activity.
 */
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.entity.Site;
import com.backend.domain.entity.SiteActivity;
import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.PageStatus;
import com.backend.domain.enums.ProductStatus;
import com.backend.domain.repository.ComponentRepository;
import com.backend.domain.repository.MediaRepository;
import com.backend.domain.repository.PageRepository;
import com.backend.domain.repository.ProductRepository;
import com.backend.domain.repository.SiteActivityRepository;
import com.backend.domain.repository.SiteRepository;
import com.backend.presentation.dto.response.SiteOverviewResponse;
import com.backend.presentation.dto.response.SiteOverviewResponse.ActionsDto;
import com.backend.presentation.dto.response.SiteOverviewResponse.ActivityDto;
import com.backend.presentation.dto.response.SiteOverviewResponse.EntityStatsDto;
import com.backend.presentation.dto.response.SiteOverviewResponse.MediaStatsDto;
import com.backend.presentation.dto.response.SiteOverviewResponse.SiteStatsDto;
import com.backend.presentation.dto.response.SiteOverviewResponse.SiteStatusDto;
import com.backend.presentation.dto.response.SiteOverviewResponse.UserDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SiteOverviewServiceImpl implements SiteOverviewService {

    private static final int DEFAULT_ACTIVITY_LIMIT = 10;
    private static final String PLATFORM_DOMAIN = "admincraft.com";

    private final SiteRepository siteRepository;
    private final SiteActivityRepository siteActivityRepository;
    private final PageRepository pageRepository;
    private final ComponentRepository componentRepository;
    private final MediaRepository mediaRepository;
    private final ProductRepository productRepository;
    private final MessageSource messageSource;

    @Override
    public SiteOverviewResponse getOverview() {
        log.debug("Getting site overview");

        return SiteOverviewResponse.builder()
                .status(getSiteStatus())
                .stats(getStats())
                .recentActivity(getRecentActivity(DEFAULT_ACTIVITY_LIMIT))
                .actions(getAvailableActions())
                .build();
    }

    @Override
    public SiteStatsDto getStats() {
        log.debug("Getting site stats");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekAgo = now.minusWeeks(1);
        LocalDateTime dayAgo = now.minusDays(1);

        // Page stats
        // Optimized: Use count queries instead of findAll
        long totalPages = pageRepository.count();
        long publishedPages = pageRepository.countByStatus(PageStatus.PUBLISHED);
        long draftPages = pageRepository.countByStatus(PageStatus.DRAFT);
        int weeklyPageChange = pageRepository.countByCreatedAtAfter(weekAgo);

        EntityStatsDto pageStats = new EntityStatsDto(totalPages, publishedPages, draftPages, weeklyPageChange);

        // Component stats
        long totalComponents = componentRepository.count();
        long publishedComponents = componentRepository.countByStatus(ComponentStatus.PUBLISHED);
        long draftComponents = componentRepository.countByStatus(ComponentStatus.DRAFT);
        int weeklyComponentChange = componentRepository.countByCreatedAtAfter(weekAgo);

        EntityStatsDto componentStats = new EntityStatsDto(totalComponents, publishedComponents, draftComponents,
                weeklyComponentChange);

        // Media stats
        long totalMedia = mediaRepository.count();
        long totalMediaSizeBytes = 0;
        try {
            totalMediaSizeBytes = mediaRepository.sumFileSize();
        } catch (Exception e) {
            log.warn("Failed to get media size sum: {}", e.getMessage());
        }
        double totalSizeMb = totalMediaSizeBytes / (1024.0 * 1024.0);

        // Optimized: Count only instead of fetching list
        int dailyMediaChange = mediaRepository.countByCreatedAtBetween(dayAgo, now);

        MediaStatsDto mediaStats = new MediaStatsDto(totalMedia, Math.round(totalSizeMb * 100.0) / 100.0,
                dailyMediaChange);

        // Product stats
        long totalProducts = productRepository.count();
        long activeProducts = productRepository.countByStatus(ProductStatus.PUBLISHED);
        long draftProducts = productRepository.countByStatus(ProductStatus.DRAFT);
        int weeklyProductChange = productRepository.countByCreatedAtAfter(weekAgo);

        EntityStatsDto productStats = new EntityStatsDto(totalProducts, activeProducts, draftProducts,
                weeklyProductChange);

        return new SiteStatsDto(pageStats, componentStats, mediaStats, productStats);
    }

    @Override
    public List<ActivityDto> getRecentActivity(int limit) {
        log.debug("Getting recent activity with limit: {}", limit);

        try {
            List<SiteActivity> activities = siteActivityRepository.findRecentActivities(limit);
            return activities.stream()
                    .map(this::toActivityDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Failed to get recent activities: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public ActionsDto getAvailableActions() {
        log.debug("Getting available actions");

        Site site = getFirstSite();
        if (site == null) {
            return new ActionsDto(false, false, false, false, null);
        }

        boolean canPublish = site.canBePublished() && !Boolean.TRUE.equals(site.getPublished());
        boolean canPreview = true;
        boolean canEnableMaintenance = !Boolean.TRUE.equals(site.getMaintenanceMode());
        boolean canDisableMaintenance = Boolean.TRUE.equals(site.getMaintenanceMode());

        String previewUrl = buildPreviewUrl(site);

        return new ActionsDto(canPublish, canPreview, canEnableMaintenance, canDisableMaintenance, previewUrl);
    }

    private SiteStatusDto getSiteStatus() {
        Site site = getFirstSite();
        if (site == null) {
            return new SiteStatusDto("draft", null, null, null);
        }

        String state = determineState(site);
        LocalDateTime lastUpdatedAt = site.getUpdatedAt();

        return new SiteStatusDto(
                state,
                site.getPublishedAt(),
                lastUpdatedAt,
                null // TODO: Get last updated by user from activity log
        );
    }

    private Site getFirstSite() {
        List<Site> sites = siteRepository.findAll();
        return sites.isEmpty() ? null : sites.get(0);
    }

    private String determineState(Site site) {
        if (Boolean.TRUE.equals(site.getMaintenanceMode())) {
            return "maintenance";
        }
        if (Boolean.TRUE.equals(site.getPublished())) {
            return "published";
        }
        return "draft";
    }

    private String buildPreviewUrl(Site site) {
        String protocol = Boolean.TRUE.equals(site.getSslEnabled()) ? "https://" : "http://";
        String domain = site.getDomain() != null ? site.getDomain() : "preview";
        return protocol + domain + "." + PLATFORM_DOMAIN + "?preview=true";
    }

    private ActivityDto toActivityDto(SiteActivity activity) {
        UserDto user = null;
        if (activity.getUserId() != null) {
            user = new UserDto(
                    activity.getUserId(),
                    activity.getUserEmail(),
                    activity.getUserFullName());
        }

        return new ActivityDto(
                activity.getId(),
                activity.getAction().name(),
                activity.getEntityType().name(),
                activity.getEntityId(),
                activity.getEntityName(),
                getReadableDescription(activity),
                user,
                activity.getCreatedAt());
    }

    private String getReadableDescription(SiteActivity activity) {
        try {
            Locale locale = LocaleContextHolder.getLocale();
            String actionKey = "activity.action." + activity.getAction().name();
            String entityKey = "activity.entity." + activity.getEntityType().name();

            String actionText = messageSource.getMessage(actionKey, null, activity.getAction().name(), locale);
            String entityText = messageSource.getMessage(entityKey, null, activity.getEntityType().name(), locale);

            return messageSource.getMessage("activity.description.format",
                    new Object[] { entityText, activity.getEntityName(), actionText },
                    entityText + " \"" + activity.getEntityName() + "\" " + actionText,
                    locale);
        } catch (Exception e) {
            log.warn("Failed to resolve activity description: {}", e.getMessage());
            return activity.getEntityType() + " \"" + activity.getEntityName() + "\" " + activity.getAction();
        }
    }
}
