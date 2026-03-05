package com.backend.infrastructure.persistence.platform.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import com.backend.domain.enums.MailSubscriberStatus;
import com.backend.infrastructure.persistence.platform.entity.PlatformNewsletterSubscriber;
import com.backend.infrastructure.persistence.platform.entity.PlatformNewsletterSubscriberSubscription;

@Repository
public interface PlatformNewsletterSubscriberSubscriptionRepository
    extends JpaRepository<PlatformNewsletterSubscriberSubscription, Long> {

    Optional<PlatformNewsletterSubscriberSubscription> findBySubscriberAndTemplateKeyIgnoreCase(
        PlatformNewsletterSubscriber subscriber,
        String templateKey
    );

    @EntityGraph(attributePaths = "subscriber")
    List<PlatformNewsletterSubscriberSubscription> findByTemplateKeyIgnoreCaseOrderBySubscriberCreatedAtDesc(
        String templateKey
    );

    @EntityGraph(attributePaths = "subscriber")
    List<PlatformNewsletterSubscriberSubscription> findByTemplateKeyIgnoreCaseAndSubscriberStatus(
        String templateKey,
        MailSubscriberStatus status
    );

    @EntityGraph(attributePaths = "subscriber")
    List<PlatformNewsletterSubscriberSubscription> findByTemplateKeyIgnoreCaseAndPermissionTrueAndSubscriberStatus(
        String templateKey,
        MailSubscriberStatus status
    );

    @EntityGraph(attributePaths = "subscriber")
    List<PlatformNewsletterSubscriberSubscription> findBySubscriberIdInOrderByTemplateKeyAsc(Collection<Long> subscriberIds);

    @EntityGraph(attributePaths = "subscriber")
    List<PlatformNewsletterSubscriberSubscription> findBySubscriberOrderByTemplateKeyAsc(PlatformNewsletterSubscriber subscriber);

    long countByTemplateKeyIgnoreCase(String templateKey);
}
