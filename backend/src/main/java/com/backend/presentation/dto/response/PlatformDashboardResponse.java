package com.backend.presentation.dto.response;

import java.util.List;

import com.backend.application.dto.response.PlatformDashboardData;

public record PlatformDashboardResponse(
    SummaryStats summary,
    List<RecentTenantDto> recentTenants,
    List<RecentJobDto> recentJobs,
    List<ModuleDistributionDto> moduleDistribution
) {
    public record SummaryStats(long total, long active, long pending, long suspended, long totalStorageMb) {}
    public record RecentTenantDto(Long id, String companyName, String subdomain, String status, String createdAt) {}
    public record RecentJobDto(Long id, Long tenantId, String tenantSubdomain, String type, String status, String createdAt, String error) {}
    public record ModuleDistributionDto(String moduleCode, String moduleName, long enabledCount) {}

    public static PlatformDashboardResponse from(PlatformDashboardData data) {
        return new PlatformDashboardResponse(
                new SummaryStats(
                        data.summary().total(),
                        data.summary().active(),
                        data.summary().pending(),
                        data.summary().suspended(),
                        data.summary().totalStorageMb()),
                data.recentTenants().stream()
                        .map(t -> new RecentTenantDto(
                                t.id(),
                                t.companyName(),
                                t.subdomain(),
                                t.status(),
                                t.createdAt()))
                        .toList(),
                data.recentJobs().stream()
                        .map(j -> new RecentJobDto(
                                j.id(),
                                j.tenantId(),
                                j.tenantSubdomain(),
                                j.type(),
                                j.status(),
                                j.createdAt(),
                                j.error()))
                        .toList(),
                data.moduleDistribution().stream()
                        .map(m -> new ModuleDistributionDto(
                                m.moduleCode(),
                                m.moduleName(),
                                m.enabledCount()))
                        .toList());
    }
}
