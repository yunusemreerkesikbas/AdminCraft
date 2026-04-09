package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
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
        com.backend.infrastructure.persistence.platform.entity.PlatformMailCampaign entity =
                campaign.getId() == null
                        ? new com.backend.infrastructure.persistence.platform.entity.PlatformMailCampaign()
                        : jpaRepository.findById(campaign.getId())
                                .orElseThrow(() -> new IllegalArgumentException("mail.marketing.campaign.not.found"));
        entity.setId(campaign.getId());
        entity.setTemplate(toTemplateReference(campaign.getTemplate()));
        entity.setSubject(campaign.getSubject());
        entity.setContent(campaign.getContent());
        entity.setStatus(campaign.getStatus());
        entity.setTotalCount(campaign.getTotalCount());
        entity.setSentCount(campaign.getSentCount());
        entity.setFailedCount(campaign.getFailedCount());
        entity.setCreatedByEmail(campaign.getCreatedByEmail());
        return PlatformMailMapper.toDomain(jpaRepository.save(entity));
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
                PageRequest.of(0, limit)
        ).stream().map(PlatformMailMapper::toDomain).toList();
    }

    private com.backend.infrastructure.persistence.platform.entity.PlatformEmailTemplate toTemplateReference(
            com.backend.domain.entity.PlatformEmailTemplate template) {
        if (template == null || template.getId() == null) {
            return null;
        }
        com.backend.infrastructure.persistence.platform.entity.PlatformEmailTemplate ref =
                new com.backend.infrastructure.persistence.platform.entity.PlatformEmailTemplate();
        ref.setId(template.getId());
        return ref;
    }
}
