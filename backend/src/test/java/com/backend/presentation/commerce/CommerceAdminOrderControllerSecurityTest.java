package com.backend.presentation.commerce;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
import org.springframework.http.MediaType;

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
	private com.backend.application.commerce.CommerceOrderResolutionRequestService resolutionRequestService;

	@MockBean
	private com.backend.application.commerce.CommerceNotificationOutboxAdminService notificationOutboxAdminService;

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

	@Test
	@DisplayName("PATCH /commerce/admin/orders/{orderUid}/status should allow TENANT_ADMIN")
	@WithMockUser(roles = "TENANT_ADMIN")
	void changeOrderStatus_ShouldAllowTenantAdmin() throws Exception {
		mockMvc.perform(patch("/commerce/admin/orders/order-uid/status")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"status":"PREPARING"}
						"""))
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("PATCH /commerce/admin/orders/{orderUid}/status should reject VIEWER")
	@WithMockUser(roles = "VIEWER")
	void changeOrderStatus_ShouldRejectViewer() throws Exception {
		mockMvc.perform(patch("/commerce/admin/orders/order-uid/status")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"status":"PREPARING"}
						"""))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("PATCH /commerce/admin/orders/{orderUid}/status should return 400 for unknown status")
	@WithMockUser(roles = "TENANT_ADMIN")
	void changeOrderStatus_ShouldReturnBadRequest_WhenStatusIsUnknown() throws Exception {
		mockMvc.perform(patch("/commerce/admin/orders/order-uid/status")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"status":"CANCELLED"}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.result").value("ERROR"));
	}

	private CommerceAdminDashboardResponse dashboard() {
		CommerceAdminMetricResponse emptyMetric = new CommerceAdminMetricResponse(0, BigDecimal.ZERO, "TRY");
		return new CommerceAdminDashboardResponse(emptyMetric, emptyMetric, 0, 0, 0, "TRY");
	}
}
