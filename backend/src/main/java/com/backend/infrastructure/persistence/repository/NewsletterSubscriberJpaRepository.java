package com.backend.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.backend.domain.entity.NewsletterSubscriber;
import com.backend.domain.enums.MailSubscriberStatus;

@Repository
public interface NewsletterSubscriberJpaRepository extends JpaRepository<NewsletterSubscriber, Long> {

    Optional<NewsletterSubscriber> findByEmailIgnoreCase(String email);

    Optional<NewsletterSubscriber> findByConfirmToken(String confirmToken);

    Optional<NewsletterSubscriber> findByUnsubscribeToken(String unsubscribeToken);

    boolean existsByEmailIgnoreCase(String email);

    List<NewsletterSubscriber> findByStatus(MailSubscriberStatus status);

    List<NewsletterSubscriber> findAllByOrderByCreatedAtDesc();

    List<NewsletterSubscriber> findDistinctByStatusAndSubscriptionsTemplateKeyIgnoreCase(
        MailSubscriberStatus status,
        String templateKey
    );

    List<NewsletterSubscriber> findDistinctBySubscriptionsTemplateKeyIgnoreCaseOrderByCreatedAtDesc(
        String templateKey
    );

    @Query("""
        SELECT s FROM NewsletterSubscriber s
        WHERE (:search IS NULL OR :search = ''
            OR LOWER(s.email) LIKE LOWER(CONCAT('%', :search, '%'))
            OR EXISTS (
                SELECT 1 FROM NewsletterSubscriberSubscription rel
                WHERE rel.subscriber = s
                  AND LOWER(COALESCE(rel.source, '')) LIKE LOWER(CONCAT('%', :search, '%'))
            )
        )
        AND (:status IS NULL OR s.status = :status)
        AND (:templateType IS NULL OR EXISTS (
            SELECT 1 FROM NewsletterSubscriberSubscription rel
            WHERE rel.subscriber = s
              AND UPPER(rel.templateKey) = UPPER(:templateType)
        ))
        AND (:permission IS NULL OR EXISTS (
            SELECT 1 FROM NewsletterSubscriberSubscription rel
            WHERE rel.subscriber = s
              AND rel.permission = :permission
              AND (:templateType IS NULL OR UPPER(rel.templateKey) = UPPER(:templateType))
        ))
        """)
    Page<NewsletterSubscriber> searchSubscribers(
        @Param("search") String search,
        @Param("status") MailSubscriberStatus status,
        @Param("templateType") String templateType,
        @Param("permission") Boolean permission,
        Pageable pageable
    );
}
