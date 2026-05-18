package com.backend.infrastructure.persistence.platform.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.backend.infrastructure.persistence.platform.entity.PlatformOutreachCampaign;

import java.util.Optional;

@Repository
public interface PlatformOutreachCampaignRepository extends JpaRepository<PlatformOutreachCampaign, Long> {

    Page<PlatformOutreachCampaign> findAll(Pageable pageable);

    boolean existsByTemplateId(Long templateId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM PlatformOutreachCampaign c WHERE c.id = :id")
    Optional<PlatformOutreachCampaign> findByIdForUpdate(@Param("id") Long id);
}
