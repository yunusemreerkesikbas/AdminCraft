package com.backend.application.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.entity.SiteActivity;
import com.backend.domain.enums.ActivityAction;
import com.backend.domain.enums.ActivityEntityType;
import com.backend.domain.repository.SiteActivityRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Publisher for site activity events.
 * Provides methods for recording user actions on various entities.
 * Activities are saved asynchronously to avoid blocking the main operation.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SiteActivityPublisher {

    private final SiteActivityRepository siteActivityRepository;

    /**
     * Publish an activity event for a page entity.
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishPageEvent(Long pageId, String pageName, ActivityAction action,
            Long userId, String userEmail, String userFullName) {
        publishEvent(ActivityEntityType.PAGE, pageId, pageName, action, userId);
    }

    /**
     * Publish an activity event for a component entity.
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishComponentEvent(Long componentId, String componentName, ActivityAction action,
            Long userId, String userEmail, String userFullName) {
        publishEvent(ActivityEntityType.COMPONENT, componentId, componentName, action, userId);
    }

    /**
     * Publish an activity event for a media entity.
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishMediaEvent(Long mediaId, String mediaName, ActivityAction action,
            Long userId, String userEmail, String userFullName) {
        publishEvent(ActivityEntityType.MEDIA, mediaId, mediaName, action, userId);
    }

    /**
     * Publish an activity event for a product entity.
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishProductEvent(Long productId, String productName, ActivityAction action,
            Long userId, String userEmail, String userFullName) {
        publishEvent(ActivityEntityType.PRODUCT, productId, productName, action, userId);
    }

    /**
     * Publish an activity event for a site entity.
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishSiteEvent(Long siteId, String siteName, ActivityAction action,
            Long userId, String userEmail, String userFullName) {
        publishEvent(ActivityEntityType.SITE, siteId, siteName, action, userId);
    }

    /**
     * Publish an activity event for site settings.
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishSiteSettingsEvent(Long siteId, String settingName, ActivityAction action,
            Long userId, String userEmail, String userFullName) {
        publishEvent(ActivityEntityType.SITE_SETTINGS, siteId, settingName, action, userId);
    }

    /**
     * Publish an activity event for a navigation entity.
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishNavigationEvent(Long navigationId, String navigationName, ActivityAction action,
            Long userId, String userEmail, String userFullName) {
        publishEvent(ActivityEntityType.NAVIGATION, navigationId, navigationName, action, userId);
    }

    /**
     * Generic method to publish any activity event.
     */
    private void publishEvent(ActivityEntityType entityType, Long entityId, String entityName,
            ActivityAction action, Long userId) {
        try {
            SiteActivity activity = SiteActivity.create(
                    action,
                    entityType,
                    entityId,
                    entityName,
                    userId);
            siteActivityRepository.save(activity);
            log.debug("Activity published: {} {} - {} by {}",
                    action, entityType, entityName, userId);
        } catch (Exception e) {
            String msg = String.format("Failed to publish activity: %s %s - %s", action, entityType, entityName);
            if (msg.length() > 500) {
                msg = msg.substring(0, 500);
            }
            log.error(msg, e);
            // Don't rethrow in async method unless handled by AsyncUncaughtExceptionHandler
            // But previous code threw it.
            // Requirement said "Rethrow exception".
            throw e;
        }
    }

    /**
     * Clean up old activity records (retention policy: 30 days).
     * Should be called by a scheduled job.
     */
    @Transactional
    public void cleanupOldActivities() {
        // No try/catch to ensure transaction rollback on failure
        var cutoffDate = java.time.LocalDateTime.now().minusDays(30);
        siteActivityRepository.deleteByCreatedAtBefore(cutoffDate);
        log.info("Cleaned up activities older than {}", cutoffDate);
    }
}
