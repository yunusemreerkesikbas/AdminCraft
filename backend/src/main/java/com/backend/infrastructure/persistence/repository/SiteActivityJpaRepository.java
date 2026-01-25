package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.SiteActivity;
import com.backend.domain.enums.ActivityAction;
import com.backend.domain.enums.ActivityEntityType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SiteActivityJpaRepository extends JpaRepository<SiteActivity, Long> {

    @Query("SELECT sa FROM SiteActivity sa ORDER BY sa.createdAt DESC")
    List<SiteActivity> findRecentActivities(Pageable pageable);

    List<SiteActivity> findByEntityType(ActivityEntityType entityType);

    List<SiteActivity> findByAction(ActivityAction action);

    List<SiteActivity> findByUserId(Long userId);

    List<SiteActivity> findByEntityTypeAndAction(ActivityEntityType entityType, ActivityAction action);

    List<SiteActivity> findByCreatedAtAfter(LocalDateTime dateTime);

    List<SiteActivity> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COUNT(sa) FROM SiteActivity sa WHERE sa.entityType = :entityType AND sa.createdAt BETWEEN :startDate AND :endDate")
    long countByEntityTypeAndCreatedAtBetween(
            @Param("entityType") ActivityEntityType entityType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(sa) FROM SiteActivity sa WHERE sa.action = :action AND sa.createdAt BETWEEN :startDate AND :endDate")
    long countByActionAndCreatedAtBetween(
            @Param("action") ActivityAction action,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Modifying
    @Query("DELETE FROM SiteActivity sa WHERE sa.createdAt < :dateTime")
    void deleteByCreatedAtBefore(@Param("dateTime") LocalDateTime dateTime);

    List<SiteActivity> findAllByOrderByCreatedAtDesc();
}
