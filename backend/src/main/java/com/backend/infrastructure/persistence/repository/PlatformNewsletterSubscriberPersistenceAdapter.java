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
        return PlatformMailMapper.toDomain(jpaRepository.save(PlatformMailMapper.toEntity(subscriber)));
    }
}
