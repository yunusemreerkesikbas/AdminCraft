package com.backend.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.application.dto.response.PlatformDashboardData;
import com.backend.application.dto.response.PlatformDashboardData.ModuleDistributionData;
import com.backend.application.dto.response.PlatformDashboardData.RecentJobData;
import com.backend.application.dto.response.PlatformDashboardData.RecentTenantData;
import com.backend.application.dto.response.PlatformDashboardData.SummaryStats;
import com.backend.infrastructure.persistence.platform.entity.ProvisioningJob;
import com.backend.infrastructure.persistence.platform.entity.Tenant;
import com.backend.infrastructure.persistence.platform.repository.ProvisioningJobRepository;
import com.backend.infrastructure.persistence.platform.repository.TenantModuleRepository;
import com.backend.infrastructure.persistence.platform.repository.TenantPlatformRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlatformDashboardServiceImpl implements PlatformDashboardService {

    private final TenantPlatformRepository tenantPlatformRepository;
    private final ProvisioningJobRepository provisioningJobRepository;
    private final TenantModuleRepository tenantModuleRepository;

    @Override
    public PlatformDashboardData getDashboardData() {
        SummaryStats summary = buildSummaryStats();
        List<RecentTenantData> recentTenants = buildRecentTenants();
        List<RecentJobData> recentJobs = buildRecentJobs();
        List<ModuleDistributionData> moduleDistribution = buildModuleDistribution();

        return new PlatformDashboardData(summary, recentTenants, recentJobs, moduleDistribution);
    }

    private SummaryStats buildSummaryStats() {
        long total = tenantPlatformRepository.count();
        long active = tenantPlatformRepository.countByStatus("ACTIVE");
        long pending = tenantPlatformRepository.countByStatus("PENDING");
        long suspended = tenantPlatformRepository.countByStatus("SUSPENDED");
        long totalStorageMb = tenantPlatformRepository.sumTotalStorageMb();

        return new SummaryStats(total, active, pending, suspended, totalStorageMb);
    }

    private List<RecentTenantData> buildRecentTenants() {
        List<Tenant> tenants = tenantPlatformRepository.findTop5ByOrderByCreatedAtDesc();

        return tenants.stream()
                .map(t -> new RecentTenantData(
                        t.getId(),
                        t.getCompanyName(),
                        t.getSubdomain(),
                        t.getStatus(),
                        t.getCreatedAt().toString()))
                .toList();
    }

    private List<RecentJobData> buildRecentJobs() {
        List<ProvisioningJob> jobs = provisioningJobRepository.findTop5ByOrderByCreatedAtDesc();

        return jobs.stream()
                .map(job -> {
                    String subdomain = job.getTenant() != null
                            ? job.getTenant().getSubdomain()
                            : "unknown";

                    return new RecentJobData(
                            job.getId(),
                            job.getTenantId(),
                            subdomain,
                            job.getType(),
                            job.getStatus(),
                            job.getCreatedAt().toString(),
                            job.getError());
                })
                .toList();
    }

    private List<ModuleDistributionData> buildModuleDistribution() {
        List<Object[]> distribution = tenantModuleRepository.findModuleDistribution();

        return distribution.stream()
                .map(row -> new ModuleDistributionData(
                        (String) row[0],
                        (String) row[1],
                        ((Number) row[2]).longValue()))
                .toList();
    }
}
