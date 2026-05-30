package com.backend.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
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
import org.springframework.test.web.servlet.MockMvc;

import com.backend.application.service.AuthenticationService;
import com.backend.application.service.RecaptchaService;
import com.backend.domain.exception.OtpRateLimitExceededException;
import com.backend.infrastructure.security.JwtProperties;
import com.backend.presentation.config.TestSecurityConfig;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ TestSecurityConfig.class })
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private org.springframework.context.MessageSource messageSource;

    @MockBean
    private RecaptchaService recaptchaService;

    @MockBean
    private JwtProperties jwtProperties;

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
    @DisplayName("POST /auth/login should return 429 when OTP resend cooldown is active")
    void login_ShouldReturn429WhenOtpRateLimitExceeded() throws Exception {
        when(authenticationService.authenticate(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(Boolean.class)))
                .thenThrow(new OtpRateLimitExceededException("Too many OTP requests", 120));
        when(messageSource.getMessage(
                eq("auth.otp.rate.limit.exceeded"),
                any(),
                any()))
                .thenReturn("Please wait before requesting another code.");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Accept-Language", "en")
                        .content("""
                                {
                                  "email": "admin@acme.test",
                                  "password": "Secret12!"
                                }
                                """))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.message").value("Please wait before requesting another code."))
                .andExpect(jsonPath("$.data.retryAfterSeconds").value(120))
                .andExpect(jsonPath("$.data.errorCode").value("OTP_RATE_LIMIT_EXCEEDED"));
    }
}
