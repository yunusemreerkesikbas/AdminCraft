package com.backend.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.backend.application.service.config.ConfigPropertyService;
import com.backend.domain.entity.Tenant;

@ExtendWith(MockitoExtension.class)
class OtpResendCooldownServiceTest {

    @Mock
    private ConfigPropertyService configPropertyService;

    @InjectMocks
    private OtpResendCooldownService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "platformCooldownSeconds", 180);
    }

    @Test
    @DisplayName("resolveTenantCooldownSeconds uses config when present")
    void resolveTenantCooldownSeconds_ShouldReadConfig() {
        when(configPropertyService.findRaw(1L, "tenant_db", OtpResendCooldownService.CONFIG_KEY))
                .thenReturn(Optional.of("120"));

        assertThat(service.resolveTenantCooldownSeconds(1L, "tenant_db")).isEqualTo(120);
    }

    @Test
    @DisplayName("resolveTenantCooldownSeconds clamps out-of-range values")
    void resolveTenantCooldownSeconds_ShouldClamp() {
        when(configPropertyService.findRaw(1L, "tenant_db", OtpResendCooldownService.CONFIG_KEY))
                .thenReturn(Optional.of("99999"));

        assertThat(service.resolveTenantCooldownSeconds(1L, "tenant_db"))
                .isEqualTo(OtpResendCooldownService.MAX_COOLDOWN_SECONDS);
    }

    @Test
    @DisplayName("resolveTenantCooldownSeconds falls back on invalid config")
    void resolveTenantCooldownSeconds_ShouldFallbackOnInvalid() {
        when(configPropertyService.findRaw(1L, "tenant_db", OtpResendCooldownService.CONFIG_KEY))
                .thenReturn(Optional.of("not-a-number"));

        assertThat(service.resolveTenantCooldownSeconds(1L, "tenant_db"))
                .isEqualTo(OtpResendCooldownService.DEFAULT_COOLDOWN_SECONDS);
    }

    @Test
    @DisplayName("resolveTenantCooldownSeconds uses default when tenant missing")
    void resolveTenantCooldownSeconds_ShouldDefaultWithoutTenant() {
        assertThat(service.resolveTenantCooldownSeconds((Long) null, "db"))
                .isEqualTo(OtpResendCooldownService.DEFAULT_COOLDOWN_SECONDS);
    }

    @Test
    @DisplayName("resolveTenantCooldownSeconds resolves from tenant entity")
    void resolveTenantCooldownSeconds_ShouldResolveFromTenant() {
        Tenant tenant = new Tenant();
        tenant.setId(2L);
        tenant.setDatabaseName("tenant_2");
        when(configPropertyService.findRaw(2L, "tenant_2", OtpResendCooldownService.CONFIG_KEY))
                .thenReturn(Optional.of("90"));

        assertThat(service.resolveTenantCooldownSeconds(tenant)).isEqualTo(90);
    }

    @Test
    @DisplayName("resolvePlatformCooldownSeconds clamps application property")
    void resolvePlatformCooldownSeconds_ShouldClamp() {
        ReflectionTestUtils.setField(service, "platformCooldownSeconds", 30);

        assertThat(service.resolvePlatformCooldownSeconds())
                .isEqualTo(OtpResendCooldownService.MIN_COOLDOWN_SECONDS);
    }
}
