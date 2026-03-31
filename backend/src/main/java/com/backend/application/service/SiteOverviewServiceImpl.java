package com.backend.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.response.SiteOverviewAppDto;
import com.backend.application.dto.response.SiteOverviewAppDto.ActionsAppDto;
import com.backend.application.dto.response.SiteOverviewAppDto.ActivityAppDto;
import com.backend.application.dto.response.SiteOverviewAppDto.ActivityTrendDayAppDto;
import com.backend.application.dto.response.SiteOverviewAppDto.EntityStatsAppDto;
import com.backend.application.dto.response.SiteOverviewAppDto.MediaStatsAppDto;
import com.backend.application.dto.response.SiteOverviewAppDto.SiteStatsAppDto;
import com.backend.application.dto.response.SiteOverviewAppDto.SiteStatusAppDto;
import com.backend.application.dto.response.SiteOverviewAppDto.UserAppDto;
import com.backend.domain.enums.ActivityAction;
import com.backend.domain.entity.Site;
import com.backend.domain.entity.SiteActivity;
import com.backend.domain.enums.ComponentStatus;
import com.backend.domain.enums.ModuleCode;
import com.backend.domain.enums.PageStatus;
import com.backend.domain.enums.ProductStatus;
import com.backend.domain.repository.ComponentRepository;
import com.backend.domain.repository.MediaRepository;
import com.backend.domain.repository.PageRepository;
import com.backend.domain.repository.ProductRepository;
import com.backend.domain.repository.SiteActivityRepository;
import com.backend.domain.repository.SiteRepository;
import com.backend.domain.repository.UserRepository;
import com.backend.domain.port.FrontendConfigPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of SiteOverviewService.
 * Provides site dashboard overview data including status, stats, and activity.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SiteOverviewServiceImpl implements SiteOverviewService {

    private static final int DEFAULT_ACTIVITY_LIMIT = 10;

    private final SiteRepository siteRepository;
    private final SiteActivityRepository siteActivityRepository;
    private final PageRepository pageRepository;
    private final ComponentRepository componentRepository;
    private final MediaRepository mediaRepository;
    private final ProductRepository productRepository;
    private final MessageSource messageSource;
    private final UserRepository userRepository;
    private final FrontendConfigPort frontendConfig;
    private final TenantModuleAccessService tenantModuleAccessService;

    @Override
    public SiteOverviewAppDto getOverview() {
        log.debug("Getting site overview");

        // Fetch site once to avoid multiple queries and lazy loading issues
        Site site = getFirstSite();

        return new SiteOverviewAppDto(
                site != null ? site.getId() : null,
                getSiteStatus(site),
                getStats(),
                getRecentActivity(DEFAULT_ACTIVITY_LIMIT),
                getAvailableActions(site));
    }

    @Override
    public SiteStatsAppDto getStats() {
        log.debug("Getting site stats");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekAgo = now.minusWeeks(1);
        LocalDateTime dayAgo = now.minusDays(1);

        // Page stats
        long totalPages = pageRepository.count();
        long publishedPages = pageRepository.countByStatus(PageStatus.PUBLISHED);
        long draftPages = pageRepository.countByStatus(PageStatus.DRAFT);
        long weeklyPageChange = pageRepository.countByCreatedAtAfter(weekAgo);

        EntityStatsAppDto pageStats = new EntityStatsAppDto(totalPages, publishedPages, draftPages, weeklyPageChange);

        // Component stats
        long totalComponents = componentRepository.count();
        long publishedComponents = componentRepository.countByStatus(ComponentStatus.PUBLISHED);
        long draftComponents = componentRepository.countByStatus(ComponentStatus.DRAFT);
        long weeklyComponentChange = componentRepository.countByCreatedAtAfter(weekAgo);

        EntityStatsAppDto componentStats = new EntityStatsAppDto(totalComponents, publishedComponents, draftComponents,
                weeklyComponentChange);

        // Media stats
        long totalMedia = mediaRepository.count();
        long totalMediaSizeBytes = 0;
        try {
            totalMediaSizeBytes = mediaRepository.sumFileSize();
        } catch (Exception e) {
            log.warn("Failed to get media size sum", e);
        }
        double totalSizeMb = totalMediaSizeBytes / (1024.0 * 1024.0);

        long dailyMediaChange = mediaRepository.countByCreatedAtBetween(dayAgo, now);

        MediaStatsAppDto mediaStats = new MediaStatsAppDto(totalMedia, Math.round(totalSizeMb * 100.0) / 100.0,
                dailyMediaChange);

	EntityStatsAppDto productStats = null;
	if (tenantModuleAccessService.isEnabledForCurrentTenant(ModuleCode.PRODUCT_CATALOG)) {
	    long totalProducts = productRepository.count();
	    long activeProducts = productRepository.countByStatus(ProductStatus.PUBLISHED);
	    long draftProducts = productRepository.countByStatus(ProductStatus.DRAFT);
	    long weeklyProductChange = productRepository.countByCreatedAtAfter(weekAgo);

	    productStats = new EntityStatsAppDto(totalProducts, activeProducts, draftProducts,
		    weeklyProductChange);
	}

        return new SiteStatsAppDto(pageStats, componentStats, mediaStats, productStats);
    }

    @Override
    public List<ActivityAppDto> getRecentActivity(int limit) {
        log.debug("Getting recent activity with limit: {}", limit);

        try {
            return mapActivities(siteActivityRepository.findRecentActivities(limit));
        } catch (Exception e) {
            log.warn("Failed to get recent activities", e);
            return Collections.emptyList();
        }
    }

    @Override
    public Page<ActivityAppDto> getRecentActivityPage(Pageable pageable) {
        log.debug("Getting recent activity page: {}", pageable);

        try {
            List<SiteActivity> activities = siteActivityRepository.findRecentActivities(
                    pageable.getPageNumber(), pageable.getPageSize());
            long total = siteActivityRepository.count();
            List<ActivityAppDto> content = mapActivities(activities);
            return new PageImpl<>(content, pageable, total);
        } catch (Exception e) {
            log.warn("Failed to get recent activity page", e);
            return Page.empty(pageable);
        }
    }

    @Override
    public ActionsAppDto getAvailableActions() {
        log.debug("Getting available actions");
        return getAvailableActions(getFirstSite());
    }

    private ActionsAppDto getAvailableActions(Site site) {
        if (site == null) {
            return new ActionsAppDto(false, false, false, false, null);
        }

        boolean canPublish = site.canBePublished() && !Boolean.TRUE.equals(site.getPublished());
        boolean canPreview = true;
        boolean canEnableMaintenance = !Boolean.TRUE.equals(site.getMaintenanceMode());
        boolean canDisableMaintenance = Boolean.TRUE.equals(site.getMaintenanceMode());

        String previewUrl = buildPreviewUrl(site);

        return new ActionsAppDto(canPublish, canPreview, canEnableMaintenance, canDisableMaintenance, previewUrl);
    }

    private SiteStatusAppDto getSiteStatus(Site site) {
        if (site == null) {
            return new SiteStatusAppDto("draft", null, null, null);
        }

        String state = determineState(site);
        LocalDateTime lastUpdatedAt = site.getUpdatedAt();

        UserAppDto lastUpdatedBy = null;
        if (site.getUpdatedBy() != null) {
            com.backend.domain.entity.User user = userRepository.findById(site.getUpdatedBy()).orElse(null);
            if (user != null) {
                lastUpdatedBy = new UserAppDto(user.getId(), user.getEmail(), user.getDisplayName());
            }
        }

        return new SiteStatusAppDto(
                state,
                site.getPublishedAt(),
                lastUpdatedAt,
                lastUpdatedBy);
    }

    private Site getFirstSite() {
	// Use JOIN FETCH to eagerly load enabledLanguages and avoid
	// LazyInitializationException
        List<Site> sites = siteRepository.findAllWithEnabledLanguages();
        if (sites.isEmpty()) {
            return null;
        }
        return sites.get(0);
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
        return protocol + resolvePreviewHost(site) + "?preview=true";
    }

    private String resolvePreviewHost(Site site) {
        if (site.getCustomDomain() != null && !site.getCustomDomain().isBlank()) {
            return site.getCustomDomain().trim();
        }
        if (site.getDomain() != null && !site.getDomain().isBlank()) {
            return site.getDomain().trim();
        }
        return resolveFrontendHost("preview");
    }

    private String resolveFrontendHost(String subdomain) {
        String formatted = String.format(frontendConfig.getBaseUrl(), subdomain).trim();
        formatted = formatted.replaceFirst("^https?://", "");
        return formatted.replaceFirst("/.*$", "");
    }

    private ActivityAppDto toActivityDto(SiteActivity activity, com.backend.domain.entity.User user) {
        UserAppDto userDto = null;
        if (user != null) {
            userDto = new UserAppDto(
                    user.getId(),
                    user.getEmail(),
                    user.getDisplayName());
        }

        return new ActivityAppDto(
                activity.getId(),
                activity.getAction().name(),
                activity.getEntityType().name(),
                activity.getEntityId(),
                activity.getEntityName(),
                getReadableDescription(activity),
                userDto,
                activity.getCreatedAt());
    }

    private List<ActivityAppDto> mapActivities(List<SiteActivity> activities) {
        Set<Long> userIds = activities.stream()
                .map(SiteActivity::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, com.backend.domain.entity.User> userMap = Collections.emptyMap();
        if (!userIds.isEmpty()) {
            userMap = userRepository.findByIdIn(new ArrayList<>(userIds)).stream()
                    .collect(Collectors.toMap(com.backend.domain.entity.User::getId, Function.identity()));
        }

        final Map<Long, com.backend.domain.entity.User> finalUserMap = userMap;

        return activities.stream()
                .map(activity -> toActivityDto(activity, finalUserMap.get(activity.getUserId())))
                .collect(Collectors.toList());
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

    @Override
    public Page<ActivityTrendDayAppDto> getActivityTrend(Pageable pageable, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(Math.max(days - 1L, 0L));
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay().minusNanos(1);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

        List<SiteActivity> activities;
        try {
            activities = siteActivityRepository.findByCreatedAtBetween(startDateTime, endDateTime);
        } catch (Exception e) {
            log.warn("Failed to fetch activity trend data", e);
            activities = Collections.emptyList();
        }

        // Group by date
        Map<LocalDate, long[]> dayMap = new HashMap<>();
        for (SiteActivity a : activities) {
            LocalDate date = a.getCreatedAt().toLocalDate();
            long[] counts = dayMap.computeIfAbsent(date, k -> new long[3]); // [created, updated, published]
            if (a.getAction() == ActivityAction.CREATED || a.getAction() == ActivityAction.UPLOADED) {
                counts[0]++;
            } else if (a.getAction() == ActivityAction.PUBLISHED) {
                counts[2]++;
            } else {
                // UPDATED, DELETED, and any other actions are grouped into the "updated" bucket
                counts[1]++;
            }
        }

        // Build one entry per day (fill missing days with zeros)
        List<ActivityTrendDayAppDto> result = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = endDate.minusDays(i);
            long[] counts = dayMap.getOrDefault(date, new long[3]);
            long created = counts[0];
            long updated = counts[1];
            long published = counts[2];
            result.add(new ActivityTrendDayAppDto(
                    date.format(dateFormatter),
                    created + updated + published,
                    created,
                    updated,
                    published));
        }

        List<ActivityTrendDayAppDto> sortedResult = new ArrayList<>(result);
        Sort.Order dateOrder = pageable.getSort().getOrderFor("date");
        if (dateOrder != null && dateOrder.isDescending()) {
            sortedResult.sort(Comparator.comparing(ActivityTrendDayAppDto::date).reversed());
        } else {
            sortedResult.sort(Comparator.comparing(ActivityTrendDayAppDto::date));
        }

        int fromIndex = Math.min((int) pageable.getOffset(), sortedResult.size());
        int toIndex = Math.min(fromIndex + pageable.getPageSize(), sortedResult.size());

        return new PageImpl<>(
                sortedResult.subList(fromIndex, toIndex),
                pageable,
                sortedResult.size());
    }
}
