package com.backend.infrastructure.persistence.platform.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.domain.enums.MailOutboxStatus;
import com.backend.infrastructure.persistence.platform.entity.PlatformMailOutbox;

@Repository
public interface PlatformMailOutboxRepository extends JpaRepository<PlatformMailOutbox, Long> {

    List<PlatformMailOutbox> findByCampaign_IdAndStatusIn(Long campaignId, Collection<MailOutboxStatus> statuses);
}
