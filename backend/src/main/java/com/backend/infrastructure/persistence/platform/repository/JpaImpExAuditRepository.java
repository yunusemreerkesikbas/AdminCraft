package com.backend.infrastructure.persistence.platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.infrastructure.persistence.platform.entity.ImpExAudit;

interface JpaImpExAuditRepository extends JpaRepository<ImpExAudit, Long> {
}
