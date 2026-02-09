package com.backend.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.infrastructure.persistence.platform.entity.ProvisioningJob;
import com.backend.infrastructure.persistence.platform.entity.Tenant;
import com.backend.infrastructure.persistence.platform.repository.ProvisioningJobRepository;
import com.backend.infrastructure.persistence.platform.repository.TenantModuleRepository;
import com.backend.infrastructure.persistence.platform.repository.TenantPlatformRepository;
import com.backend.presentation.dto.response.PlatformDashboardResponse;
import com.backend.presentation.dto.response.PlatformDashboardResponse.ModuleDistributionDto;
import com.backend.presentation.dto.response.PlatformDashboardResponse.RecentJobDto;
import com.backend.presentation.dto.response.PlatformDashboardResponse.RecentTenantDto;
import com.backend.presentation.dto.response.PlatformDashboardResponse.SummaryStats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PlatformDashboardServiceImpl implements PlatformDashboardService {

    private final TenantPlatformRepository tenantPlatformRepository;
    private final ProvisioningJobRepository provisioningJobRepository;
    private final TenantModuleRepository tenantModuleRepository;

    @Override
    public PlatformDashboardResponse getDashboardData() {
        SummaryStats summary = buildSummaryStats();
        List<RecentTenantDto> recentTenants = buildRecentTenants();
        List<RecentJobDto> recentJobs = buildRecentJobs();
        List<ModuleDistributionDto> moduleDistribution = buildModuleDistribution();

        return new PlatformDashboardResponse(summary, recentTenants, recentJobs, moduleDistribution);
    }

    private SummaryStats buildSummaryStats() {
        long total = tenantPlatformRepository.count();
        long active = tenantPlatformRepository.countByStatus("ACTIVE");
        long pending = tenantPlatformRepository.countByStatus("PENDING");
        long suspended = tenantPlatformRepository.countByStatus("SUSPENDED");
        long totalStorageMb = tenantPlatformRepository.sumTotalStorageMb();

        return new SummaryStats(total, active, pending, suspended, totalStorageMb);
    }

    private List<RecentTenantDto> buildRecentTenants() {
        List<Tenant> tenants = tenantPlatformRepository.findTop5ByOrderByCreatedAtDesc();

        return tenants.stream()
                .map(t -> new RecentTenantDto(
                        t.getId(),
                        t.getCompanyName(),
                        t.getSubdomain(),
                        t.getStatus(),
                        t.getCreatedAt().toString()))
                .toList();
    }

    private List<RecentJobDto> buildRecentJobs() {
        List<ProvisioningJob> jobs = provisioningJobRepository.findTop5ByOrderByCreatedAtDesc();

        return jobs.stream()
                .map(job -> {
                    String subdomain = tenantPlatformRepository.findById(job.getTenantId())
                            .map(Tenant::getSubdomain)
                            .orElse("unknown");

                    return new RecentJobDto(
                            job.getId(),
                            job.getTenantId(),
                            subdomain,
                            job.getType(),
                            job.getStatus(),
                            job.getCreatedAt().toString());
                })
                .toList();
    }

    private List<ModuleDistributionDto> buildModuleDistribution() {
        List<Object[]> distribution = tenantModuleRepository.findModuleDistribution();

        log.info("=== Module Distribution Debug ===");
        distribution.forEach(row -> {
            log.info("Module: {} - Name: {} - Count: {}", row[0], row[1], row[2]);
        });

        return distribution.stream()
                .map(row -> new ModuleDistributionDto(
                        (String) row[0],
                        (String) row[1],
                        (Long) row[2]))
                .toList();
    }
}
