package com.backend.domain.commerce.repository;

import java.util.Optional;

import com.backend.domain.commerce.CommerceNotificationOutbox;

public interface CommerceNotificationOutboxRepository {

	CommerceNotificationOutbox save(CommerceNotificationOutbox outbox);

	Optional<CommerceNotificationOutbox> findById(Long id);
}
