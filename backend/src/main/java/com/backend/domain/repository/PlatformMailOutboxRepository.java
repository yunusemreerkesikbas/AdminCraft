package com.backend.domain.repository;

import java.util.List;

import com.backend.domain.entity.PlatformMailOutbox;
import com.backend.domain.enums.MailOutboxStatus;

public interface PlatformMailOutboxRepository {

    PlatformMailOutbox save(PlatformMailOutbox outbox);

    List<PlatformMailOutbox> findByCampaignIdAndStatusIn(Long campaignId, List<MailOutboxStatus> statuses);
}
