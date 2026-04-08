package com.backend.domain.repository;

import java.util.List;
import java.util.Optional;

import com.backend.domain.entity.MailCampaign;

public interface MailCampaignRepository {

    MailCampaign save(MailCampaign campaign);

    Optional<MailCampaign> findById(Long id);

    Optional<MailCampaign> findTopByTemplateKeyOrderByCreatedAtDesc(String templateKey);

    List<MailCampaign> findRecentByTemplateKey(String templateKey, int limit);
}
