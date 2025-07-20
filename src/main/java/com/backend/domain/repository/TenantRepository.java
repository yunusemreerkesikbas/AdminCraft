package com.backend.domain.repository;

import com.backend.domain.entity.Tenant;
import com.backend.domain.enums.TenantStatus;

import java.util.List;
import java.util.Optional;

public interface TenantRepository {
    
    Tenant save(Tenant tenant);
    
    Optional<Tenant> findById(Long id);
    
    Optional<Tenant> findBySubdomain(String subdomain);
    
    Optional<Tenant> findByDatabaseName(String databaseName);
    
    Optional<Tenant> findByCustomDomain(String customDomain);
    
    List<Tenant> findByStatus(TenantStatus status);
    
    List<Tenant> findByAdminEmail(String adminEmail);
    
    List<Tenant> findAll();
    
    void deleteById(Long id);
    
    boolean existsBySubdomain(String subdomain);
    
    boolean existsByDatabaseName(String databaseName);
    
    boolean existsByCustomDomain(String customDomain);
    
    long countByStatus(TenantStatus status);
}