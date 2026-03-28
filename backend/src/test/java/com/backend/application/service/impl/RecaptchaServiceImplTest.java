package com.backend.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.web.client.RestTemplate;

import com.backend.application.service.config.ConfigPropertyService;
import com.backend.application.service.config.GlobalRuntimeConfigService;
import com.backend.domain.port.EncryptionServicePort;
import com.backend.testutil.BaseServiceTest;

class RecaptchaServiceImplTest extends BaseServiceTest {

    private static final Long TEST_TENANT_ID_LONG = 1L;

    @Mock
    private GlobalRuntimeConfigService globalRuntimeConfigService;

    @Mock
    private ConfigPropertyService configPropertyService;

    @Mock
    private EncryptionServicePort encryptionService;

    @Mock
    private RestTemplate restTemplate;

    private RecaptchaServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RecaptchaServiceImpl(
                globalRuntimeConfigService,
                configPropertyService,
                getTenantContext(),
                encryptionService,
                restTemplate);
    }

    @Test
    @DisplayName("getPublicClientConfig should read tenant reCAPTCHA state only from config properties")
    void getPublicClientConfig_ShouldUseConfigPropertiesOnly() {
        when(configPropertyService.getBoolean(
                TEST_TENANT_ID_LONG,
                TEST_TENANT_DB_NAME,
                "security.recaptcha.enabled",
                false)).thenReturn(true);
        when(configPropertyService.findRaw(
                TEST_TENANT_ID_LONG,
                TEST_TENANT_DB_NAME,
                "security.recaptcha.site_key")).thenReturn(Optional.of("override-site-key"));

        Map<String, String> result = service.getPublicClientConfig();

        assertThat(result).containsEntry("security.recaptcha.enabled", "true");
        assertThat(result).containsEntry("security.recaptcha.site_key", "override-site-key");
    }

    @Test
    @DisplayName("getPublicClientConfig should return disabled defaults when tenant config properties are missing")
    void getPublicClientConfig_ShouldReturnDisabledDefaults_WhenMissing() {
        when(configPropertyService.getBoolean(
                TEST_TENANT_ID_LONG,
                TEST_TENANT_DB_NAME,
                "security.recaptcha.enabled",
                false)).thenReturn(false);
        when(configPropertyService.findRaw(
                TEST_TENANT_ID_LONG,
                TEST_TENANT_DB_NAME,
                "security.recaptcha.site_key")).thenReturn(Optional.empty());

        Map<String, String> result = service.getPublicClientConfig();

        assertThat(result).containsEntry("security.recaptcha.enabled", "false");
        assertThat(result).containsEntry("security.recaptcha.site_key", "");
    }
}
