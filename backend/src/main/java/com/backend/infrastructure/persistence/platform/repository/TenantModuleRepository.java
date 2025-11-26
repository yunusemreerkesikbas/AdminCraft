package com.backend.infrastructure.persistence.platform.repository;

import com.backend.infrastructure.persistence.platform.entity.TenantModule;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenantModuleRepository extends JpaRepository<TenantModule, Long> {

  List<TenantModule> findByTenantId(Long tenantId);

  @EntityGraph(attributePaths = "moduleCatalog")
  List<TenantModule> findByTenantIdAndStatus(Long tenantId, String status);

  boolean existsByTenantIdAndModuleCode(Long tenantId, String moduleCode);

  @Query("SELECT COUNT(tm) FROM TenantModule tm WHERE tm.tenantId = :tenantId AND tm.status = 'enabled'")
  Integer countEnabledModulesByTenantId(@Param("tenantId") Long tenantId);
}
