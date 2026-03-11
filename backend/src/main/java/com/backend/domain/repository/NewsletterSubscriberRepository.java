package com.backend.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

import com.backend.domain.entity.NewsletterSubscriber;
import com.backend.domain.enums.MailSubscriberStatus;

public interface NewsletterSubscriberRepository {

    Optional<NewsletterSubscriber> findByEmailIgnoreCase(String email);

    Optional<NewsletterSubscriber> findByConfirmToken(String confirmToken);

    Optional<NewsletterSubscriber> findByUnsubscribeToken(String unsubscribeToken);

    Optional<NewsletterSubscriber> findById(Long id);

    boolean existsByEmailIgnoreCase(String email);

    Page<NewsletterSubscriber> searchSubscribers(
        String search,
        MailSubscriberStatus status,
        String templateType,
        Boolean permission,
        Pageable pageable
    );

    NewsletterSubscriber save(NewsletterSubscriber subscriber);
}
