package com.backend.domain.repository;

import com.backend.domain.entity.ProvisioningJob;

import java.util.Optional;

public interface ProvisioningJobRepository {

    ProvisioningJob save(ProvisioningJob job);

    Optional<ProvisioningJob> findById(Long id);

    void flush();
}
