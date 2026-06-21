package com.backend.infrastructure.job;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.backend.application.commerce.CommerceNotificationOutboxAdminService;
import com.backend.application.commerce.CommerceNotificationProperties;
import com.backend.domain.entity.Tenant;
import com.backend.domain.enums.ModuleCode;
import com.backend.domain.enums.TenantStatus;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.repository.TenantModuleRepository;
import com.backend.domain.repository.TenantRepository;

@ExtendWith(MockitoExtension.class)
class CommerceNotificationRetryJobTest {

	@Mock private TenantRepository tenantRepository;
	@Mock private TenantModuleRepository tenantModuleRepository;
	@Mock private TenantContextPort tenantContext;
	@Mock private CommerceNotificationOutboxAdminService outboxAdminService;

	private CommerceNotificationProperties properties;
	private CommerceNotificationRetryJob job;

	@BeforeEach
	void setUp() {
		properties = new CommerceNotificationProperties();
		job = new CommerceNotificationRetryJob(
				tenantRepository,
				tenantModuleRepository,
				tenantContext,
				outboxAdminService,
				properties);
	}

	@Test
	void retryDueNotifications_ShouldSkipTenantLookup_WhenJobDisabled() {
		properties.setRetryJobEnabled(false);

		job.retryDueNotifications();

		verifyNoInteractions(tenantRepository, tenantModuleRepository, tenantContext, outboxAdminService);
	}

	@Test
	void retryDueNotifications_ShouldProcessOnlyActiveCommerceEnabledTenants() {
		Tenant commerceEnabled = tenant(1L, "ac_tenant_1");
		Tenant commerceDisabled = tenant(2L, "ac_tenant_2");
		when(tenantRepository.findByStatus(TenantStatus.ACTIVE)).thenReturn(List.of(commerceEnabled, commerceDisabled));
		when(tenantModuleRepository.existsByTenantIdAndModuleCodeAndStatus(
				1L,
				ModuleCode.COMMERCE.getCode(),
				"enabled"))
				.thenReturn(true);
		when(tenantModuleRepository.existsByTenantIdAndModuleCodeAndStatus(
				2L,
				ModuleCode.COMMERCE.getCode(),
				"enabled"))
				.thenReturn(false);
		when(outboxAdminService.retryDueNotificationsForCurrentTenant()).thenReturn(2);

		job.retryDueNotifications();

		verify(tenantContext).setTenantId("1");
		verify(tenantContext).setTenantDbName("ac_tenant_1");
		verify(tenantContext, never()).setTenantId("2");
		verify(outboxAdminService).retryDueNotificationsForCurrentTenant();
		verify(tenantContext).clear();
	}

	@Test
	void retryDueNotifications_ShouldContinueAndClearContext_WhenTenantFails() {
		Tenant first = tenant(1L, "ac_tenant_1");
		Tenant second = tenant(2L, "ac_tenant_2");
		when(tenantRepository.findByStatus(TenantStatus.ACTIVE)).thenReturn(List.of(first, second));
		when(tenantModuleRepository.existsByTenantIdAndModuleCodeAndStatus(
				1L,
				ModuleCode.COMMERCE.getCode(),
				"enabled"))
				.thenReturn(true);
		when(tenantModuleRepository.existsByTenantIdAndModuleCodeAndStatus(
				2L,
				ModuleCode.COMMERCE.getCode(),
				"enabled"))
				.thenReturn(true);
		when(outboxAdminService.retryDueNotificationsForCurrentTenant())
				.thenThrow(new IllegalStateException("tenant failure"))
				.thenReturn(1);

		job.retryDueNotifications();

		verify(tenantContext).setTenantId("1");
		verify(tenantContext).setTenantDbName("ac_tenant_1");
		verify(tenantContext).setTenantId("2");
		verify(tenantContext).setTenantDbName("ac_tenant_2");
		verify(outboxAdminService, times(2)).retryDueNotificationsForCurrentTenant();
		verify(tenantContext, times(2)).clear();
	}

	private Tenant tenant(Long id, String databaseName) {
		Tenant tenant = new Tenant();
		tenant.setId(id);
		tenant.setSubdomain("tenant-" + id);
		tenant.setCompanyName("Tenant " + id);
		tenant.setDatabaseName(databaseName);
		tenant.setStatus(TenantStatus.ACTIVE);
		return tenant;
	}
}
