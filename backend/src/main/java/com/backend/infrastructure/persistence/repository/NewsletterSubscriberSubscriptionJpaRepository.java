package com.backend.infrastructure.persistence.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import com.backend.domain.entity.NewsletterSubscriber;
import com.backend.domain.entity.NewsletterSubscriberSubscription;
import com.backend.domain.enums.MailSubscriberStatus;

@Repository
public interface NewsletterSubscriberSubscriptionJpaRepository
    extends JpaRepository<NewsletterSubscriberSubscription, Long> {

    Optional<NewsletterSubscriberSubscription> findBySubscriberAndTemplateKeyIgnoreCase(
        NewsletterSubscriber subscriber,
        String templateKey
    );

    @EntityGraph(attributePaths = "subscriber")
    List<NewsletterSubscriberSubscription> findByTemplateKeyIgnoreCaseOrderBySubscriberCreatedAtDesc(String templateKey);

    List<NewsletterSubscriberSubscription> findByTemplateKeyIgnoreCaseAndSubscriberStatus(
        String templateKey,
        MailSubscriberStatus status
    );

    List<NewsletterSubscriberSubscription> findByTemplateKeyIgnoreCaseAndPermissionTrueAndSubscriberStatus(
        String templateKey,
        MailSubscriberStatus status
    );

    @EntityGraph(attributePaths = "subscriber")
    List<NewsletterSubscriberSubscription> findBySubscriberIdInOrderByTemplateKeyAsc(Collection<Long> subscriberIds);

    @EntityGraph(attributePaths = "subscriber")
    List<NewsletterSubscriberSubscription> findBySubscriberOrderByTemplateKeyAsc(NewsletterSubscriber subscriber);

    long countByTemplateKeyIgnoreCase(String templateKey);
}
