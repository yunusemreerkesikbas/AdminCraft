package com.backend.infrastructure.persistence.repository;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.backend.domain.entity.PlatformNewsletterSubscriber;
import com.backend.domain.entity.PlatformNewsletterSubscriberSubscription;
import com.backend.domain.enums.MailSubscriberStatus;
import com.backend.domain.repository.PlatformNewsletterSubscriberSubscriptionRepository;
import com.backend.infrastructure.persistence.platform.mapper.PlatformMailMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PlatformNewsletterSubscriberSubscriptionPersistenceAdapter
        implements PlatformNewsletterSubscriberSubscriptionRepository {

    private final com.backend.infrastructure.persistence.platform.repository.PlatformNewsletterSubscriberSubscriptionRepository jpaRepository;

    @Override
    public Optional<PlatformNewsletterSubscriberSubscription> findBySubscriberAndTemplateKeyIgnoreCase(
            PlatformNewsletterSubscriber subscriber, String templateKey) {
        return jpaRepository.findBySubscriberAndTemplateKeyIgnoreCase(toSubscriberReference(subscriber), templateKey)
                .map(PlatformMailMapper::toDomain);
    }

    @Override
    public List<PlatformNewsletterSubscriberSubscription> findByTemplateKeyIgnoreCaseOrderBySubscriberCreatedAtDesc(
            String templateKey) {
        return jpaRepository.findByTemplateKeyIgnoreCaseOrderBySubscriberCreatedAtDesc(templateKey).stream()
                .map(PlatformMailMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PlatformNewsletterSubscriberSubscription> findAllByOrderBySubscriberCreatedAtDesc() {
        return jpaRepository.findAllByOrderBySubscriberCreatedAtDesc().stream()
                .map(PlatformMailMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PlatformNewsletterSubscriberSubscription> findByTemplateKeyIgnoreCaseAndSubscriberStatus(
            String templateKey, MailSubscriberStatus status) {
        return jpaRepository.findByTemplateKeyIgnoreCaseAndSubscriberStatus(templateKey, status).stream()
                .map(PlatformMailMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PlatformNewsletterSubscriberSubscription> findByTemplateKeyIgnoreCaseAndPermissionTrueAndSubscriberStatus(
            String templateKey, MailSubscriberStatus status) {
        return jpaRepository.findByTemplateKeyIgnoreCaseAndPermissionTrueAndSubscriberStatus(templateKey, status)
                .stream()
                .map(PlatformMailMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PlatformNewsletterSubscriberSubscription> findBySubscriberIdInOrderByTemplateKeyAsc(
            Collection<Long> subscriberIds) {
        return jpaRepository.findBySubscriberIdInOrderByTemplateKeyAsc(subscriberIds).stream()
                .map(PlatformMailMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<PlatformNewsletterSubscriberSubscription> findBySubscriberOrderByTemplateKeyAsc(
            PlatformNewsletterSubscriber subscriber) {
        return jpaRepository.findBySubscriberOrderByTemplateKeyAsc(toSubscriberReference(subscriber)).stream()
                .map(PlatformMailMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countByTemplateKeyIgnoreCase(String templateKey) {
        return jpaRepository.countByTemplateKeyIgnoreCase(templateKey);
    }

    @Override
    public void deleteAll(Iterable<PlatformNewsletterSubscriberSubscription> subscriptions) {
        List<com.backend.infrastructure.persistence.platform.entity.PlatformNewsletterSubscriberSubscription> entities =
                toDeleteEntityList(subscriptions);
        jpaRepository.deleteAll(entities);
    }

    @Override
    public List<PlatformNewsletterSubscriberSubscription> saveAll(
            Iterable<PlatformNewsletterSubscriberSubscription> subscriptions) {
        return jpaRepository.saveAll(toEntityList(subscriptions)).stream()
                .map(PlatformMailMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public PlatformNewsletterSubscriberSubscription save(PlatformNewsletterSubscriberSubscription subscription) {
        return PlatformMailMapper.toDomain(jpaRepository.save(toEntityForSave(subscription)));
    }

    private List<com.backend.infrastructure.persistence.platform.entity.PlatformNewsletterSubscriberSubscription> toEntityList(
            Iterable<PlatformNewsletterSubscriberSubscription> subscriptions) {
        return java.util.stream.StreamSupport.stream(subscriptions.spliterator(), false)
                .map(this::toEntityForSave)
                .collect(Collectors.toList());
    }

    private List<com.backend.infrastructure.persistence.platform.entity.PlatformNewsletterSubscriberSubscription> toDeleteEntityList(
            Iterable<PlatformNewsletterSubscriberSubscription> subscriptions) {
        return java.util.stream.StreamSupport.stream(subscriptions.spliterator(), false)
                .map(subscription -> {
                    com.backend.infrastructure.persistence.platform.entity.PlatformNewsletterSubscriberSubscription entity =
                            new com.backend.infrastructure.persistence.platform.entity.PlatformNewsletterSubscriberSubscription();
                    entity.setId(subscription.getId());
                    return entity;
                })
                .collect(Collectors.toList());
    }

    private com.backend.infrastructure.persistence.platform.entity.PlatformNewsletterSubscriberSubscription toEntityForSave(
            PlatformNewsletterSubscriberSubscription source) {
        com.backend.infrastructure.persistence.platform.entity.PlatformNewsletterSubscriberSubscription target =
                source.getId() == null
                        ? new com.backend.infrastructure.persistence.platform.entity.PlatformNewsletterSubscriberSubscription()
                        : jpaRepository.findById(source.getId())
                                .orElseGet(com.backend.infrastructure.persistence.platform.entity.PlatformNewsletterSubscriberSubscription::new);
        target.setId(source.getId());
        target.setSubscriber(toSubscriberReference(source.getSubscriber()));
        target.setTemplateKey(source.getTemplateKey());
        target.setSource(source.getSource());
        target.setPreferredLanguage(source.getPreferredLanguage());
        target.setPermission(source.getPermission());
        return target;
    }

    private com.backend.infrastructure.persistence.platform.entity.PlatformNewsletterSubscriber toSubscriberReference(
            PlatformNewsletterSubscriber subscriber) {
        Long subscriberId = Objects.requireNonNull(subscriber, "subscriber is required").getId();
        if (subscriberId == null) {
            throw new IllegalArgumentException("subscriber id is required");
        }
        com.backend.infrastructure.persistence.platform.entity.PlatformNewsletterSubscriber ref =
                new com.backend.infrastructure.persistence.platform.entity.PlatformNewsletterSubscriber();
        ref.setId(subscriberId);
        return ref;
    }
}
