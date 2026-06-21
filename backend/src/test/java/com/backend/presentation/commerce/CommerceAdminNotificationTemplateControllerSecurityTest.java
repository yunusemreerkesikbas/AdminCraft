package com.backend.presentation.commerce;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.backend.application.commerce.CommerceNotificationTemplateAdminService;
import com.backend.presentation.config.TestSecurityConfig;

@WebMvcTest(CommerceAdminNotificationTemplateController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ TestSecurityConfig.class, com.backend.shared.common.GlobalExceptionHandler.class })
class CommerceAdminNotificationTemplateControllerSecurityTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CommerceNotificationTemplateAdminService templateAdminService;

	@MockBean
	private com.backend.infrastructure.security.JwtAuthenticationFilter jwtAuthenticationFilter;

	@MockBean
	private com.backend.infrastructure.security.CommerceCustomerAuthenticationFilter commerceCustomerAuthenticationFilter;

	@MockBean
	private com.backend.infrastructure.tenant.TenantFilter tenantFilter;

	@MockBean
	private com.backend.presentation.filter.CmsPreviewFilter cmsPreviewFilter;

	@Test
	@DisplayName("GET /commerce/admin/notifications/templates should allow TENANT_ADMIN")
	@WithMockUser(roles = "TENANT_ADMIN")
	void list_ShouldAllowTenantAdmin() throws Exception {
		mockMvc.perform(get("/commerce/admin/notifications/templates"))
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("GET /commerce/admin/notifications/templates should reject VIEWER")
	@WithMockUser(roles = "VIEWER")
	void list_ShouldRejectViewer() throws Exception {
		mockMvc.perform(get("/commerce/admin/notifications/templates"))
				.andExpect(status().isForbidden());
	}

	@Test
	@DisplayName("PUT /commerce/admin/notifications/templates/{templateUid} should allow TENANT_ADMIN")
	@WithMockUser(roles = "TENANT_ADMIN")
	void update_ShouldAllowTenantAdmin() throws Exception {
		mockMvc.perform(put("/commerce/admin/notifications/templates/tpl-uid")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"subject":"Subject","content":"Content","active":true}
						"""))
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("PUT /commerce/admin/notifications/templates/{templateUid} should reject invalid payload")
	@WithMockUser(roles = "TENANT_ADMIN")
	void update_ShouldRejectInvalidPayload() throws Exception {
		mockMvc.perform(put("/commerce/admin/notifications/templates/tpl-uid")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"subject":"","content":"Content","active":true}
						"""))
				.andExpect(status().isBadRequest());
	}
}
