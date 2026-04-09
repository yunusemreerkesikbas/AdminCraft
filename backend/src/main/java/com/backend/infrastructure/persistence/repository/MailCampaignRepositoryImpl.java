package com.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.backend.domain.entity.MailCampaign;
import com.backend.domain.repository.MailCampaignRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MailCampaignRepositoryImpl implements MailCampaignRepository {

    private final MailCampaignJpaRepository jpaRepository;

    @Override
    public MailCampaign save(MailCampaign campaign) {
        return jpaRepository.save(campaign);
    }

    @Override
    public Optional<MailCampaign> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<MailCampaign> findTopByTemplateKeyOrderByCreatedAtDesc(String templateKey) {
        return jpaRepository.findTopByTemplate_TemplateKeyIgnoreCaseOrderByCreatedAtDesc(templateKey);
    }

    @Override
    public List<MailCampaign> findRecentByTemplateKey(String templateKey, int limit) {
        return jpaRepository.findByTemplate_TemplateKeyIgnoreCaseOrderByCreatedAtDesc(
                templateKey,
                PageRequest.of(0, limit)
        );
    }
}
