package com.backend.domain.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.backend.domain.entity.NewsletterSubscriber;
import com.backend.domain.entity.NewsletterSubscriberSubscription;
import com.backend.domain.enums.MailSubscriberStatus;

public interface NewsletterSubscriberSubscriptionRepository {

    Optional<NewsletterSubscriberSubscription> findBySubscriberAndTemplateKeyIgnoreCase(
        NewsletterSubscriber subscriber, String templateKey);

    List<NewsletterSubscriberSubscription> findByTemplateKeyIgnoreCaseOrderBySubscriberCreatedAtDesc(String templateKey);

    List<NewsletterSubscriberSubscription> findAllByOrderBySubscriberCreatedAtDesc();

    List<NewsletterSubscriberSubscription> findByTemplateKeyIgnoreCaseAndSubscriberStatus(
        String templateKey, MailSubscriberStatus status);

    List<NewsletterSubscriberSubscription> findByTemplateKeyIgnoreCaseAndPermissionTrueAndSubscriberStatus(
        String templateKey, MailSubscriberStatus status);

    List<NewsletterSubscriberSubscription> findBySubscriberIdInOrderByTemplateKeyAsc(Collection<Long> subscriberIds);

    List<NewsletterSubscriberSubscription> findBySubscriberOrderByTemplateKeyAsc(NewsletterSubscriber subscriber);

    long countByTemplateKeyIgnoreCase(String templateKey);

    void deleteAll(Iterable<NewsletterSubscriberSubscription> subscriptions);

    List<NewsletterSubscriberSubscription> saveAll(Iterable<NewsletterSubscriberSubscription> subscriptions);

    NewsletterSubscriberSubscription save(NewsletterSubscriberSubscription subscription);
}
