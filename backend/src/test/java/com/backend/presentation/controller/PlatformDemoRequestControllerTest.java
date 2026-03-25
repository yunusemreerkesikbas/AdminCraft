package com.backend.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.backend.application.dto.platform.PlatformDemoRequestAdminDto;
import com.backend.application.service.PlatformDemoRequestService;
import com.backend.presentation.config.TestSecurityConfig;

@WebMvcTest(PlatformDemoRequestController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ TestSecurityConfig.class, com.backend.shared.common.GlobalExceptionHandler.class })
class PlatformDemoRequestControllerTest {

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
    @DisplayName("GET /platform/demo-requests should return paged data for SUPER_ADMIN")
    @WithMockUser(roles = "SUPER_ADMIN")
    void list_ShouldReturnSuccess() throws Exception {
        var dto = new PlatformDemoRequestAdminDto(
                1L,
                "uid1",
                "Jane",
                "j@example.com",
                "+905551234567",
                "Hi",
                "Hi",
                "en",
                "landing",
                "127.0.0.1",
                "Mozilla",
                LocalDateTime.parse("2026-01-01T12:00:00"));
        var page = new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);
        when(platformDemoRequestService.getPage(any(), isNull())).thenReturn(page);
        when(messageSource.getMessage(any(String.class), any(), any(String.class), any(Locale.class)))
                .thenReturn("fetched");

        mockMvc.perform(get("/platform/demo-requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content[0].email").value("j@example.com"));
    }

    @Test
    @DisplayName("GET /platform/demo-requests should return 403 for TENANT_ADMIN")
    @WithMockUser(roles = "TENANT_ADMIN")
    void list_ShouldRejectForTenantAdmin() throws Exception {
        mockMvc.perform(get("/platform/demo-requests"))
                .andExpect(status().isForbidden());
    }
}
