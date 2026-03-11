package com.backend.infrastructure.persistence.platform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.backend.domain.enums.MailSubscriberStatus;
import com.backend.infrastructure.persistence.platform.entity.PlatformNewsletterSubscriber;

@Repository
public interface PlatformNewsletterSubscriberRepository extends JpaRepository<PlatformNewsletterSubscriber, Long> {

    Optional<PlatformNewsletterSubscriber> findByEmailIgnoreCase(String email);

    Optional<PlatformNewsletterSubscriber> findByConfirmToken(String confirmToken);

    Optional<PlatformNewsletterSubscriber> findByUnsubscribeToken(String unsubscribeToken);

    List<PlatformNewsletterSubscriber> findByStatus(MailSubscriberStatus status);

    List<PlatformNewsletterSubscriber> findAllByOrderByCreatedAtDesc();

    List<PlatformNewsletterSubscriber> findDistinctByStatusAndSubscriptionsTemplateKeyIgnoreCase(
        MailSubscriberStatus status,
        String templateKey
    );

    List<PlatformNewsletterSubscriber> findDistinctBySubscriptionsTemplateKeyIgnoreCaseOrderByCreatedAtDesc(
        String templateKey
    );

    boolean existsByEmailIgnoreCase(String email);

    @Query("""
        SELECT s FROM PlatformNewsletterSubscriber s
        WHERE (:search IS NULL OR :search = ''
            OR LOWER(s.email) LIKE LOWER(CONCAT('%', :search, '%'))
            OR EXISTS (
                SELECT 1 FROM PlatformNewsletterSubscriberSubscription rel
                WHERE rel.subscriber = s
                  AND LOWER(COALESCE(rel.source, '')) LIKE LOWER(CONCAT('%', :search, '%'))
            )
        )
        AND (:status IS NULL OR s.status = :status)
        AND (:templateType IS NULL OR EXISTS (
            SELECT 1 FROM PlatformNewsletterSubscriberSubscription rel
            WHERE rel.subscriber = s
              AND UPPER(rel.templateKey) = UPPER(:templateType)
        ))
        AND (:permission IS NULL OR EXISTS (
            SELECT 1 FROM PlatformNewsletterSubscriberSubscription rel
            WHERE rel.subscriber = s
              AND rel.permission = :permission
              AND (:templateType IS NULL OR UPPER(rel.templateKey) = UPPER(:templateType))
        ))
        """)
    Page<PlatformNewsletterSubscriber> searchSubscribers(
        @Param("search") String search,
        @Param("status") MailSubscriberStatus status,
        @Param("templateType") String templateType,
        @Param("permission") Boolean permission,
        Pageable pageable
    );
}
