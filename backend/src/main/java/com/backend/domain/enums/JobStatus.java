package com.backend.domain.enums;

/**
 * Enum representing the lifecycle status of a background job.
 * Jobs transition through states: PENDING → RUNNING → COMPLETED/FAILED
 */
public enum JobStatus {

    PENDING("Pending", "Job is queued and waiting to be processed"),

    RUNNING("Running", "Job is currently being processed"),

    COMPLETED("Completed", "Job has been completed successfully"),

    FAILED("Failed", "Job has failed with errors");

    private final String displayName;
    private final String description;

    JobStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED;
    }

    public boolean isInProgress() {
        return this == RUNNING;
    }
}
