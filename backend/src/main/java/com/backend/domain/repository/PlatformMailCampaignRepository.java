package com.backend.domain.repository;

import java.util.Optional;

import com.backend.domain.entity.PlatformMailCampaign;

public interface PlatformMailCampaignRepository {

    PlatformMailCampaign save(PlatformMailCampaign campaign);

    Optional<PlatformMailCampaign> findById(Long id);

    Optional<PlatformMailCampaign> findTopByTemplateKeyOrderByCreatedAtDesc(String templateKey);
}
