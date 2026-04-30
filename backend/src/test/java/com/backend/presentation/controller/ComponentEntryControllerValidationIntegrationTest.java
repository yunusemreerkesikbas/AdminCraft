package com.backend.presentation.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import com.backend.application.service.ComponentEntryI18nService;
import com.backend.application.service.ComponentEntryService;
import com.backend.infrastructure.tenant.TenantContext;
import com.backend.presentation.config.TestSecurityConfig;

@WebMvcTest(ComponentEntryController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({
        TestSecurityConfig.class,
        com.backend.shared.common.GlobalExceptionHandler.class,
        com.backend.infrastructure.config.InternationalizationConfig.class
})
class ComponentEntryControllerValidationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ComponentEntryService entryService;

    @MockBean
    private ComponentEntryI18nService entryI18nService;

    @MockBean
    private com.backend.infrastructure.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private com.backend.infrastructure.tenant.TenantFilter tenantFilter;

    @MockBean
    private TenantContext tenantContext;

    @BeforeEach
    void setUp() {
        Map<String, Object> details = new HashMap<>();
        details.put("userId", 1L);
        details.put("tenantId", 1L);
        details.put("role", "TENANT_ADMIN");

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "test@example.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_TENANT_ADMIN")));
        authentication.setDetails(details);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should return Turkish localized validation message for empty entry translations on create")
    void createComposite_ShouldReturnTurkishLocalizedValidationMessage() throws Exception {
        mockMvc.perform(post("/components/entries/composite")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Accept-Language", "tr")
                .content("""
                        {
                          "componentId": 42,
                          "translations": {}
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.message").value("En az bir çeviri zorunludur"))
                .andExpect(jsonPath("$.data.translations").value("En az bir çeviri zorunludur"));
    }

    @Test
    @DisplayName("Should return English localized validation message for empty entry translations on update")
    void updateComposite_ShouldReturnEnglishLocalizedValidationMessage() throws Exception {
        mockMvc.perform(put("/components/entries/{id}/composite", 42L)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Accept-Language", "en")
                .content("""
                        {
                          "translations": {}
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.message").value("At least one translation is required"))
                .andExpect(jsonPath("$.data.translations").value("At least one translation is required"));
    }
}
