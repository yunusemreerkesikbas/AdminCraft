package com.backend.application.service;

import java.util.List;

public interface MediaMigrationService {

    enum MigrationState {
        IDLE, RUNNING, COMPLETED, PARTIAL_FAILURE
    }

    record MigrationStatus(
        String tenantSubdomain,
        int total,
        int migrated,
        int failed,
        List<String> failedFileNames,
        MigrationState state
    ) {}

    void startMigration();

    MigrationStatus getMigrationStatus();
}
