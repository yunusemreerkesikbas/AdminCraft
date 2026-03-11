package com.backend.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.domain.entity.MailCampaign;

@Repository
public interface MailCampaignJpaRepository extends JpaRepository<MailCampaign, Long> {

    Optional<MailCampaign> findTopByTemplate_TemplateKeyIgnoreCaseOrderByCreatedAtDesc(String templateKey);
}
