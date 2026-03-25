package com.backend.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.mockito.stubbing.Answer;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Locale;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.backend.application.dto.platform.PlatformDemoRequestSubmitCommand;
import com.backend.application.service.PlatformDemoRequestService;
import com.backend.domain.exception.RecaptchaVerificationException;
import com.backend.presentation.config.TestSecurityConfig;

@WebMvcTest(PlatformPublicDemoRequestController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ TestSecurityConfig.class, com.backend.shared.common.GlobalExceptionHandler.class })
class PlatformPublicDemoRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlatformDemoRequestService platformDemoRequestService;

    @MockBean
    private MessageSource messageSource;

    @MockBean
    private com.backend.infrastructure.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private com.backend.infrastructure.tenant.TenantFilter tenantFilter;

    @Test
    @DisplayName("POST /platform/public/demo-requests should accept valid body")
    void submit_ShouldReturnSuccess() throws Exception {
        doNothing().when(platformDemoRequestService).submit(any(PlatformDemoRequestSubmitCommand.class));
        when(messageSource.getMessage(any(String.class), any(), any(String.class), any(Locale.class)))
                .thenAnswer((Answer<String>) invocation -> {
                    String code = invocation.getArgument(0);
                    if ("platform.demo.request.submitted.note".equals(code)) {
                        return "Spam klasörünü kontrol edin";
                    }
                    return "Talebiniz alındı";
                });

        mockMvc.perform(post("/platform/public/demo-requests")
                .header("Accept-Language", "tr")
                .contentType(APPLICATION_JSON)
                .content("""
                        {"fullName":"Test User","email":"t@example.com","phone":"","message":"Hello","locale":"en","recaptchaToken":""}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Talebiniz alındı"))
                .andExpect(jsonPath("$.data.followUpNote").value("Spam klasörünü kontrol edin"));
    }

    @Test
    @DisplayName("POST /platform/public/demo-requests should return 400 when reCAPTCHA fails")
    void submit_ShouldReturnBadRequestOnRecaptcha() throws Exception {
        doThrow(new RecaptchaVerificationException("fail")).when(platformDemoRequestService)
                .submit(any(PlatformDemoRequestSubmitCommand.class));
        when(messageSource.getMessage(any(String.class), any(), any(String.class), any(Locale.class)))
                .thenReturn("recaptcha failed");

        mockMvc.perform(post("/platform/public/demo-requests")
                .contentType(APPLICATION_JSON)
                .content("""
                        {"fullName":"Test User","email":"t@example.com","phone":"","message":"Hello","locale":"en","recaptchaToken":"x"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("recaptcha failed"));
    }
}
