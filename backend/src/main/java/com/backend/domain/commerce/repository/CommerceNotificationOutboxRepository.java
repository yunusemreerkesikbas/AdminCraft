package com.backend.domain.commerce.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.backend.domain.commerce.CommerceNotificationEventType;
import com.backend.domain.commerce.CommerceNotificationOutbox;
import com.backend.domain.commerce.CommerceNotificationStatus;

public interface CommerceNotificationOutboxRepository {

	CommerceNotificationOutbox save(CommerceNotificationOutbox outbox);

	Optional<CommerceNotificationOutbox> findById(Long id);

	Optional<CommerceNotificationOutbox> findByIdForUpdate(Long id);

	Optional<CommerceNotificationOutbox> findByUid(String uid);

	Page<CommerceNotificationOutbox> findAdminOutbox(
			String search,
			CommerceNotificationStatus status,
			CommerceNotificationEventType eventType,
			String aggregateUid,
			Pageable pageable);

	Page<CommerceNotificationOutbox> findDueRetries(
			int maxAttemptCount,
			java.time.LocalDateTime now,
			Pageable pageable);

	long countByStatus(CommerceNotificationStatus status);
}
