package com.backend.infrastructure.persistence.repository;

import com.backend.domain.entity.Tenant;
import com.backend.domain.enums.TenantStatus;
import com.backend.domain.repository.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TenantRepositoryImpl implements TenantRepository {

    @Autowired
    private TenantJpaRepository tenantJpaRepository;

    @Override
    public Tenant save(Tenant tenant) {
        return tenantJpaRepository.save(tenant);
    }

    @Override
    public Optional<Tenant> findById(Long id) {
        return tenantJpaRepository.findById(id);
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
    public List<Tenant> findAll() {
        return tenantJpaRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        tenantJpaRepository.deleteById(id);
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
}