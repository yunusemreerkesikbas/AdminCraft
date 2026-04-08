package com.backend.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.stereotype.Component;

import com.backend.domain.entity.PlatformMailOutbox;
import com.backend.domain.enums.MailOutboxStatus;
import com.backend.domain.repository.PlatformMailOutboxRepository;
import com.backend.infrastructure.persistence.platform.mapper.PlatformMailMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PlatformMailOutboxPersistenceAdapter implements PlatformMailOutboxRepository {

    private final com.backend.infrastructure.persistence.platform.repository.PlatformMailOutboxRepository jpaRepository;

    @Override
    public PlatformMailOutbox save(PlatformMailOutbox outbox) {
        return toDomain(jpaRepository.save(toEntity(outbox)));
    }

    private PlatformMailOutbox toDomain(com.backend.infrastructure.persistence.platform.entity.PlatformMailOutbox source) {
        if (source == null) return null;
        PlatformMailOutbox target = new PlatformMailOutbox();
        target.setId(source.getId());
        target.setCampaign(PlatformMailMapper.toDomain(source.getCampaign()));
        target.setToEmail(source.getToEmail());
        target.setSubject(source.getSubject());
        target.setContent(source.getContent());
        target.setStatus(source.getStatus());
        target.setProviderMessageId(source.getProviderMessageId());
        target.setErrorMessage(source.getErrorMessage());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    @Override
    public List<PlatformMailOutbox> findByCampaignIdAndStatusIn(Long campaignId, List<MailOutboxStatus> statuses) {
        return jpaRepository.findByCampaign_IdAndStatusIn(campaignId, statuses)
                .stream().map(this::toDomain).toList();
    }

    private com.backend.infrastructure.persistence.platform.entity.PlatformMailOutbox toEntity(PlatformMailOutbox source) {
        if (source == null) return null;
        com.backend.infrastructure.persistence.platform.entity.PlatformMailOutbox target =
                new com.backend.infrastructure.persistence.platform.entity.PlatformMailOutbox();
        target.setId(source.getId());
        target.setCampaign(PlatformMailMapper.toEntity(source.getCampaign()));
        target.setToEmail(source.getToEmail());
        target.setSubject(source.getSubject());
        target.setContent(source.getContent());
        target.setStatus(source.getStatus());
        target.setProviderMessageId(source.getProviderMessageId());
        target.setErrorMessage(source.getErrorMessage());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }
}
