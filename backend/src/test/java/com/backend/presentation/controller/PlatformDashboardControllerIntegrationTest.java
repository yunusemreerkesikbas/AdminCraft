package com.backend.presentation.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.backend.application.dto.response.PlatformDashboardData;
import com.backend.application.service.PlatformDashboardService;
import com.backend.presentation.config.TestSecurityConfig;

@WebMvcTest(PlatformDashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ TestSecurityConfig.class, com.backend.shared.common.GlobalExceptionHandler.class })
class PlatformDashboardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlatformDashboardService platformDashboardService;

    @MockBean
    private com.backend.infrastructure.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private com.backend.infrastructure.tenant.TenantFilter tenantFilter;

    @Test
    @DisplayName("GET /platform/dashboard should return dashboard data for SUPER_ADMIN")
    @WithMockUser(roles = "SUPER_ADMIN")
    void getDashboard_ShouldReturnSuccess() throws Exception {
        PlatformDashboardData data = new PlatformDashboardData(
                new PlatformDashboardData.SummaryStats(10, 7, 2, 1, 1024),
                List.of(new PlatformDashboardData.RecentTenantData(1L, "Acme", "acme", "ACTIVE", "2026-01-01T10:00:00")),
                List.of(new PlatformDashboardData.RecentJobData(
                        2L,
                        1L,
                        "acme",
                        "provision",
                        "succeeded",
                        "2026-01-01T11:00:00",
                        null)),
                List.of(new PlatformDashboardData.ModuleDistributionData("core", "Core", 5)));
        when(platformDashboardService.getDashboardData()).thenReturn(data);

        mockMvc.perform(get("/platform/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.summary.total").value(10))
                .andExpect(jsonPath("$.data.recentTenants[0].subdomain").value("acme"))
                .andExpect(jsonPath("$.data.moduleDistribution[0].moduleCode").value("core"));
    }

    @Test
    @DisplayName("GET /platform/dashboard should return 403 for non-super-admin")
    @WithMockUser(roles = "TENANT_ADMIN")
    void getDashboard_ShouldReturnForbiddenForTenantAdmin() throws Exception {
        mockMvc.perform(get("/platform/dashboard"))
                .andExpect(status().isForbidden());
    }
}
