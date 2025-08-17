package com.backend.domain.repository;

import com.backend.domain.entity.Tenant;
import com.backend.domain.enums.TenantStatus;

import java.util.List;
import java.util.Optional;

public interface TenantRepository {
    
    // Basic CRUD operations
    Tenant save(Tenant tenant);
    List<Tenant> saveAll(Iterable<Tenant> tenants);
    Optional<Tenant> findById(Long id);
    List<Tenant> findAll();
    void deleteById(Long id);
    boolean existsById(Long id);
    long count();
    void deleteAllById(Iterable<Long> ids);
    
    Optional<Tenant> findBySubdomain(String subdomain);
    
    Optional<Tenant> findByDatabaseName(String databaseName);
    
    Optional<Tenant> findByCustomDomain(String customDomain);
    
    List<Tenant> findByStatus(TenantStatus status);
    
    List<Tenant> findByAdminEmail(String adminEmail);
    
    boolean existsBySubdomain(String subdomain);
    
    boolean existsByDatabaseName(String databaseName);
    
    boolean existsByCustomDomain(String customDomain);
    
    boolean existsByCustomDomainAndIdNot(String customDomain, Long id);
    
    long countByStatus(TenantStatus status);
    
    List<Tenant> findByStatusOrderByCreatedAtDesc(TenantStatus status);

    List<Tenant> findTenantsExceedingStorageThreshold(Long threshold);

    long countTenantsActivatedToday();
}