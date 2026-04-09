package com.backend.domain.repository;

import java.util.List;
import java.util.Optional;

import com.backend.domain.entity.PlatformMailOutbox;
import com.backend.domain.enums.MailOutboxStatus;

public interface PlatformMailOutboxRepository {

    PlatformMailOutbox save(PlatformMailOutbox outbox);

    Optional<PlatformMailOutbox> findById(Long id);

    List<PlatformMailOutbox> findByCampaignIdAndStatusIn(Long campaignId, List<MailOutboxStatus> statuses);
}
