package com.backend.application.service.impl.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.backend.application.service.TenantDbExecutor;
import com.backend.domain.entity.ConfigProperty;
import com.backend.domain.port.EncryptionServicePort;
import com.backend.domain.repository.ConfigPropertyRepository;
import com.backend.testutil.BaseServiceTest;

class ConfigPropertyServiceImplTest extends BaseServiceTest {

    private static final Long TEST_TENANT_ID_LONG = 1L;

    @Mock
    private ConfigPropertyRepository configPropertyRepository;

    @Mock
    private EncryptionServicePort encryptionService;

    private TenantDbExecutor tenantDbExecutor;

    @InjectMocks
    private ConfigPropertyServiceImpl configPropertyService;

    @BeforeEach
    void setUp() {
        tenantDbExecutor = new TenantDbExecutor(getTenantContext());
        configPropertyService = new ConfigPropertyServiceImpl(tenantDbExecutor, configPropertyRepository, encryptionService);
    }

    @Test
    @DisplayName("getBoolean should parse common truthy/falsey values")
    void getBoolean_ShouldParseValues() {
        ConfigProperty p = new ConfigProperty();
        p.setConfigKey("k");
        p.setConfigValue("yes");
        when(configPropertyRepository.findByConfigKey("k")).thenReturn(Optional.of(p));

        boolean result = configPropertyService.getBoolean(TEST_TENANT_ID_LONG, TEST_TENANT_DB_NAME, "k", false);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("getBoolean should return default when missing or invalid")
    void getBoolean_ShouldReturnDefault_WhenMissingOrInvalid() {
        when(configPropertyRepository.findByConfigKey("missing")).thenReturn(Optional.empty());
        boolean missing = configPropertyService.getBoolean(TEST_TENANT_ID_LONG, TEST_TENANT_DB_NAME, "missing", true);
        assertThat(missing).isTrue();

        ConfigProperty invalid = new ConfigProperty();
        invalid.setConfigKey("bad");
        invalid.setConfigValue("notabool");
        when(configPropertyRepository.findByConfigKey("bad")).thenReturn(Optional.of(invalid));
        boolean bad = configPropertyService.getBoolean(TEST_TENANT_ID_LONG, TEST_TENANT_DB_NAME, "bad", false);
        assertThat(bad).isFalse();
    }

    @Test
    @DisplayName("getDecimal should parse decimal values and fall back to default on invalid")
    void getDecimal_ShouldParseOrDefault() {
        ConfigProperty ok = new ConfigProperty();
        ok.setConfigKey("d1");
        ok.setConfigValue("0.75");
        when(configPropertyRepository.findByConfigKey("d1")).thenReturn(Optional.of(ok));

        BigDecimal parsed = configPropertyService.getDecimal(TEST_TENANT_ID_LONG, TEST_TENANT_DB_NAME, "d1", new BigDecimal("0.5"));
        assertThat(parsed).isEqualByComparingTo("0.75");

        ConfigProperty bad = new ConfigProperty();
        bad.setConfigKey("d2");
        bad.setConfigValue("x");
        when(configPropertyRepository.findByConfigKey("d2")).thenReturn(Optional.of(bad));

        BigDecimal fallback = configPropertyService.getDecimal(TEST_TENANT_ID_LONG, TEST_TENANT_DB_NAME, "d2", new BigDecimal("0.5"));
        assertThat(fallback).isEqualByComparingTo("0.5");
    }

    @Test
    @DisplayName("upsert should encrypt value when secret=true")
    void upsert_ShouldEncrypt_WhenSecret() {
        when(configPropertyRepository.findByConfigKey("s")).thenReturn(Optional.empty());
        when(encryptionService.encrypt("plain")).thenReturn("enc");
        when(configPropertyRepository.save(any(ConfigProperty.class))).thenAnswer(inv -> inv.getArgument(0));

        ConfigProperty saved = configPropertyService.upsert(
                TEST_TENANT_ID_LONG,
                TEST_TENANT_DB_NAME,
                "s",
                "plain",
                true,
                TEST_USER_ID);

        assertThat(saved.getConfigKey()).isEqualTo("s");
        assertThat(saved.getSecret()).isTrue();
        assertThat(saved.getConfigValue()).isEqualTo("enc");
        assertThat(saved.getUpdatedBy()).isEqualTo(TEST_USER_ID);

        verify(encryptionService).encrypt("plain");
        verify(configPropertyRepository).save(any(ConfigProperty.class));
        verify(configPropertyRepository).findByConfigKey(eq("s"));
    }
}

