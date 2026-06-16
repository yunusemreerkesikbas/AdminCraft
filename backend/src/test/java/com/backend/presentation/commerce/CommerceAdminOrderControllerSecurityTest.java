package com.backend.presentation.commerce;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.backend.application.commerce.CommerceAdminOrderService;
import com.backend.application.commerce.dto.CommerceAdminDashboardResponse;
import com.backend.application.commerce.dto.CommerceAdminMetricResponse;
import com.backend.presentation.config.TestSecurityConfig;

@WebMvcTest(CommerceAdminOrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ TestSecurityConfig.class, com.backend.shared.common.GlobalExceptionHandler.class })
class CommerceAdminOrderControllerSecurityTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CommerceAdminOrderService adminOrderService;

	@MockBean
	private com.backend.infrastructure.security.JwtAuthenticationFilter jwtAuthenticationFilter;

	@MockBean
	private com.backend.infrastructure.security.CommerceCustomerAuthenticationFilter commerceCustomerAuthenticationFilter;

	@MockBean
	private com.backend.infrastructure.tenant.TenantFilter tenantFilter;

	@MockBean
	private com.backend.presentation.filter.CmsPreviewFilter cmsPreviewFilter;

	@Test
	@DisplayName("GET /commerce/admin/dashboard should allow TENANT_ADMIN")
	@WithMockUser(roles = "TENANT_ADMIN")
	void dashboard_ShouldAllowTenantAdmin() throws Exception {
		when(adminOrderService.dashboard()).thenReturn(dashboard());

		mockMvc.perform(get("/commerce/admin/dashboard"))
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("GET /commerce/admin/dashboard should reject VIEWER")
	@WithMockUser(roles = "VIEWER")
	void dashboard_ShouldRejectViewer() throws Exception {
		mockMvc.perform(get("/commerce/admin/dashboard"))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("GET /commerce/admin/dashboard should reject commerce customer role")
	@WithMockUser(roles = "COMMERCE_CUSTOMER")
	void dashboard_ShouldRejectCommerceCustomer() throws Exception {
		mockMvc.perform(get("/commerce/admin/dashboard"))
				.andExpect(status().isForbidden());
	}

	private CommerceAdminDashboardResponse dashboard() {
		CommerceAdminMetricResponse emptyMetric = new CommerceAdminMetricResponse(0, BigDecimal.ZERO, "TRY");
		return new CommerceAdminDashboardResponse(emptyMetric, emptyMetric, 0, 0, "TRY");
	}
}
