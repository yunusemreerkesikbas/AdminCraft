package com.backend.infrastructure.persistence.repository;

import java.util.Optional;

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
}
