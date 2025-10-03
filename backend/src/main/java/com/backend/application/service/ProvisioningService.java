package com.backend.application.service;

import com.backend.domain.entity.ProvisioningJob;
import com.backend.domain.enums.Language;
import com.backend.presentation.dto.response.ProvisioningJobResponse;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface ProvisioningService {
    ProvisioningJobResponse createLanguageProvisioningJob(Long tenantId, Set<Language> languages);
    CompletableFuture<Void> executeLanguageProvisioning(ProvisioningJob job);
    ProvisioningJobResponse getJobStatus(String jobUuid);
}
