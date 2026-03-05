package com.backend.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

import com.backend.domain.enums.MailSubscriberStatus;
import com.backend.domain.entity.PlatformNewsletterSubscriber;

public interface PlatformNewsletterSubscriberRepository {

    Optional<PlatformNewsletterSubscriber> findByEmailIgnoreCase(String email);

    Optional<PlatformNewsletterSubscriber> findByConfirmToken(String confirmToken);

    Optional<PlatformNewsletterSubscriber> findByUnsubscribeToken(String unsubscribeToken);

    Optional<PlatformNewsletterSubscriber> findById(Long id);

    boolean existsByEmailIgnoreCase(String email);

    Page<PlatformNewsletterSubscriber> searchSubscribers(
        String search,
        MailSubscriberStatus status,
        String templateType,
        Boolean permission,
        Pageable pageable
    );

    PlatformNewsletterSubscriber save(PlatformNewsletterSubscriber subscriber);
}
