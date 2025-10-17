package com.backend.infrastructure.persistence.platform.repository;

import com.backend.infrastructure.persistence.platform.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantPlatformRepository extends JpaRepository<Tenant, Long> {

  Optional<Tenant> findBySubdomain(String subdomain);

  Optional<Tenant> findByDbName(String dbName);

  boolean existsBySubdomain(String subdomain);
}
