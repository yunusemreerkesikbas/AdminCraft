package com.backend.infrastructure.persistence.platform.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.backend.infrastructure.persistence.platform.entity.Tenant;

@Repository
public interface TenantPlatformRepository extends JpaRepository<Tenant, Long> {

  Optional<Tenant> findBySubdomain(String subdomain);

  Optional<Tenant> findByDatabaseName(String databaseName);

  Optional<Tenant> findByCustomDomain(String customDomain);

  boolean existsBySubdomain(String subdomain);

  boolean existsByCustomDomainAndIdNot(String customDomain, Long id);

  long countByStatus(String status);

  java.util.List<Tenant> findByStatus(String status);

  java.util.List<Tenant> findByStatusOrderByCreatedAtDesc(String status);

  @Query("SELECT t FROM Tenant t WHERE t.storageUsedMb > :threshold")
  java.util.List<Tenant> findTenantsExceedingStorageThreshold(Long threshold);

  @Query("SELECT COUNT(t) FROM Tenant t WHERE t.activatedAt >= CURRENT_DATE")
  long countTenantsActivatedToday();
}
