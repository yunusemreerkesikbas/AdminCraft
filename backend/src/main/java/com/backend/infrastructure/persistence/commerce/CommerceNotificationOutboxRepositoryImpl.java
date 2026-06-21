package com.backend.infrastructure.persistence.commerce;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.commerce.CommerceNotificationEventType;
import com.backend.domain.commerce.CommerceNotificationOutbox;
import com.backend.domain.commerce.CommerceNotificationStatus;
import com.backend.domain.commerce.repository.CommerceNotificationOutboxRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class CommerceNotificationOutboxRepositoryImpl implements CommerceNotificationOutboxRepository {

	private final CommerceNotificationOutboxJpaRepository jpaRepository;

	@Override
	@Transactional
	public CommerceNotificationOutbox save(CommerceNotificationOutbox outbox) {
		return jpaRepository.save(outbox);
	}

	@Override
	public Optional<CommerceNotificationOutbox> findById(Long id) {
		return jpaRepository.findById(id);
	}

	@Override
	public Optional<CommerceNotificationOutbox> findByIdForUpdate(Long id) {
		return jpaRepository.findByIdForUpdate(id);
	}

	@Override
	public Optional<CommerceNotificationOutbox> findByUid(String uid) {
		return jpaRepository.findByUid(uid);
	}

	@Override
	public Page<CommerceNotificationOutbox> findAdminOutbox(
			String search,
			CommerceNotificationStatus status,
			CommerceNotificationEventType eventType,
			String aggregateUid,
			Pageable pageable) {
		return jpaRepository.findAdminOutbox(search, status, eventType, aggregateUid, pageable);
	}

	@Override
	public Page<CommerceNotificationOutbox> findDueRetries(
			int maxAttemptCount,
			java.time.LocalDateTime now,
			Pageable pageable) {
		return jpaRepository.findDueRetries(maxAttemptCount, now, pageable);
	}

	@Override
	public long countByStatus(CommerceNotificationStatus status) {
		return jpaRepository.countByStatus(status);
	}
}
