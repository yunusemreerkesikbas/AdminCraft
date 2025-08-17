package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.Tenant;
import com.backend.domain.enums.TenantStatus;
import com.backend.domain.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TenantRepositoryImpl implements TenantRepository {
    
    private final TenantJpaRepository tenantJpaRepository;
    
    @Override
    public Tenant save(Tenant tenant) {
        return tenantJpaRepository.save(tenant);
    }
    
    @Override
    public List<Tenant> saveAll(Iterable<Tenant> tenants) {
        return tenantJpaRepository.saveAll(tenants);
    }
    
    @Override
    public Optional<Tenant> findById(Long id) {
        return tenantJpaRepository.findById(id);
    }
    
    @Override
    public List<Tenant> findAll() {
        return tenantJpaRepository.findAll();
    }
    
    @Override
    public void deleteById(Long id) {
        tenantJpaRepository.deleteById(id);
    }
    
    @Override
    public boolean existsById(Long id) {
        return tenantJpaRepository.existsById(id);
    }
    
    @Override
    public long count() {
        return tenantJpaRepository.count();
    }
    
    @Override
    public void deleteAllById(Iterable<Long> ids) {
        tenantJpaRepository.deleteAllById(ids);
    }
    
    @Override
    public Optional<Tenant> findBySubdomain(String subdomain) {
        return tenantJpaRepository.findBySubdomain(subdomain);
    }
    
    @Override
    public Optional<Tenant> findByDatabaseName(String databaseName) {
        return tenantJpaRepository.findByDatabaseName(databaseName);
    }
    
    @Override
    public Optional<Tenant> findByCustomDomain(String customDomain) {
        return tenantJpaRepository.findByCustomDomain(customDomain);
    }
    
    @Override
    public List<Tenant> findByStatus(TenantStatus status) {
        return tenantJpaRepository.findByStatus(status);
    }
    
    @Override
    public List<Tenant> findByAdminEmail(String adminEmail) {
        return tenantJpaRepository.findByAdminEmail(adminEmail);
    }
    
    @Override
    public boolean existsBySubdomain(String subdomain) {
        return tenantJpaRepository.existsBySubdomain(subdomain);
    }
    
    @Override
    public boolean existsByDatabaseName(String databaseName) {
        return tenantJpaRepository.existsByDatabaseName(databaseName);
    }
    
    @Override
    public boolean existsByCustomDomain(String customDomain) {
        return tenantJpaRepository.existsByCustomDomain(customDomain);
    }
    
    @Override
    public boolean existsByCustomDomainAndIdNot(String customDomain, Long id) {
        return tenantJpaRepository.existsByCustomDomainAndIdNot(customDomain, id);
    }
    
    @Override
    public long countByStatus(TenantStatus status) {
        return tenantJpaRepository.countByStatus(status);
    }
    
    @Override
    public List<Tenant> findByStatusOrderByCreatedAtDesc(TenantStatus status) {
        return tenantJpaRepository.findByStatusOrderByCreatedAtDesc(status);
    }
    
    @Override
    public List<Tenant> findTenantsExceedingStorageThreshold(Long threshold) {
        return tenantJpaRepository.findTenantsExceedingStorageThreshold(threshold);
    }
    
    @Override
    public long countTenantsActivatedToday() {
        return tenantJpaRepository.countTenantsActivatedToday();
    }
}