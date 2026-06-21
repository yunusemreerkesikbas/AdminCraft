package com.backend.infrastructure.persistence.commerce;

import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.commerce.CommerceNotificationOutbox;
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
}
