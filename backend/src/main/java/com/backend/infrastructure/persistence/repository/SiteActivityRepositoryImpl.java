package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.SiteActivity;
import com.backend.domain.enums.ActivityAction;
import com.backend.domain.enums.ActivityEntityType;
import com.backend.domain.repository.SiteActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SiteActivityRepositoryImpl implements SiteActivityRepository {

    private final SiteActivityJpaRepository siteActivityJpaRepository;

    @Override
    public SiteActivity save(SiteActivity activity) {
        return siteActivityJpaRepository.save(activity);
    }

    @Override
    public List<SiteActivity> saveAll(Iterable<SiteActivity> activities) {
        return siteActivityJpaRepository.saveAll(activities);
    }

    @Override
    public List<SiteActivity> findAll() {
        return siteActivityJpaRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public List<SiteActivity> findRecentActivities(int limit) {
        return siteActivityJpaRepository.findRecentActivities(PageRequest.of(0, limit));
    }

    @Override
    public List<SiteActivity> findByEntityType(ActivityEntityType entityType) {
        return siteActivityJpaRepository.findByEntityType(entityType);
    }

    @Override
    public List<SiteActivity> findByAction(ActivityAction action) {
        return siteActivityJpaRepository.findByAction(action);
    }

    @Override
    public List<SiteActivity> findByUserId(Long userId) {
        return siteActivityJpaRepository.findByUserId(userId);
    }

    @Override
    public List<SiteActivity> findByEntityTypeAndAction(ActivityEntityType entityType, ActivityAction action) {
        return siteActivityJpaRepository.findByEntityTypeAndAction(entityType, action);
    }

    @Override
    public List<SiteActivity> findByCreatedAtAfter(LocalDateTime dateTime) {
        return siteActivityJpaRepository.findByCreatedAtAfter(dateTime);
    }

    @Override
    public List<SiteActivity> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return siteActivityJpaRepository.findByCreatedAtBetween(startDate, endDate);
    }

    @Override
    public long countByEntityTypeAndCreatedAtBetween(ActivityEntityType entityType, LocalDateTime startDate, LocalDateTime endDate) {
        return siteActivityJpaRepository.countByEntityTypeAndCreatedAtBetween(entityType, startDate, endDate);
    }

    @Override
    public long countByActionAndCreatedAtBetween(ActivityAction action, LocalDateTime startDate, LocalDateTime endDate) {
        return siteActivityJpaRepository.countByActionAndCreatedAtBetween(action, startDate, endDate);
    }

    @Override
    public void deleteByCreatedAtBefore(LocalDateTime dateTime) {
        siteActivityJpaRepository.deleteByCreatedAtBefore(dateTime);
    }

    @Override
    public long count() {
        return siteActivityJpaRepository.count();
    }
}
