package com.backend.infrastructure.persistence.platform.mapper;

import com.backend.domain.entity.PlatformEmailTemplate;
import com.backend.domain.entity.PlatformMailCampaign;
import com.backend.domain.entity.PlatformNewsletterSubscriber;
import com.backend.domain.entity.PlatformNewsletterSubscriberSubscription;

public final class PlatformMailMapper {

    private PlatformMailMapper() {}

    public static PlatformEmailTemplate toDomain(
            com.backend.infrastructure.persistence.platform.entity.PlatformEmailTemplate source) {
        if (source == null) return null;
        PlatformEmailTemplate target = new PlatformEmailTemplate();
        target.setId(source.getId());
        target.setTemplateKey(source.getTemplateKey());
        target.setLanguage(source.getLanguage());
        target.setSubject(source.getSubject());
        target.setContent(source.getContent());
        target.setIsActive(source.getIsActive());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    public static com.backend.infrastructure.persistence.platform.entity.PlatformEmailTemplate toEntity(
            PlatformEmailTemplate source) {
        if (source == null) return null;
        com.backend.infrastructure.persistence.platform.entity.PlatformEmailTemplate target =
                new com.backend.infrastructure.persistence.platform.entity.PlatformEmailTemplate();
        target.setId(source.getId());
        target.setTemplateKey(source.getTemplateKey());
        target.setLanguage(source.getLanguage());
        target.setSubject(source.getSubject());
        target.setContent(source.getContent());
        target.setIsActive(source.getIsActive());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    public static PlatformMailCampaign toDomain(
            com.backend.infrastructure.persistence.platform.entity.PlatformMailCampaign source) {
        if (source == null) return null;
        PlatformMailCampaign target = new PlatformMailCampaign();
        target.setId(source.getId());
        target.setTemplate(toDomain(source.getTemplate()));
        target.setSubject(source.getSubject());
        target.setContent(source.getContent());
        target.setStatus(source.getStatus());
        target.setTotalCount(source.getTotalCount());
        target.setSentCount(source.getSentCount());
        target.setFailedCount(source.getFailedCount());
        target.setCreatedByEmail(source.getCreatedByEmail());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    public static com.backend.infrastructure.persistence.platform.entity.PlatformMailCampaign toEntity(
            PlatformMailCampaign source) {
        if (source == null) return null;
        com.backend.infrastructure.persistence.platform.entity.PlatformMailCampaign target =
                new com.backend.infrastructure.persistence.platform.entity.PlatformMailCampaign();
        target.setId(source.getId());
        target.setTemplate(toEntity(source.getTemplate()));
        target.setSubject(source.getSubject());
        target.setContent(source.getContent());
        target.setStatus(source.getStatus());
        target.setTotalCount(source.getTotalCount());
        target.setSentCount(source.getSentCount());
        target.setFailedCount(source.getFailedCount());
        target.setCreatedByEmail(source.getCreatedByEmail());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    public static PlatformNewsletterSubscriber toDomain(
            com.backend.infrastructure.persistence.platform.entity.PlatformNewsletterSubscriber source) {
        if (source == null) return null;
        PlatformNewsletterSubscriber target = new PlatformNewsletterSubscriber();
        target.setId(source.getId());
        target.setEmail(source.getEmail());
        target.setStatus(source.getStatus());
        target.setSource(source.getSource());
        target.setConfirmToken(source.getConfirmToken());
        target.setUnsubscribeToken(source.getUnsubscribeToken());
        target.setConfirmedAt(source.getConfirmedAt());
        target.setUnsubscribedAt(source.getUnsubscribedAt());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    public static com.backend.infrastructure.persistence.platform.entity.PlatformNewsletterSubscriber toEntity(
            PlatformNewsletterSubscriber source) {
        if (source == null) return null;
        com.backend.infrastructure.persistence.platform.entity.PlatformNewsletterSubscriber target =
                new com.backend.infrastructure.persistence.platform.entity.PlatformNewsletterSubscriber();
        target.setId(source.getId());
        target.setEmail(source.getEmail());
        target.setStatus(source.getStatus());
        target.setSource(source.getSource());
        target.setConfirmToken(source.getConfirmToken());
        target.setUnsubscribeToken(source.getUnsubscribeToken());
        target.setConfirmedAt(source.getConfirmedAt());
        target.setUnsubscribedAt(source.getUnsubscribedAt());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    public static PlatformNewsletterSubscriberSubscription toDomain(
            com.backend.infrastructure.persistence.platform.entity.PlatformNewsletterSubscriberSubscription source) {
        if (source == null) return null;
        PlatformNewsletterSubscriberSubscription target = new PlatformNewsletterSubscriberSubscription();
        target.setId(source.getId());
        target.setSubscriber(toSubscriberShallow(source.getSubscriber()));
        target.setTemplateKey(source.getTemplateKey());
        target.setSource(source.getSource());
        target.setPreferredLanguage(source.getPreferredLanguage());
        target.setPermission(source.getPermission());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    public static com.backend.infrastructure.persistence.platform.entity.PlatformNewsletterSubscriberSubscription toEntity(
            PlatformNewsletterSubscriberSubscription source) {
        if (source == null) return null;
        com.backend.infrastructure.persistence.platform.entity.PlatformNewsletterSubscriberSubscription target =
                new com.backend.infrastructure.persistence.platform.entity.PlatformNewsletterSubscriberSubscription();
        target.setId(source.getId());
        target.setSubscriber(toEntity(source.getSubscriber()));
        target.setTemplateKey(source.getTemplateKey());
        target.setSource(source.getSource());
        target.setPreferredLanguage(source.getPreferredLanguage());
        target.setPermission(source.getPermission());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    private static PlatformNewsletterSubscriber toSubscriberShallow(
            com.backend.infrastructure.persistence.platform.entity.PlatformNewsletterSubscriber source) {
        if (source == null) return null;
        PlatformNewsletterSubscriber target = new PlatformNewsletterSubscriber();
        target.setId(source.getId());
        target.setEmail(source.getEmail());
        target.setStatus(source.getStatus());
        target.setSource(source.getSource());
        target.setConfirmToken(source.getConfirmToken());
        target.setUnsubscribeToken(source.getUnsubscribeToken());
        target.setConfirmedAt(source.getConfirmedAt());
        target.setUnsubscribedAt(source.getUnsubscribedAt());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }
}
