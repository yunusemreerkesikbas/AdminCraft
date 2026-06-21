package com.backend.infrastructure.persistence.commerce;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.domain.commerce.CommerceNotificationOutbox;

interface CommerceNotificationOutboxJpaRepository extends JpaRepository<CommerceNotificationOutbox, Long> {
}
