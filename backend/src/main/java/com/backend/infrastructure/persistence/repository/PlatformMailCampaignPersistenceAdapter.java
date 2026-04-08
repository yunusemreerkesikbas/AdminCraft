package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.backend.domain.entity.PlatformMailCampaign;
import com.backend.domain.repository.PlatformMailCampaignRepository;
import com.backend.infrastructure.persistence.platform.mapper.PlatformMailMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PlatformMailCampaignPersistenceAdapter implements PlatformMailCampaignRepository {

    private final com.backend.infrastructure.persistence.platform.repository.PlatformMailCampaignRepository jpaRepository;

    @Override
    public PlatformMailCampaign save(PlatformMailCampaign campaign) {
        return PlatformMailMapper.toDomain(jpaRepository.save(PlatformMailMapper.toEntity(campaign)));
    }

    @Override
    public Optional<PlatformMailCampaign> findById(Long id) {
        return jpaRepository.findById(id).map(PlatformMailMapper::toDomain);
    }

    @Override
    public Optional<PlatformMailCampaign> findTopByTemplateKeyOrderByCreatedAtDesc(String templateKey) {
        return jpaRepository.findTopByTemplate_TemplateKeyIgnoreCaseOrderByCreatedAtDesc(templateKey)
                .map(PlatformMailMapper::toDomain);
    }

    @Override
    public List<PlatformMailCampaign> findRecentByTemplateKey(String templateKey, int limit) {
        return jpaRepository.findByTemplate_TemplateKeyIgnoreCaseOrderByCreatedAtDesc(
                templateKey,
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).stream().map(PlatformMailMapper::toDomain).toList();
    }
}
