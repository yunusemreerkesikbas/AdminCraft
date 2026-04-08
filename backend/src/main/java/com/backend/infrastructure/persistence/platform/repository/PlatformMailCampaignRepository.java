package com.backend.infrastructure.persistence.platform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.domain.enums.MailOutboxStatus;
import com.backend.infrastructure.persistence.platform.entity.PlatformMailCampaign;

@Repository
public interface PlatformMailCampaignRepository extends JpaRepository<PlatformMailCampaign, Long> {

    Optional<PlatformMailCampaign> findTopByTemplate_TemplateKeyIgnoreCaseOrderByCreatedAtDesc(String templateKey);

    List<PlatformMailCampaign> findByTemplate_TemplateKeyIgnoreCaseOrderByCreatedAtDesc(String templateKey, Pageable pageable);
}
