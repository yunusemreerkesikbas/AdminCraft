package com.backend.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;

import com.backend.domain.entity.PlatformConfigProperty;
import com.backend.domain.repository.ConfigPropertyRepository;
import com.backend.domain.repository.PlatformConfigPropertyRepository;
import com.backend.domain.repository.TenantRepository;
import com.backend.infrastructure.security.EncryptionService;

@ExtendWith(MockitoExtension.class)
@DisplayName("SEC-010: SecretReEncryptionService")
class SecretReEncryptionServiceTest {

    @Mock private PlatformConfigPropertyRepository platformConfigPropertyRepository;
    @Mock private ConfigPropertyRepository tenantConfigPropertyRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private TenantDbExecutor tenantDbExecutor;
    @Mock private EncryptionService encryptionService;
    @Mock private PlatformTransactionManager tenantTransactionManager;
    @Mock private PlatformTransactionManager platformTransactionManager;

    private SecretReEncryptionService service;

    @BeforeEach
    void setUp() {
        service = new SecretReEncryptionService(
                platformConfigPropertyRepository,
                tenantConfigPropertyRepository,
                tenantRepository,
                tenantDbExecutor,
                encryptionService,
                tenantTransactionManager,
                platformTransactionManager);
    }

    @Test
    @DisplayName("SEC-010: ECB-encrypted secret is re-saved with GCM ciphertext")
    void migratePlatformSecrets_ecbValue_isSaved() {
        PlatformConfigProperty prop = secretProp("recaptcha.secret", "ECB_CIPHER_BASE64");
        when(platformConfigPropertyRepository.findAll()).thenReturn(List.of(prop));
        when(encryptionService.reEncryptIfLegacy("ECB_CIPHER_BASE64"))
                .thenReturn(java.util.Optional.of("GCM_CIPHER_BASE64"));

        int migrated = service.migratePlatformSecrets();

        assertThat(migrated).isEqualTo(1);
        assertThat(prop.getConfigValue()).isEqualTo("GCM_CIPHER_BASE64");
        verify(platformConfigPropertyRepository).save(prop);
    }

    @Test
    @DisplayName("SEC-010: already-GCM secret is skipped without DB write")
    void migratePlatformSecrets_gcmValue_isSkipped() {
        PlatformConfigProperty prop = secretProp("recaptcha.secret", "GCM_CIPHER_BASE64");
        when(platformConfigPropertyRepository.findAll()).thenReturn(List.of(prop));
        when(encryptionService.reEncryptIfLegacy("GCM_CIPHER_BASE64"))
                .thenReturn(java.util.Optional.empty());

        int migrated = service.migratePlatformSecrets();

        assertThat(migrated).isEqualTo(0);
        verify(platformConfigPropertyRepository, never()).save(any());
    }

    @Test
    @DisplayName("SEC-010: non-secret property is filtered out before re-encryption")
    void migratePlatformSecrets_nonSecret_isSkipped() {
        PlatformConfigProperty prop = new PlatformConfigProperty();
        prop.setConfigKey("site.name");
        prop.setConfigValue("Craftive");
        prop.setSecret(false);

        when(platformConfigPropertyRepository.findAll()).thenReturn(List.of(prop));

        int migrated = service.migratePlatformSecrets();

        assertThat(migrated).isEqualTo(0);
        verify(encryptionService, never()).reEncryptIfLegacy(any());
        verify(platformConfigPropertyRepository, never()).save(any());
    }

    private PlatformConfigProperty secretProp(String key, String value) {
        PlatformConfigProperty p = new PlatformConfigProperty();
        p.setConfigKey(key);
        p.setConfigValue(value);
        p.setSecret(true);
        return p;
    }
}
