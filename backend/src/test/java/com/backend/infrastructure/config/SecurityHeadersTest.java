package com.backend.infrastructure.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.infrastructure.security.JwtAuthenticationFilter;
import com.backend.infrastructure.tenant.TenantFilter;
import com.backend.application.cms.preview.CmsPreviewTicketService;
import com.backend.application.cms.preview.CmsRequestContext;
import com.backend.domain.port.TenantContextPort;

@WebMvcTest(controllers = SecurityHeadersTest.PingController.class)
@Import({ SecurityConfig.class, SecurityHeadersTest.TestCorsConfig.class })
@ActiveProfiles("dev")
class SecurityHeadersTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtAuthenticationFilter jwtAuthFilter;

    @MockBean
    private TenantFilter tenantFilter;

    @MockBean
    private CmsPreviewTicketService cmsPreviewTicketService;

    @MockBean
    private CmsRequestContext cmsRequestContext;

    @MockBean
    private TenantContextPort tenantContextPort;

    @Test
    @DisplayName("X-Frame-Options: DENY present on all responses")
    void xFrameOptions_deny() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Frame-Options", "DENY"));
    }

    @Test
    @DisplayName("X-Content-Type-Options: nosniff present")
    void xContentTypeOptions_nosniff() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    @DisplayName("Referrer-Policy: strict-origin-when-cross-origin present")
    void referrerPolicy_strictOriginWhenCrossOrigin() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"));
    }

    @Test
    @DisplayName("Content-Security-Policy present with default-src 'self'")
    void contentSecurityPolicy_present() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Security-Policy"))
                .andExpect(header().string("Content-Security-Policy",
                        org.hamcrest.Matchers.containsString("default-src 'self'")));
    }

    @Test
    @DisplayName("Strict-Transport-Security absent on dev profile (localhost HTTP)")
    void hsts_absentOnDevProfile() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Strict-Transport-Security"));
    }

    // Minimal controller so @WebMvcTest has something to load
    @RestController
    static class PingController {
        @GetMapping("/actuator/health")
        String health() {
            return "{\"status\":\"UP\"}";
        }
    }

    // Provides CorsProperties with safe defaults so SecurityConfig can build
    static class TestCorsConfig {
        @Bean
        @Primary
        CorsProperties corsProperties() {
            return new CorsProperties();
        }
    }
}
