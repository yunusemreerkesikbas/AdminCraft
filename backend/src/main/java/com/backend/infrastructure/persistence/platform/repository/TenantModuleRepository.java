package com.backend.infrastructure.persistence.platform.repository;

import com.backend.infrastructure.persistence.platform.entity.TenantModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenantModuleRepository extends JpaRepository<TenantModule, Long> {

  List<TenantModule> findByTenantId(Long tenantId);

  List<TenantModule> findByTenantIdAndStatus(Long tenantId, String status);

  @Query("SELECT tm FROM TenantModule tm WHERE tm.tenantId = :tenantId AND tm.status = :status")
  List<TenantModule> findEnabledModulesByTenantId(@Param("tenantId") Long tenantId, @Param("status") String status);

  boolean existsByTenantIdAndModuleCode(Long tenantId, String moduleCode);
}
