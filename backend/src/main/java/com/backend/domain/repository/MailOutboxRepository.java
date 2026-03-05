package com.backend.domain.repository;

import java.util.Optional;

import com.backend.domain.entity.MailOutbox;

public interface MailOutboxRepository {

    MailOutbox save(MailOutbox outbox);

    Optional<MailOutbox> findById(Long id);
}
