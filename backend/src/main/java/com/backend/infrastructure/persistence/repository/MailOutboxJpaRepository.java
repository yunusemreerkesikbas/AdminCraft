package com.backend.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.domain.entity.MailOutbox;

@Repository
public interface MailOutboxJpaRepository extends JpaRepository<MailOutbox, Long> {
}
