package com.backend.infrastructure.persistence.platform.repository;

import com.backend.infrastructure.persistence.platform.entity.ProvisioningJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProvisioningJobRepository extends JpaRepository<ProvisioningJob, Long> {

  List<ProvisioningJob> findByTenantIdOrderByCreatedAtDesc(Long tenantId);

  List<ProvisioningJob> findByStatus(String status);
}

