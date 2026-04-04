package com.backend.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for Site Dashboard Overview tab.
 * Contains site status, stats, recent activity, and available actions.
 */
public record SiteOverviewResponse(
    Long id,
    SiteStatusDto status,
    SiteStatsDto stats,
    List<ActivityDto> recentActivity,
    ActionsDto actions,
    SpotlightDto spotlight
) {

    public record SiteStatusDto(
        String state,           // "published", "draft", "maintenance"
        LocalDateTime publishedAt,
        LocalDateTime lastUpdatedAt,
        UserDto lastUpdatedBy
    ) {}

    public record UserDto(
        Long id,
        String email,
        String displayName
    ) {}

    public record SiteStatsDto(
        EntityStatsDto pages,
        EntityStatsDto components,
        MediaStatsDto media,
        EntityStatsDto products
    ) {}

    public record EntityStatsDto(
        long total,
        long published,
        long draft,
        int weeklyChange
    ) {}

    public record MediaStatsDto(
        long total,
        double totalSizeMb,
        int dailyChange
    ) {}

    public record ActivityDto(
        Long id,
        String action,
        String entityType,
        Long entityId,
        String entityName,
        String description,
        UserDto user,
        LocalDateTime createdAt
    ) {}

    public record ActionsDto(
        boolean canPublish,
        boolean canPreview,
        boolean canEnableMaintenance,
        boolean canDisableMaintenance,
        String previewUrl
    ) {}

    public record SpotlightDto(
        int operationalScore,
        SpotlightStatusDto status,
        List<SpotlightContextCardDto> contextCards,
        List<SpotlightRecommendationDto> recommendations
    ) {}

    public record SpotlightStatusDto(
        String tone,
        String code
    ) {}

    public record SpotlightContextCardDto(
        String id,
        String icon,
        int progress,
        String tone,
        String valueCode,
        String detailCode,
        LocalDateTime detailDate
    ) {}

    public record SpotlightRecommendationDto(
        String id,
        String icon,
        String tone,
        Long count
    ) {}

    public record ActivityTrendResponse(
        String period,
        List<ActivityTrendDayResponse> days
    ) {}

    public record ActivityTrendDayResponse(
        String date,
        long total,
        long created,
        long updated,
        long published
    ) {}

    /**
     * Builder for creating SiteOverviewResponse.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private SiteStatusDto status;
        private SiteStatsDto stats;
        private List<ActivityDto> recentActivity;
        private ActionsDto actions;
        private SpotlightDto spotlight;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder status(SiteStatusDto status) {
            this.status = status;
            return this;
        }

        public Builder stats(SiteStatsDto stats) {
            this.stats = stats;
            return this;
        }

        public Builder recentActivity(List<ActivityDto> recentActivity) {
            this.recentActivity = recentActivity;
            return this;
        }

        public Builder actions(ActionsDto actions) {
            this.actions = actions;
            return this;
        }

        public Builder spotlight(SpotlightDto spotlight) {
            this.spotlight = spotlight;
            return this;
        }

        public SiteOverviewResponse build() {
            return new SiteOverviewResponse(id, status, stats, recentActivity, actions, spotlight);
        }
    }
}
