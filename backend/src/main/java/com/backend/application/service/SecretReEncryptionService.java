package com.backend.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.backend.domain.entity.ConfigProperty;
import com.backend.domain.entity.PlatformConfigProperty;
import com.backend.domain.entity.Tenant;
import com.backend.domain.repository.ConfigPropertyRepository;
import com.backend.domain.repository.PlatformConfigPropertyRepository;
import com.backend.domain.repository.TenantRepository;
import com.backend.infrastructure.security.EncryptionService;

import lombok.extern.slf4j.Slf4j;

/**
 * SEC-010: one-shot ECB→GCM secret re-encryption for platform and tenant config properties.
 * Idempotent: values already in GCM format are skipped without modification.
 */
@Slf4j
@Service
public class SecretReEncryptionService {

    private final PlatformConfigPropertyRepository platformConfigPropertyRepository;
    private final ConfigPropertyRepository tenantConfigPropertyRepository;
    private final TenantRepository tenantRepository;
    private final TenantDbExecutor tenantDbExecutor;
    private final EncryptionService encryptionService;
    private final PlatformTransactionManager tenantTransactionManager;
    private final PlatformTransactionManager platformTransactionManager;

    public SecretReEncryptionService(
            PlatformConfigPropertyRepository platformConfigPropertyRepository,
            ConfigPropertyRepository tenantConfigPropertyRepository,
            TenantRepository tenantRepository,
            TenantDbExecutor tenantDbExecutor,
            EncryptionService encryptionService,
            @Qualifier("tenantTransactionManager") PlatformTransactionManager tenantTransactionManager,
            @Qualifier("platformTransactionManager") PlatformTransactionManager platformTransactionManager) {
        this.platformConfigPropertyRepository = platformConfigPropertyRepository;
        this.tenantConfigPropertyRepository = tenantConfigPropertyRepository;
        this.tenantRepository = tenantRepository;
        this.tenantDbExecutor = tenantDbExecutor;
        this.encryptionService = encryptionService;
        this.tenantTransactionManager = tenantTransactionManager;
        this.platformTransactionManager = platformTransactionManager;
    }

    @Transactional("platformTransactionManager")
    public int migratePlatformSecrets() {
        List<PlatformConfigProperty> secrets = platformConfigPropertyRepository.findAll()
                .stream()
                .filter(p -> Boolean.TRUE.equals(p.getSecret()) && p.getConfigValue() != null)
                .toList();

        int migrated = 0;
        for (PlatformConfigProperty prop : secrets) {
            Optional<String> newValue = encryptionService.reEncryptIfLegacy(prop.getConfigValue());
            if (newValue.isPresent()) {
                prop.setConfigValue(newValue.get());
                platformConfigPropertyRepository.save(prop);
                migrated++;
                log.info("SEC-010: platform secret migrated ECB→GCM, key={}", prop.getConfigKey());
            }
        }
        return migrated;
    }

    public int migrateAllTenantSecrets() {
        List<Tenant> tenants = tenantRepository.findAll();
        int total = 0;
        for (Tenant tenant : tenants) {
            try {
                int count = tenantDbExecutor.withTenant(
                        tenant.getId(), tenant.getDatabaseName(),
                        () -> migrateTenantSecretsInContext(tenant.getId()));
                total += count;
                if (count > 0) {
                    log.info("SEC-010: tenant {} migrated {} secret(s) ECB→GCM", tenant.getId(), count);
                }
            } catch (Exception e) {
                log.error("SEC-010: failed to migrate secrets for tenant {}: {}", tenant.getId(), e.getMessage());
            }
        }
        return total;
    }

    private int migrateTenantSecretsInContext(Long tenantId) {
        TransactionTemplate txTemplate = new TransactionTemplate(tenantTransactionManager);
        Integer result = txTemplate.execute(status -> {
            List<ConfigProperty> secrets = tenantConfigPropertyRepository.findAll()
                    .stream()
                    .filter(p -> Boolean.TRUE.equals(p.getSecret()) && p.getConfigValue() != null)
                    .toList();
            int migrated = 0;
            for (ConfigProperty prop : secrets) {
                Optional<String> newValue = encryptionService.reEncryptIfLegacy(prop.getConfigValue());
                if (newValue.isPresent()) {
                    prop.setConfigValue(newValue.get());
                    tenantConfigPropertyRepository.save(prop);
                    migrated++;
                }
            }
            return migrated;
        });
        return result != null ? result : 0;
    }
}
