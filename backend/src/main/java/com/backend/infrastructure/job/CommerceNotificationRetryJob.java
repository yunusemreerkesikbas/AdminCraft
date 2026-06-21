package com.backend.infrastructure.job;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.backend.application.commerce.CommerceNotificationOutboxAdminService;
import com.backend.application.commerce.CommerceNotificationProperties;
import com.backend.domain.enums.ModuleCode;
import com.backend.domain.enums.TenantStatus;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.repository.TenantModuleRepository;
import com.backend.domain.repository.TenantRepository;
import com.backend.shared.common.LogSanitizer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommerceNotificationRetryJob {

	private static final String MODULE_STATUS_ENABLED = "enabled";

	private final TenantRepository tenantRepository;
	private final TenantModuleRepository tenantModuleRepository;
	private final TenantContextPort tenantContext;
	private final CommerceNotificationOutboxAdminService outboxAdminService;
	private final CommerceNotificationProperties properties;

	@Scheduled(cron = "${app.commerce.notifications.retry-cron:0 */15 * * * *}")
	public void retryDueNotifications() {
		if (!properties.isRetryJobEnabled()) {
			return;
		}
		for (var tenant : tenantRepository.findByStatus(TenantStatus.ACTIVE)) {
			if (!tenantModuleRepository.existsByTenantIdAndModuleCodeAndStatus(
					tenant.getId(),
					ModuleCode.COMMERCE.getCode(),
					MODULE_STATUS_ENABLED)) {
				continue;
			}
			try {
				tenantContext.setTenantId(String.valueOf(tenant.getId()));
				tenantContext.setTenantDbName(tenant.getDatabaseName());
				int retried = outboxAdminService.retryDueNotificationsForCurrentTenant();
				if (retried > 0) {
					log.info("Commerce notification retry job processed {} row(s) for tenantId={}", retried, tenant.getId());
				}
			} catch (Exception ex) {
				log.warn(
						"Commerce notification retry job failed for tenantId={}: {}",
						tenant.getId(),
						LogSanitizer.sanitizeForLog(ex.getMessage()));
			} finally {
				tenantContext.clear();
			}
		}
	}
}
