package com.backend.presentation.dto.response;

import java.util.List;

public record PlatformDashboardResponse(
    SummaryStats summary,
    List<RecentTenantDto> recentTenants,
    List<RecentJobDto> recentJobs,
    List<ModuleDistributionDto> moduleDistribution
) {
    public record SummaryStats(long total, long active, long pending, long suspended, long totalStorageMb) {}
    public record RecentTenantDto(Long id, String companyName, String subdomain, String status, String createdAt) {}
    public record RecentJobDto(Long id, Long tenantId, String tenantSubdomain, String type, String status, String createdAt) {}
    public record ModuleDistributionDto(String moduleCode, String moduleName, long enabledCount) {}
}
