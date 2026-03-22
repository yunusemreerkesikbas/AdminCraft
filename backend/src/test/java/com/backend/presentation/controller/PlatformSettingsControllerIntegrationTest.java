package com.backend.presentation.controller;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.backend.application.dto.response.PlatformSettingsData;
import com.backend.application.service.PlatformSettingsService;
import com.backend.domain.enums.TwoFactorPolicy;
import com.backend.presentation.config.TestSecurityConfig;

@WebMvcTest(PlatformSettingsController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ TestSecurityConfig.class, com.backend.shared.common.GlobalExceptionHandler.class })
class PlatformSettingsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlatformSettingsService platformSettingsService;

    @MockBean
    private com.backend.infrastructure.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private com.backend.infrastructure.tenant.TenantFilter tenantFilter;

    @Test
    @DisplayName("GET /platform/settings should return settings for SUPER_ADMIN")
    @WithMockUser(roles = "SUPER_ADMIN")
    void getSettings_ShouldReturnSuccess() throws Exception {
        when(platformSettingsService.getSettings()).thenReturn(
                new PlatformSettingsData(
                        "Craftive",
                        "TR",
                        "TRY",
                        "noreply@craftive.io",
                        "Craftive",
                        TwoFactorPolicy.DISABLED,
                        false,
                        null,
                        new java.math.BigDecimal("0.5")));

        mockMvc.perform(get("/platform/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.platformName").value("Craftive"))
                .andExpect(jsonPath("$.data.defaultCurrency").value("TRY"));
    }

    @Test
    @DisplayName("PATCH /platform/settings should patch subset of fields (null-skip contract)")
    @WithMockUser(roles = "SUPER_ADMIN")
    void patchSettings_ShouldSupportNullSkipSemantics() throws Exception {
        when(platformSettingsService.patchSettings(argThat(request ->
                "Acme Platform".equals(request.platformName())
                        && request.defaultLanguage() == null
                        && request.defaultCurrency() == null
                        && request.emailFromAddress() == null
                        && request.emailFromName() == null
                        && request.twoFactorPolicy() == null
                        && request.recaptchaEnabled() == null
                        && request.recaptchaSiteKey() == null
                        && request.recaptchaSecretKey() == null
                        && request.recaptchaThreshold() == null)))
                .thenReturn(new PlatformSettingsData(
                        "Acme Platform",
                        "TR",
                        "TRY",
                        "noreply@craftive.io",
                        "Craftive",
                        TwoFactorPolicy.DISABLED,
                        false,
                        null,
                        new java.math.BigDecimal("0.5")));

        mockMvc.perform(patch("/platform/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "platformName": "Acme Platform"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.platformName").value("Acme Platform"))
                .andExpect(jsonPath("$.data.defaultCurrency").value("TRY"));

        verify(platformSettingsService).patchSettings(argThat(request ->
                "Acme Platform".equals(request.platformName())
                        && request.defaultLanguage() == null
                        && request.defaultCurrency() == null));
    }

    @Test
    @DisplayName("PATCH /platform/settings should return 400 for invalid currency")
    @WithMockUser(roles = "SUPER_ADMIN")
    void patchSettings_ShouldReturnBadRequestForInvalidCurrency() throws Exception {
        mockMvc.perform(patch("/platform/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "defaultCurrency": "TR"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value("ERROR"));
    }

    @Test
    @DisplayName("GET /platform/settings should return 403 for non-super-admin")
    @WithMockUser(roles = "TENANT_ADMIN")
    void getSettings_ShouldReturnForbiddenForTenantAdmin() throws Exception {
        mockMvc.perform(get("/platform/settings"))
                .andExpect(status().isForbidden());
    }
}
