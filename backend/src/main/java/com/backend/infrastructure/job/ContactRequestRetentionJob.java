package com.backend.infrastructure.job;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.backend.domain.enums.TenantStatus;
import com.backend.domain.port.TenantContextPort;
import com.backend.domain.repository.TenantRepository;
import com.backend.infrastructure.config.AppSecurityProperties;
import com.backend.infrastructure.persistence.repository.ContactRequestJpaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Deletes {@code contact_requests} rows older than the configured retention window for each
 * active tenant database.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContactRequestRetentionJob {

    private final TenantRepository tenantRepository;
    private final TenantContextPort tenantContext;
    private final ContactRequestJpaRepository contactRequestJpaRepository;
    private final AppSecurityProperties appSecurityProperties;

    @Qualifier("tenantTransactionManager")
    private final PlatformTransactionManager tenantTransactionManager;

    @Scheduled(cron = "0 45 2 * * *")
    public void purgeExpiredContactRequests() {
        if (!appSecurityProperties.isContactRequestRetentionJobEnabled()) {
            return;
        }
        int retentionDays = Math.max(1, appSecurityProperties.getContactRequestRetentionDays());
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        TransactionTemplate tx = new TransactionTemplate(tenantTransactionManager);

        for (var tenant : tenantRepository.findByStatus(TenantStatus.ACTIVE)) {
            try {
                tenantContext.setTenantId(String.valueOf(tenant.getId()));
                tenantContext.setTenantDbName(tenant.getDatabaseName());
                int deleted = Optional
                        .ofNullable(tx.execute(status -> contactRequestJpaRepository.deleteByCreatedAtBefore(cutoff)))
                        .orElse(0);
                if (deleted > 0) {
                    log.info("Contact request retention: deleted {} row(s) for tenantId={}", deleted, tenant.getId());
                }
            } catch (Exception e) {
                log.warn("Contact request retention failed for tenantId={}: {}", tenant.getId(), e.getMessage());
            } finally {
                tenantContext.clear();
            }
        }
    }
}
