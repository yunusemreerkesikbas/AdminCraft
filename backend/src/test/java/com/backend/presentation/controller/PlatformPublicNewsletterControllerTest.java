package com.backend.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Locale;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.backend.application.dto.platform.PlatformPublicNewsletterSubscribeCommand;
import com.backend.application.service.PlatformMailMarketingService;
import com.backend.presentation.config.TestSecurityConfig;

@WebMvcTest(PlatformPublicNewsletterController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ TestSecurityConfig.class, com.backend.shared.common.GlobalExceptionHandler.class })
class PlatformPublicNewsletterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlatformMailMarketingService service;

    @MockBean
    private MessageSource messageSource;

    @MockBean
    private com.backend.infrastructure.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private com.backend.infrastructure.tenant.TenantFilter tenantFilter;

    @Test
    @DisplayName("POST /platform/public/newsletter/subscribe should accept valid body")
    void subscribe_ShouldReturnSuccess() throws Exception {
        when(service.subscribe(any(PlatformPublicNewsletterSubscribeCommand.class)))
                .thenReturn("mail.marketing.newsletter.subscribe.sent");
        when(messageSource.getMessage(any(String.class), any(), any(String.class), any(Locale.class)))
                .thenAnswer((Answer<String>) invocation -> {
                    String code = invocation.getArgument(0);
                    if ("mail.marketing.newsletter.subscribe.sent".equals(code)) {
                        return "Subscription confirmation email sent";
                    }
                    return code;
                });

        mockMvc.perform(post("/platform/public/newsletter/subscribe")
                .header("Accept-Language", "en")
                .contentType(APPLICATION_JSON)
                .content("""
                        {"email":"t@example.com","source":"LANDING_NEWSLETTER","templateType":"NEWSLETTER_DEFAULT","locale":"en","honeypot":"","formStartedAt":1700000000000}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Subscription confirmation email sent"));
    }

    @Test
    @DisplayName("POST /platform/public/newsletter/subscribe should return 400 when anti-bot validation fails")
    void subscribe_ShouldReturnBadRequest_WhenAntiBotValidationFails() throws Exception {
        doThrow(new IllegalArgumentException("mail.marketing.newsletter.subscribe.blocked"))
                .when(service)
                .subscribe(any(PlatformPublicNewsletterSubscribeCommand.class));
        when(messageSource.getMessage(any(String.class), any(), any(String.class), any(Locale.class)))
                .thenReturn("Suspicious newsletter signup blocked");

        mockMvc.perform(post("/platform/public/newsletter/subscribe")
                .contentType(APPLICATION_JSON)
                .content("""
                        {"email":"t@example.com","source":"LANDING_NEWSLETTER","templateType":"NEWSLETTER_DEFAULT","locale":"en","honeypot":"bot","formStartedAt":1700000000000}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.message").value("Suspicious newsletter signup blocked"));
    }

    @Test
    @DisplayName("POST /platform/public/newsletter/subscribe should return resolved backend message when already active")
    void subscribe_ShouldReturnResolvedSuccessMessage_WhenAlreadyActive() throws Exception {
        when(service.subscribe(any(PlatformPublicNewsletterSubscribeCommand.class)))
                .thenReturn("mail.marketing.newsletter.subscribe.already.active");
        when(messageSource.getMessage(any(String.class), any(), any(String.class), any(Locale.class)))
                .thenAnswer((Answer<String>) invocation -> {
                    String code = invocation.getArgument(0);
                    if ("mail.marketing.newsletter.subscribe.already.active".equals(code)) {
                        return "This email address is already subscribed to the newsletter";
                    }
                    return code;
                });

        mockMvc.perform(post("/platform/public/newsletter/subscribe")
                .header("Accept-Language", "en")
                .contentType(APPLICATION_JSON)
                .content("""
                        {"email":"t@example.com","source":"LANDING_NEWSLETTER","templateType":"NEWSLETTER_DEFAULT","locale":"en","honeypot":"","formStartedAt":1700000000000}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("This email address is already subscribed to the newsletter"));
    }

    @Test
    @DisplayName("POST /platform/public/newsletter/subscribe should return localized backend error when mail send fails")
    void subscribe_ShouldReturnLocalizedError_WhenMailSendFails() throws Exception {
        doThrow(new IllegalStateException("mail.marketing.newsletter.confirm.send.failed"))
                .when(service)
                .subscribe(any(PlatformPublicNewsletterSubscribeCommand.class));
        when(messageSource.getMessage(any(String.class), any(), any(String.class), any(Locale.class)))
                .thenAnswer((Answer<String>) invocation -> {
                    String code = invocation.getArgument(0);
                    if ("mail.marketing.newsletter.confirm.send.failed".equals(code)) {
                        return "Subscription confirmation email could not be sent. Please try again.";
                    }
                    return code;
                });

        mockMvc.perform(post("/platform/public/newsletter/subscribe")
                .header("Accept-Language", "en")
                .contentType(APPLICATION_JSON)
                .content("""
                        {"email":"t@example.com","source":"LANDING_NEWSLETTER","templateType":"NEWSLETTER_DEFAULT","locale":"en","honeypot":"","formStartedAt":1700000000000}
                        """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.message").value("Subscription confirmation email could not be sent. Please try again."));
    }
}
