package com.backend.infrastructure.persistence.repository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Component;

import com.backend.domain.entity.Tenant;
import com.backend.domain.enums.Currency;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.TenantStatus;
import com.backend.domain.repository.TenantRepository;
import com.backend.infrastructure.persistence.platform.repository.TenantPlatformRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TenantRepositoryImpl implements TenantRepository {

    private final TenantPlatformRepository tenantPlatformRepository;
    private final ObjectMapper objectMapper;

    @Override
    public Tenant save(Tenant tenant) {
        com.backend.infrastructure.persistence.platform.entity.Tenant platformTenant = toPlatform(tenant);
        if (tenant.getId() != null) {
            platformTenant.setId(tenant.getId());
        }

        com.backend.infrastructure.persistence.platform.entity.Tenant saved = tenantPlatformRepository
                .save(platformTenant);
        return toDomain(saved);
    }

    @Override
    public List<Tenant> saveAll(Iterable<Tenant> tenants) {
        List<com.backend.infrastructure.persistence.platform.entity.Tenant> platformTenants = StreamSupport
                .stream(tenants.spliterator(), false)
                .map(this::toPlatform)
                .collect(Collectors.toList());

        return tenantPlatformRepository.saveAll(platformTenants).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Tenant> findById(Long id) {
        return tenantPlatformRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Tenant> findAll() {
        return tenantPlatformRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        tenantPlatformRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return tenantPlatformRepository.existsById(id);
    }

    @Override
    public long count() {
        return tenantPlatformRepository.count();
    }

    @Override
    public void deleteAllById(Iterable<Long> ids) {
        tenantPlatformRepository.deleteAllById(ids);
    }

    @Override
    public Optional<Tenant> findBySubdomain(String subdomain) {
        return tenantPlatformRepository.findBySubdomain(subdomain).map(this::toDomain);
    }

    @Override
    public Optional<Tenant> findByDatabaseName(String databaseName) {
        return tenantPlatformRepository.findByDatabaseName(databaseName).map(this::toDomain);
    }

    @Override
    public Optional<Tenant> findByCustomDomain(String customDomain) {
        return tenantPlatformRepository.findByCustomDomain(customDomain).map(this::toDomain);
    }

    @Override
    public List<Tenant> findByStatus(TenantStatus status) {
        return tenantPlatformRepository.findByStatus(status.name()).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsBySubdomain(String subdomain) {
        return tenantPlatformRepository.existsBySubdomain(subdomain);
    }

    @Override
    public boolean existsByDatabaseName(String databaseName) {
        return tenantPlatformRepository.findByDatabaseName(databaseName).isPresent();
    }

    @Override
    public boolean existsByCustomDomain(String customDomain) {
        return tenantPlatformRepository.findByCustomDomain(customDomain).isPresent();
    }

    @Override
    public boolean existsByCustomDomainAndIdNot(String customDomain, Long id) {
        return tenantPlatformRepository.existsByCustomDomainAndIdNot(customDomain, id);
    }

    @Override
    public long countByStatus(TenantStatus status) {
        return tenantPlatformRepository.countByStatus(status.name());
    }

    @Override
    public List<Tenant> findByStatusOrderByCreatedAtDesc(TenantStatus status) {
        return tenantPlatformRepository.findByStatusOrderByCreatedAtDesc(status.name()).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Tenant> findTenantsExceedingStorageThreshold(Long threshold) {
        return tenantPlatformRepository.findTenantsExceedingStorageThreshold(threshold).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countTenantsActivatedToday() {
        return tenantPlatformRepository.countTenantsActivatedToday();
    }

    private Tenant toDomain(com.backend.infrastructure.persistence.platform.entity.Tenant source) {
        if (source == null)
            return null;

        Tenant target = new Tenant();
        target.setId(source.getId());
        target.setSubdomain(source.getSubdomain());
        target.setCompanyName(source.getCompanyName());
        target.setDatabaseName(source.getDatabaseName());
        target.setCustomDomain(source.getCustomDomain());
        target.setStorageUsedMb(source.getStorageUsedMb() != null ? source.getStorageUsedMb() : 0L);
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        target.setActivatedAt(source.getActivatedAt());
        target.setLastBackupAt(source.getLastBackupAt());
        target.setNotes(source.getNotes());
        target.setAdminEmail(source.getAdminEmail());
        target.setAdminName(source.getAdminName());

        try {
            target.setStatus(TenantStatus.valueOf(source.getStatus()));
        } catch (Exception e) {
            log.warn("Invalid tenant status in DB: {}", source.getStatus());
            target.setStatus(TenantStatus.PENDING);
        }

        try {
            if (source.getTwoFactorPolicy() != null) {
                target.setTwoFactorPolicy(
                        com.backend.domain.enums.TwoFactorPolicy.valueOf(source.getTwoFactorPolicy()));
            }
        } catch (Exception e) {
            log.warn("Invalid two factor policy in DB: {}", source.getTwoFactorPolicy());
            target.setTwoFactorPolicy(com.backend.domain.enums.TwoFactorPolicy.DISABLED);
        }

        try {
            target.setCurrency(Currency.valueOf(source.getCurrency()));
        } catch (Exception e) {
            target.setCurrency(Currency.TRY);
        }

        try {
            target.setDefaultLanguage(Language.valueOf(source.getDefaultLanguage()));
        } catch (Exception e) {
            target.setDefaultLanguage(Language.TR);
        }

        if (source.getSupportedLanguages() != null) {
            try {
                Set<Language> langs = objectMapper.readValue(source.getSupportedLanguages(),
                        new TypeReference<Set<Language>>() {
                        });
                target.setSupportedLanguages(langs);
            } catch (JsonProcessingException e) {
                log.error("Failed to parse supported languages for tenant {}", source.getId(), e);
                target.setSupportedLanguages(new HashSet<>(Set.of(Language.TR)));
            }
        } else {
            target.setSupportedLanguages(new HashSet<>(Set.of(Language.TR)));
        }

        return target;
    }

    private com.backend.infrastructure.persistence.platform.entity.Tenant toPlatform(Tenant tenant) {
        if (tenant == null)
            return null;

        String languagesJson = "[\"TR\"]";
        try {
            languagesJson = objectMapper.writeValueAsString(tenant.getSupportedLanguages());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize supported languages for tenant {}", tenant.getSubdomain(), e);
        }

        String dbName = tenant.getDatabaseName();
        if (dbName == null || dbName.isEmpty()) {
            dbName = "tenant_" + tenant.getSubdomain() + "_db";
        }

        return com.backend.infrastructure.persistence.platform.entity.Tenant.builder()
                .id(tenant.getId())
                .subdomain(tenant.getSubdomain())
                .companyName(tenant.getCompanyName())
                .databaseName(dbName)
                .customDomain(tenant.getCustomDomain())
                .status(tenant.getStatus().name())
                .twoFactorPolicy(tenant.getTwoFactorPolicy() != null ? tenant.getTwoFactorPolicy().name() : "DISABLED")
                .currency(tenant.getCurrency().name())
                .defaultLanguage(tenant.getDefaultLanguage().name())
                .supportedLanguages(languagesJson)
                .adminEmail(tenant.getAdminEmail())
                .adminName(tenant.getAdminName())
                .storageUsedMb(tenant.getStorageUsedMb())
                .activatedAt(tenant.getActivatedAt())
                .lastBackupAt(tenant.getLastBackupAt())
                .notes(tenant.getNotes())
                .hasAdminUser(tenant.getAdminEmail() != null)
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .build();
    }
}
