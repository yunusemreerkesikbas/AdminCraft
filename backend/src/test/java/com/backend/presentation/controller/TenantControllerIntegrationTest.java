package com.backend.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.backend.application.dto.response.TenantProvisioningJobData;
import com.backend.application.service.TenantService;
import com.backend.application.usecase.CreateTenantUseCase;
import com.backend.application.usecase.GenerateTenantAdminUserUseCase;
import com.backend.domain.enums.Currency;
import com.backend.domain.enums.Language;
import com.backend.domain.enums.TenantStatus;
import com.backend.presentation.config.TestSecurityConfig;
import com.backend.presentation.dto.response.LanguageResponse;
import com.backend.presentation.dto.response.TenantListResponse;
import com.backend.shared.common.SecurityHelper;

@WebMvcTest(TenantController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ TestSecurityConfig.class, com.backend.shared.common.GlobalExceptionHandler.class })
class TenantControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TenantService tenantService;

    @MockBean
    private CreateTenantUseCase createTenantUseCase;

    @MockBean
    private GenerateTenantAdminUserUseCase generateTenantAdminUserUseCase;

    @MockBean
    private SecurityHelper securityHelper;

    @MockBean
    private com.backend.infrastructure.tenant.TenantContext tenantContext;

    @MockBean
    private com.backend.infrastructure.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private com.backend.infrastructure.tenant.TenantFilter tenantFilter;

    @Test
    @DisplayName("GET /tenants should return paged tenant list with default sort")
    @WithMockUser(roles = "SUPER_ADMIN")
    void listTenants_ShouldReturnPagedResponse() throws Exception {
        TenantListResponse tenant = new TenantListResponse(
                1L,
                "acme",
                "Acme Corp",
                TenantStatus.ACTIVE,
                Language.TR,
                Set.of(LanguageResponse.from(Language.TR)),
                Currency.TRY,
                "idle",
                2,
                LocalDateTime.of(2026, 1, 1, 10, 0),
                null,
                "acme.com");
        Page<TenantListResponse> page = new PageImpl<>(List.of(tenant), PageRequest.of(0, 20), 1);
        when(tenantService.searchTenants(eq(null), eq(null), any(Pageable.class), any(Language.class)))
                .thenReturn(page);

        mockMvc.perform(get("/tenants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.sortConfig.currentSort.code").value("createdAt,desc"))
                .andExpect(jsonPath("$.data.content[0].subdomain").value("acme"));
    }

    @Test
    @DisplayName("GET /tenants should return 400 for invalid sort")
    @WithMockUser(roles = "SUPER_ADMIN")
    void listTenants_ShouldReturnBadRequestForInvalidSort() throws Exception {
        mockMvc.perform(get("/tenants")
                .param("sort", "invalidField,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.message").value("tenant sort invalid"));
    }

    @Test
    @DisplayName("GET /tenants should pass search and status filters to service")
    @WithMockUser(roles = "SUPER_ADMIN")
    void listTenants_ShouldPassFiltersToService() throws Exception {
        when(tenantService.searchTenants(eq("acme"), eq(TenantStatus.ACTIVE), any(Pageable.class), any(Language.class)))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/tenants")
                .param("search", "acme")
                .param("status", "ACTIVE")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk());

        verify(tenantService).searchTenants(eq("acme"), eq(TenantStatus.ACTIVE), any(Pageable.class), any(Language.class));
    }

    @Test
    @DisplayName("GET /tenants/{tenantId}/provisioning-jobs should return jobs")
    @WithMockUser(roles = "SUPER_ADMIN")
    void getTenantProvisioningJobs_ShouldReturnSuccess() throws Exception {
        when(tenantService.getTenantProvisioningJobs(1L)).thenReturn(List.of(
                new TenantProvisioningJobData(
                        10L,
                        1L,
                        "provision",
                        "succeeded",
                        100,
                        null,
                        LocalDateTime.of(2026, 1, 1, 12, 0),
                        LocalDateTime.of(2026, 1, 1, 12, 1),
                        LocalDateTime.of(2026, 1, 1, 12, 5))));

        mockMvc.perform(get("/tenants/1/provisioning-jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].id").value(10))
                .andExpect(jsonPath("$.data[0].status").value("succeeded"));
    }

    @Test
    @DisplayName("GET /tenants should return 403 for non-super-admin")
    @WithMockUser(roles = "TENANT_ADMIN")
    void listTenants_ShouldReturnForbiddenForTenantAdmin() throws Exception {
        mockMvc.perform(get("/tenants"))
                .andExpect(status().isForbidden());
    }
}
