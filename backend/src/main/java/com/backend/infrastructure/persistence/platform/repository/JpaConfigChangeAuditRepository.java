package com.backend.infrastructure.persistence.platform.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.infrastructure.persistence.platform.entity.ConfigChangeAudit;

interface JpaConfigChangeAuditRepository extends JpaRepository<ConfigChangeAudit, Long> {

    List<ConfigChangeAudit> findByTargetTenantIdOrderByCreatedAtDesc(Long targetTenantId, Pageable pageable);
}
