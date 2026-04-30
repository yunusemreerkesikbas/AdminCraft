package com.backend.application.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.migration.re-encrypt-secrets", havingValue = "true")
@RequiredArgsConstructor
public class SecretReEncryptionRunner implements ApplicationRunner {

    private final SecretReEncryptionService secretReEncryptionService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("SEC-010: starting ECB→GCM secret re-encryption migration");
        int platform = secretReEncryptionService.migratePlatformSecrets();
        log.info("SEC-010: platform secrets migrated={}", platform);
        int tenant = secretReEncryptionService.migrateAllTenantSecrets();
        log.info("SEC-010: tenant secrets migrated={}, migration complete", tenant);
    }
}
