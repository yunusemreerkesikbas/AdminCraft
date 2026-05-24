package com.backend.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import com.backend.application.dto.TwoFactorPolicyChangeRequestResult;
import com.backend.application.service.PlatformSecuritySettingsService;
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
    private PlatformSecuritySettingsService platformSecuritySettingsService;

    @MockBean
    private org.springframework.context.MessageSource messageSource;

    @MockBean
    private com.backend.infrastructure.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private com.backend.infrastructure.tenant.TenantFilter tenantFilter;

    @MockBean
    private com.backend.application.cms.preview.CmsPreviewTicketService cmsPreviewTicketService;

    @MockBean
    private com.backend.application.cms.preview.CmsRequestContext cmsRequestContext;

    @MockBean
    private com.backend.domain.port.TenantContextPort tenantContextPort;

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
                        TwoFactorPolicy.DISABLED));

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
                        && request.twoFactorPolicy() == null)))
                .thenReturn(new PlatformSettingsData(
                        "Acme Platform",
                        "TR",
                        "TRY",
                        "noreply@craftive.io",
                        "Craftive",
                        TwoFactorPolicy.DISABLED));

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
    @DisplayName("PATCH /platform/settings should return 409 when twoFactorPolicy is sent directly")
    @WithMockUser(roles = "SUPER_ADMIN")
    void patchSettings_ShouldRejectDirectTwoFactorPolicyChange() throws Exception {
        when(platformSettingsService.patchSettings(any())).thenThrow(
                new com.backend.domain.exception.TwoFactorPolicyVerificationRequiredException());
        when(messageSource.getMessage(
                eq("platform.settings.security.verification.required"),
                any(),
                any()))
                .thenReturn("Verification required");

        mockMvc.perform(patch("/platform/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "twoFactorPolicy": "REQUIRED"
                        }
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.result").value("ERROR"));
    }

    @Test
    @DisplayName("POST /platform/settings/two-factor/request-change should return pending session")
    @WithMockUser(roles = "SUPER_ADMIN")
    void requestTwoFactorPolicyChange_ShouldReturnPendingSession() throws Exception {
        when(platformSecuritySettingsService.requestTwoFactorPolicyChange(
                eq(TwoFactorPolicy.REQUIRED),
                any(),
                any()))
                .thenReturn(new TwoFactorPolicyChangeRequestResult(
                        "pending-1",
                        "a****@craftive.io",
                        TwoFactorPolicy.REQUIRED,
                        300));
        when(messageSource.getMessage(eq("platform.settings.security.twoFactor.otp.sent"), any(), any()))
                .thenReturn("OTP sent");

        mockMvc.perform(post("/platform/settings/two-factor/request-change")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "twoFactorPolicy": "REQUIRED"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pendingChangeId").value("pending-1"))
                .andExpect(jsonPath("$.data.targetPolicy").value("REQUIRED"));
    }

    @Test
    @DisplayName("GET /platform/settings should return 403 for non-super-admin")
    @WithMockUser(roles = "TENANT_ADMIN")
    void getSettings_ShouldReturnForbiddenForTenantAdmin() throws Exception {
        mockMvc.perform(get("/platform/settings"))
                .andExpect(status().isForbidden());
    }
}
