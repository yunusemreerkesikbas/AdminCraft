package com.backend.domain.repository;

import com.backend.domain.entity.ProvisioningJob;
import com.backend.domain.enums.JobStatus;
import com.backend.domain.enums.JobType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProvisioningJobRepository extends JpaRepository<ProvisioningJob, Long> {

    Optional<ProvisioningJob> findByUuid(String uuid);

    List<ProvisioningJob> findByTenantId(Long tenantId);

    List<ProvisioningJob> findByStatus(JobStatus status);

    List<ProvisioningJob> findByTenantIdAndStatus(Long tenantId, JobStatus status);

    List<ProvisioningJob> findByTenantIdAndJobType(Long tenantId, JobType jobType);

    @Query("SELECT j FROM ProvisioningJob j WHERE j.status = 'PENDING' ORDER BY j.createdAt ASC")
    List<ProvisioningJob> findPendingJobs();

    @Query("SELECT j FROM ProvisioningJob j WHERE j.status = 'RUNNING'")
    List<ProvisioningJob> findRunningJobs();

    @Query("SELECT j FROM ProvisioningJob j WHERE j.status = 'RUNNING' AND j.startedAt < :threshold")
    List<ProvisioningJob> findStaleRunningJobs(@Param("threshold") LocalDateTime threshold);

    @Query("SELECT j FROM ProvisioningJob j WHERE j.tenantId = :tenantId ORDER BY j.createdAt DESC")
    List<ProvisioningJob> findRecentJobsByTenant(@Param("tenantId") Long tenantId);

    @Query("SELECT j FROM ProvisioningJob j WHERE j.tenantId = :tenantId AND j.status = 'COMPLETED' ORDER BY j.completedAt DESC")
    List<ProvisioningJob> findCompletedJobsByTenant(@Param("tenantId") Long tenantId);

    @Query("SELECT j FROM ProvisioningJob j WHERE j.tenantId = :tenantId AND j.status = 'FAILED' ORDER BY j.completedAt DESC")
    List<ProvisioningJob> findFailedJobsByTenant(@Param("tenantId") Long tenantId);

    long countByTenantIdAndStatus(Long tenantId, JobStatus status);

    long countByTenantIdAndJobType(Long tenantId, JobType jobType);

    @Query("SELECT j FROM ProvisioningJob j WHERE j.tenantId = :tenantId AND j.createdAt BETWEEN :startDate AND :endDate ORDER BY j.createdAt DESC")
    List<ProvisioningJob> findByTenantAndDateRange(@Param("tenantId") Long tenantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("DELETE FROM ProvisioningJob j WHERE j.status = 'COMPLETED' AND j.completedAt < :completedBefore")
    int deleteCompletedJobsBefore(@Param("completedBefore") LocalDateTime completedBefore);
}
