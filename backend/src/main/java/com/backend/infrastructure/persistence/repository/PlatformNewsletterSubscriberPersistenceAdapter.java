package com.backend.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.backend.domain.entity.PlatformNewsletterSubscriber;
import com.backend.domain.enums.MailSubscriberStatus;
import com.backend.domain.repository.PlatformNewsletterSubscriberRepository;
import com.backend.infrastructure.persistence.platform.mapper.PlatformMailMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PlatformNewsletterSubscriberPersistenceAdapter implements PlatformNewsletterSubscriberRepository {

    private final com.backend.infrastructure.persistence.platform.repository.PlatformNewsletterSubscriberRepository jpaRepository;

    @Override
    public Optional<PlatformNewsletterSubscriber> findByEmailIgnoreCase(String email) {
        return jpaRepository.findByEmailIgnoreCase(email).map(PlatformMailMapper::toDomain);
    }

    @Override
    public Optional<PlatformNewsletterSubscriber> findByConfirmToken(String confirmToken) {
        return jpaRepository.findByConfirmToken(confirmToken).map(PlatformMailMapper::toDomain);
    }

    @Override
    public Optional<PlatformNewsletterSubscriber> findByUnsubscribeToken(String unsubscribeToken) {
        return jpaRepository.findByUnsubscribeToken(unsubscribeToken).map(PlatformMailMapper::toDomain);
    }

    @Override
    public Optional<PlatformNewsletterSubscriber> findById(Long id) {
        return jpaRepository.findById(id).map(PlatformMailMapper::toDomain);
    }

    @Override
    public boolean existsByEmailIgnoreCase(String email) {
        return jpaRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    public Page<PlatformNewsletterSubscriber> searchSubscribers(
            String search,
            MailSubscriberStatus status,
            String templateType,
            Boolean permission,
            Pageable pageable) {
        Page<com.backend.infrastructure.persistence.platform.entity.PlatformNewsletterSubscriber> page =
                jpaRepository.searchSubscribers(search, status, templateType, permission, pageable);

        return new PageImpl<>(
                page.getContent().stream().map(PlatformMailMapper::toDomain).toList(),
                pageable,
                page.getTotalElements());
    }

    @Override
    public PlatformNewsletterSubscriber save(PlatformNewsletterSubscriber subscriber) {
        com.backend.infrastructure.persistence.platform.entity.PlatformNewsletterSubscriber entity =
                subscriber.getId() == null
                        ? new com.backend.infrastructure.persistence.platform.entity.PlatformNewsletterSubscriber()
                        : jpaRepository.findById(subscriber.getId())
                                .orElseThrow(() -> new IllegalArgumentException("mail.marketing.subscriber.not.found"));
        entity.setId(subscriber.getId());
        entity.setEmail(subscriber.getEmail());
        entity.setStatus(subscriber.getStatus());
        entity.setSource(subscriber.getSource());
        entity.setConfirmToken(subscriber.getConfirmToken());
        entity.setUnsubscribeToken(subscriber.getUnsubscribeToken());
        entity.setConfirmedAt(subscriber.getConfirmedAt());
        entity.setUnsubscribedAt(subscriber.getUnsubscribedAt());
        return PlatformMailMapper.toDomain(jpaRepository.save(entity));
    }
}
