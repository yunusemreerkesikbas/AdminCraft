package com.backend.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.backend.domain.enums.ModuleCode;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.repository.TenantModuleRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("TenantModuleAccessService Tests")
class TenantModuleAccessServiceTest {

	@Mock
	private TenantContextPort tenantContext;

	@Mock
	private TenantModuleRepository tenantModuleRepository;

	@InjectMocks
	private TenantModuleAccessService tenantModuleAccessService;

	@Test
	@DisplayName("isEnabledForCurrentTenant should return false when tenant context is missing")
	void isEnabledForCurrentTenant_ShouldReturnFalse_WhenTenantContextMissing() {
		when(tenantContext.getTenantId()).thenReturn(null);

		boolean enabled = tenantModuleAccessService.isEnabledForCurrentTenant(ModuleCode.PRODUCT_CATALOG);

		assertThat(enabled).isFalse();
		verify(tenantModuleRepository, never()).existsByTenantIdAndModuleCodeAndStatus(
				org.mockito.ArgumentMatchers.anyLong(),
				org.mockito.ArgumentMatchers.anyString(),
				org.mockito.ArgumentMatchers.anyString());
	}

	@Test
	@DisplayName("isEnabledForCurrentTenant should return true for enabled module")
	void isEnabledForCurrentTenant_ShouldReturnTrue_WhenModuleEnabled() {
		when(tenantContext.getTenantId()).thenReturn("7");
		when(tenantModuleRepository.existsByTenantIdAndModuleCodeAndStatus(7L, "product", "enabled"))
				.thenReturn(true);

		boolean enabled = tenantModuleAccessService.isEnabledForCurrentTenant(ModuleCode.PRODUCT_CATALOG);

		assertThat(enabled).isTrue();
	}

	@Test
	@DisplayName("assertEnabledForCurrentTenant should fail when tenant context is missing")
	void assertEnabledForCurrentTenant_ShouldFail_WhenTenantContextMissing() {
		when(tenantContext.getTenantId()).thenReturn(" ");

		assertThatThrownBy(() -> tenantModuleAccessService.assertEnabledForCurrentTenant(
				ModuleCode.MAIL_MARKETING,
				"mail.marketing.tenant.context.required",
				"mail.marketing.module.not.enabled"))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("mail.marketing.tenant.context.required");
	}

	@Test
	@DisplayName("assertEnabledForCurrentTenant should fail when module is disabled")
	void assertEnabledForCurrentTenant_ShouldFail_WhenModuleDisabled() {
		when(tenantContext.getTenantId()).thenReturn("9");
		when(tenantModuleRepository.existsByTenantIdAndModuleCodeAndStatus(9L, "mail_marketing", "enabled"))
				.thenReturn(false);

		assertThatThrownBy(() -> tenantModuleAccessService.assertEnabledForCurrentTenant(
				ModuleCode.MAIL_MARKETING,
				"mail.marketing.tenant.context.required",
				"mail.marketing.module.not.enabled"))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("mail.marketing.module.not.enabled");
	}

	@Test
	@DisplayName("assertEnabledForCurrentTenant should pass for enabled module")
	void assertEnabledForCurrentTenant_ShouldPass_WhenModuleEnabled() {
		when(tenantContext.getTenantId()).thenReturn("9");
		when(tenantModuleRepository.existsByTenantIdAndModuleCodeAndStatus(9L, "mail_marketing", "enabled"))
				.thenReturn(true);

		tenantModuleAccessService.assertEnabledForCurrentTenant(
				ModuleCode.MAIL_MARKETING,
				"mail.marketing.tenant.context.required",
				"mail.marketing.module.not.enabled");

		verify(tenantModuleRepository).existsByTenantIdAndModuleCodeAndStatus(9L, "mail_marketing", "enabled");
	}
}
