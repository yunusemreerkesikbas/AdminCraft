package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.backend.domain.entity.MailOutbox;
import com.backend.domain.enums.MailOutboxStatus;
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

    @Override
    public List<MailOutbox> findByCampaignIdAndStatusIn(Long campaignId, List<MailOutboxStatus> statuses) {
        return jpaRepository.findByCampaign_IdAndStatusIn(campaignId, statuses);
    }
}
