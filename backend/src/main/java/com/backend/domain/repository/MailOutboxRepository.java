package com.backend.domain.repository;

import java.util.List;
import java.util.Optional;

import com.backend.domain.entity.MailOutbox;
import com.backend.domain.enums.MailOutboxStatus;

public interface MailOutboxRepository {

    MailOutbox save(MailOutbox outbox);

    Optional<MailOutbox> findById(Long id);

    List<MailOutbox> findByCampaignIdAndStatusIn(Long campaignId, List<MailOutboxStatus> statuses);
}
