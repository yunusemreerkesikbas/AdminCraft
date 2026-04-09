package com.backend.infrastructure.persistence.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.domain.entity.MailOutbox;
import com.backend.domain.enums.MailOutboxStatus;

@Repository
public interface MailOutboxJpaRepository extends JpaRepository<MailOutbox, Long> {

    List<MailOutbox> findByCampaign_IdAndStatusIn(Long campaignId, Collection<MailOutboxStatus> statuses);
}
