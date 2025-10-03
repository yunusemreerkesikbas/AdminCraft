package com.backend.domain.entity;

import com.backend.domain.enums.JobStatus;
import com.backend.domain.enums.JobType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "provisioning_jobs", indexes = {
        @Index(name = "idx_provisioning_jobs_tenant", columnList = "tenant_id"),
        @Index(name = "idx_provisioning_jobs_status", columnList = "status"),
        @Index(name = "idx_provisioning_jobs_type", columnList = "job_type"),
        @Index(name = "idx_provisioning_jobs_created", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProvisioningJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String uuid;

    @NotNull(message = "validation.provisioning_job.tenant_id.not_null")
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @NotNull(message = "validation.provisioning_job.job_type.not_null")
    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 50)
    private JobType jobType;

    @NotNull(message = "validation.provisioning_job.status.not_null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JobStatus status = JobStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String languages;

    @PositiveOrZero(message = "validation.provisioning_job.total_items.positive_or_zero")
    @Column(name = "total_items", nullable = false)
    private Integer totalItems = 0;

    @PositiveOrZero(message = "validation.provisioning_job.processed_items.positive_or_zero")
    @Column(name = "processed_items", nullable = false)
    private Integer processedItems = 0;

    @PositiveOrZero(message = "validation.provisioning_job.failed_items.positive_or_zero")
    @Column(name = "failed_items", nullable = false)
    private Integer failedItems = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public void start() {
        this.status = JobStatus.RUNNING;
        this.startedAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = JobStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void fail(String errorMessage) {
        this.status = JobStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }

    public void incrementProcessed() {
        this.processedItems++;
    }

    public void incrementFailed() {
        this.failedItems++;
    }

    public double getProgressPercentage() {
        if (totalItems == null || totalItems == 0) {
            return 0.0;
        }
        return ((double) processedItems / totalItems) * 100.0;
    }

    public boolean isTerminal() {
        return status != null && status.isTerminal();
    }
}
