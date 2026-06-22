package com.backend.infrastructure.persistence.commerce;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.domain.commerce.CommerceNotificationEventType;
import com.backend.domain.commerce.CommerceNotificationOutbox;
import com.backend.domain.commerce.CommerceNotificationStatus;

import jakarta.persistence.LockModeType;

interface CommerceNotificationOutboxJpaRepository extends JpaRepository<CommerceNotificationOutbox, Long> {

	Optional<CommerceNotificationOutbox> findByUid(String uid);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select outbox from CommerceNotificationOutbox outbox where outbox.id = :id")
	Optional<CommerceNotificationOutbox> findByIdForUpdate(@Param("id") Long id);

	@Query("""
			select outbox from CommerceNotificationOutbox outbox
			where (:status is null or outbox.status = :status)
				and (:eventType is null or outbox.eventType = :eventType)
				and (:aggregateUid is null or outbox.aggregateUid = :aggregateUid)
				and (:search is null
					or lower(outbox.uid) like concat('%', :search, '%')
					or lower(outbox.aggregateUid) like concat('%', :search, '%')
					or lower(coalesce(outbox.recipientEmail, '')) like concat('%', :search, '%')
					or lower(coalesce(outbox.recipientPhone, '')) like concat('%', :search, '%')
					or lower(outbox.subject) like concat('%', :search, '%')
					or lower(coalesce(outbox.errorMessage, '')) like concat('%', :search, '%'))
			""")
	Page<CommerceNotificationOutbox> findAdminOutbox(
			@Param("search") String search,
			@Param("status") CommerceNotificationStatus status,
			@Param("eventType") CommerceNotificationEventType eventType,
			@Param("aggregateUid") String aggregateUid,
			Pageable pageable);

	@Query("""
			select outbox from CommerceNotificationOutbox outbox
			where outbox.status = com.backend.domain.commerce.CommerceNotificationStatus.FAILED
				and outbox.attemptCount <= :maxAttemptCount
				and outbox.nextRetryAt is not null
				and outbox.nextRetryAt <= :now
			""")
	Page<CommerceNotificationOutbox> findDueRetries(
			@Param("maxAttemptCount") int maxAttemptCount,
			@Param("now") LocalDateTime now,
			Pageable pageable);

	long countByStatus(CommerceNotificationStatus status);
}
