package com.backend.application.service;

import com.backend.application.dto.provisioning.ProvisionRequest;
import com.backend.application.dto.provisioning.ProvisioningJobResponse;

public interface ProvisioningService {

    ProvisioningJobResponse provisionTenant(Long tenantId, ProvisionRequest request);

    ProvisioningJobResponse getJobStatus(Long jobId);
}
