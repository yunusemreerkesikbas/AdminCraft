package com.backend.infrastructure.persistence.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.backend.domain.entity.NewsletterSubscriber;
import com.backend.domain.entity.NewsletterSubscriberSubscription;
import com.backend.domain.enums.MailSubscriberStatus;
import com.backend.domain.repository.NewsletterSubscriberSubscriptionRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NewsletterSubscriberSubscriptionRepositoryImpl implements NewsletterSubscriberSubscriptionRepository {

    private final NewsletterSubscriberSubscriptionJpaRepository jpaRepository;

    @Override
    public Optional<NewsletterSubscriberSubscription> findBySubscriberAndTemplateKeyIgnoreCase(
            NewsletterSubscriber subscriber, String templateKey) {
        return jpaRepository.findBySubscriberAndTemplateKeyIgnoreCase(subscriber, templateKey);
    }

    @Override
    public List<NewsletterSubscriberSubscription> findByTemplateKeyIgnoreCaseOrderBySubscriberCreatedAtDesc(
            String templateKey) {
        return jpaRepository.findByTemplateKeyIgnoreCaseOrderBySubscriberCreatedAtDesc(templateKey);
    }

    @Override
    public List<NewsletterSubscriberSubscription> findByTemplateKeyIgnoreCaseAndSubscriberStatus(
            String templateKey, MailSubscriberStatus status) {
        return jpaRepository.findByTemplateKeyIgnoreCaseAndSubscriberStatus(templateKey, status);
    }

    @Override
    public List<NewsletterSubscriberSubscription> findByTemplateKeyIgnoreCaseAndPermissionTrueAndSubscriberStatus(
            String templateKey, MailSubscriberStatus status) {
        return jpaRepository.findByTemplateKeyIgnoreCaseAndPermissionTrueAndSubscriberStatus(templateKey, status);
    }

    @Override
    public List<NewsletterSubscriberSubscription> findBySubscriberIdInOrderByTemplateKeyAsc(Collection<Long> subscriberIds) {
        return jpaRepository.findBySubscriberIdInOrderByTemplateKeyAsc(subscriberIds);
    }

    @Override
    public List<NewsletterSubscriberSubscription> findBySubscriberOrderByTemplateKeyAsc(NewsletterSubscriber subscriber) {
        return jpaRepository.findBySubscriberOrderByTemplateKeyAsc(subscriber);
    }

    @Override
    public long countByTemplateKeyIgnoreCase(String templateKey) {
        return jpaRepository.countByTemplateKeyIgnoreCase(templateKey);
    }

    @Override
    public void deleteAll(Iterable<NewsletterSubscriberSubscription> subscriptions) {
        jpaRepository.deleteAll(subscriptions);
    }

    @Override
    public List<NewsletterSubscriberSubscription> saveAll(Iterable<NewsletterSubscriberSubscription> subscriptions) {
        return jpaRepository.saveAll(subscriptions);
    }

    @Override
    public NewsletterSubscriberSubscription save(NewsletterSubscriberSubscription subscription) {
        return jpaRepository.save(subscription);
    }
}
