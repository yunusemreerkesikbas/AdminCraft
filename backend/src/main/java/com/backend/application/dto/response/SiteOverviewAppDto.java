package com.backend.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Application layer DTO for Site Overview data.
 * Replaces the dependency on presentation layer SiteOverviewResponse.
 */
public record SiteOverviewAppDto(
    SiteStatusAppDto status,
    SiteStatsAppDto stats,
    List<ActivityAppDto> recentActivity,
    ActionsAppDto actions) {
  public record SiteStatusAppDto(
      String state,
      LocalDateTime publishedAt,
      LocalDateTime lastUpdatedAt,
      UserAppDto lastUpdatedBy) {
  }

  public record SiteStatsAppDto(
      EntityStatsAppDto pages,
      EntityStatsAppDto components,
      MediaStatsAppDto media,
      EntityStatsAppDto products) {
  }

  public record EntityStatsAppDto(
      long total,
      long published,
      long draft,
      long weeklyChange) {
  }

  public record MediaStatsAppDto(
      long totalCount,
      double totalSizeMb,
      long dailyChange) {
  }

  public record ActivityAppDto(
      Long id,
      String action,
      String entityType,
      Long entityId,
      String entityName,
      String description,
      UserAppDto user,
      LocalDateTime createdAt) {
  }

  public record UserAppDto(
      Long id,
      String email,
      String displayName) {
  }

  public record ActionsAppDto(
      boolean canPublish,
      boolean canPreview,
      boolean canEnableMaintenance,
      boolean canDisableMaintenance,
      String previewUrl) {
  }
}
