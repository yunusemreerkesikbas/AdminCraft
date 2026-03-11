package com.backend.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.backend.domain.entity.NewsletterSubscriber;
import com.backend.domain.enums.MailSubscriberStatus;
import com.backend.domain.repository.NewsletterSubscriberRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NewsletterSubscriberRepositoryImpl implements NewsletterSubscriberRepository {

    private final NewsletterSubscriberJpaRepository jpaRepository;

    @Override
    public Optional<NewsletterSubscriber> findByEmailIgnoreCase(String email) {
        return jpaRepository.findByEmailIgnoreCase(email);
    }

    @Override
    public Optional<NewsletterSubscriber> findByConfirmToken(String confirmToken) {
        return jpaRepository.findByConfirmToken(confirmToken);
    }

    @Override
    public Optional<NewsletterSubscriber> findByUnsubscribeToken(String unsubscribeToken) {
        return jpaRepository.findByUnsubscribeToken(unsubscribeToken);
    }

    @Override
    public Optional<NewsletterSubscriber> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public boolean existsByEmailIgnoreCase(String email) {
        return jpaRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    public Page<NewsletterSubscriber> searchSubscribers(
            String search,
            MailSubscriberStatus status,
            String templateType,
            Boolean permission,
            Pageable pageable) {
        return jpaRepository.searchSubscribers(search, status, templateType, permission, pageable);
    }

    @Override
    public NewsletterSubscriber save(NewsletterSubscriber subscriber) {
        return jpaRepository.save(subscriber);
    }
}
