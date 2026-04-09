package com.backend.domain.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.backend.domain.entity.PlatformNewsletterSubscriber;
import com.backend.domain.entity.PlatformNewsletterSubscriberSubscription;
import com.backend.domain.enums.MailSubscriberStatus;

public interface PlatformNewsletterSubscriberSubscriptionRepository {

    Optional<PlatformNewsletterSubscriberSubscription> findBySubscriberAndTemplateKeyIgnoreCase(
        PlatformNewsletterSubscriber subscriber, String templateKey);

    List<PlatformNewsletterSubscriberSubscription> findByTemplateKeyIgnoreCaseOrderBySubscriberCreatedAtDesc(
        String templateKey);

    List<PlatformNewsletterSubscriberSubscription> findAllByOrderBySubscriberCreatedAtDesc();

    List<PlatformNewsletterSubscriberSubscription> findByTemplateKeyIgnoreCaseAndSubscriberStatus(
        String templateKey, MailSubscriberStatus status);

    List<PlatformNewsletterSubscriberSubscription> findByTemplateKeyIgnoreCaseAndPermissionTrueAndSubscriberStatus(
        String templateKey, MailSubscriberStatus status);

    List<PlatformNewsletterSubscriberSubscription> findBySubscriberIdInOrderByTemplateKeyAsc(Collection<Long> subscriberIds);

    List<PlatformNewsletterSubscriberSubscription> findBySubscriberOrderByTemplateKeyAsc(PlatformNewsletterSubscriber subscriber);

    long countByTemplateKeyIgnoreCase(String templateKey);

    void deleteAll(Iterable<PlatformNewsletterSubscriberSubscription> subscriptions);

    List<PlatformNewsletterSubscriberSubscription> saveAll(Iterable<PlatformNewsletterSubscriberSubscription> subscriptions);

    PlatformNewsletterSubscriberSubscription save(PlatformNewsletterSubscriberSubscription subscription);
}
