package com.backend.domain.repository;

import com.backend.domain.entity.SiteActivity;
import com.backend.domain.enums.ActivityAction;
import com.backend.domain.enums.ActivityEntityType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for SiteActivity entity.
 * Provides methods for querying site activity history.
 */
public interface SiteActivityRepository {

    SiteActivity save(SiteActivity activity);

    List<SiteActivity> saveAll(Iterable<SiteActivity> activities);

    List<SiteActivity> findAll();

    /**
     * Find recent activities ordered by creation date descending.
     * @param limit maximum number of records to return
     * @return list of recent activities
     */
    List<SiteActivity> findRecentActivities(int limit);

    Page<SiteActivity> findRecentActivities(Pageable pageable);

    /**
     * Find activities by entity type.
     */
    List<SiteActivity> findByEntityType(ActivityEntityType entityType);

    /**
     * Find activities by action type.
     */
    List<SiteActivity> findByAction(ActivityAction action);

    /**
     * Find activities by user ID.
     */
    List<SiteActivity> findByUserId(Long userId);

    /**
     * Find activities by entity type and action.
     */
    List<SiteActivity> findByEntityTypeAndAction(ActivityEntityType entityType, ActivityAction action);

    /**
     * Find activities created after a specific date.
     */
    List<SiteActivity> findByCreatedAtAfter(LocalDateTime dateTime);

    /**
     * Find activities created between two dates.
     */
    List<SiteActivity> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Count activities by entity type within a date range.
     */
    long countByEntityTypeAndCreatedAtBetween(ActivityEntityType entityType, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Count activities by action within a date range.
     */
    long countByActionAndCreatedAtBetween(ActivityAction action, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Delete activities older than a specific date (for retention policy).
     */
    void deleteByCreatedAtBefore(LocalDateTime dateTime);

    /**
     * Count total activities.
     */
    long count();
}
