package com.backend.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.backend.domain.entity.MailOutbox;
import com.backend.domain.repository.MailOutboxRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MailOutboxRepositoryImpl implements MailOutboxRepository {

    private final MailOutboxJpaRepository jpaRepository;

    @Override
    public MailOutbox save(MailOutbox outbox) {
        return jpaRepository.save(outbox);
    }

    @Override
    public Optional<MailOutbox> findById(Long id) {
        return jpaRepository.findById(id);
    }
}
