package com.backend.application.service;

import com.backend.application.dto.provisioning.ProvisionRequest;
import com.backend.application.dto.provisioning.ProvisioningJobResponse;
import com.backend.application.dto.provisioning.SyncMigrationsRequest;

public interface ProvisioningService {

    ProvisioningJobResponse provisionTenant(Long tenantId, ProvisionRequest request);

    ProvisioningJobResponse syncTenantMigrations(Long tenantId, SyncMigrationsRequest request);

    ProvisioningJobResponse getJobStatus(Long jobId);
}
