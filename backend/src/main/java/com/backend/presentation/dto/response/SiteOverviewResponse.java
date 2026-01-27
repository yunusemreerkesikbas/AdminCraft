package com.backend.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for Site Dashboard Overview tab.
 * Contains site status, stats, recent activity, and available actions.
 */
public record SiteOverviewResponse(
    SiteStatusDto status,
    SiteStatsDto stats,
    List<ActivityDto> recentActivity,
    ActionsDto actions
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
        String fullName
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

    /**
     * Builder for creating SiteOverviewResponse.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private SiteStatusDto status;
        private SiteStatsDto stats;
        private List<ActivityDto> recentActivity;
        private ActionsDto actions;

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

        public SiteOverviewResponse build() {
            return new SiteOverviewResponse(status, stats, recentActivity, actions);
        }
    }
}
