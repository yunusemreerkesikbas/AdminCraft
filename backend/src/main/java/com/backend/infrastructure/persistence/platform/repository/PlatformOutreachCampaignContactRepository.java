package com.backend.infrastructure.persistence.platform.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.backend.domain.enums.OutreachCampaignContactStatus;
import com.backend.infrastructure.persistence.platform.entity.PlatformOutreachCampaignContact;

@Repository
public interface PlatformOutreachCampaignContactRepository extends JpaRepository<PlatformOutreachCampaignContact, Long> {

    @Query("SELECT cc FROM PlatformOutreachCampaignContact cc JOIN FETCH cc.contact WHERE cc.campaign.id = :campaignId")
    List<PlatformOutreachCampaignContact> findByCampaignId(@Param("campaignId") Long campaignId);

    @Query(value = "SELECT cc FROM PlatformOutreachCampaignContact cc JOIN FETCH cc.contact WHERE cc.campaign.id = :campaignId",
           countQuery = "SELECT COUNT(cc) FROM PlatformOutreachCampaignContact cc WHERE cc.campaign.id = :campaignId")
    Page<PlatformOutreachCampaignContact> findByCampaignId(@Param("campaignId") Long campaignId, Pageable pageable);

    List<PlatformOutreachCampaignContact> findByCampaignIdAndStatus(Long campaignId, OutreachCampaignContactStatus status);
}
