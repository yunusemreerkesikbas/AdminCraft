package com.backend.infrastructure.persistence.platform.repository;

import com.backend.infrastructure.persistence.platform.entity.ProvisioningJob;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProvisioningJobRepository extends JpaRepository<ProvisioningJob, Long> {

  List<ProvisioningJob> findByTenantIdOrderByCreatedAtDesc(Long tenantId);

  List<ProvisioningJob> findByStatus(String status);

  Optional<ProvisioningJob> findFirstByTenantIdOrderByCreatedAtDesc(Long tenantId);

  @EntityGraph(attributePaths = "tenant")
  List<ProvisioningJob> findTop5ByOrderByCreatedAtDesc();

  @Query("""
      SELECT pj.tenantId, pj.status
      FROM ProvisioningJob pj
      WHERE pj.tenantId IN :tenantIds
        AND pj.createdAt = (
          SELECT MAX(p2.createdAt)
          FROM ProvisioningJob p2
          WHERE p2.tenantId = pj.tenantId
        )
      """)
  List<Object[]> findLatestStatusesByTenantIds(@Param("tenantIds") Collection<Long> tenantIds);
}
