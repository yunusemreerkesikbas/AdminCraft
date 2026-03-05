package com.backend.infrastructure.persistence.platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.infrastructure.persistence.platform.entity.PlatformMailOutbox;

@Repository
public interface PlatformMailOutboxRepository extends JpaRepository<PlatformMailOutbox, Long> {
}
