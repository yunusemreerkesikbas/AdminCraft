package com.backend.application.dto.response;

import java.util.List;

public record PlatformDashboardData(
        SummaryStats summary,
        List<RecentTenantData> recentTenants,
        List<RecentJobData> recentJobs,
        List<ModuleDistributionData> moduleDistribution) {

    public record SummaryStats(long total, long active, long pending, long suspended, long totalStorageMb) {
    }

    public record RecentTenantData(Long id, String companyName, String subdomain, String status, String createdAt) {
    }

    public record RecentJobData(
            Long id,
            Long tenantId,
            String tenantSubdomain,
            String type,
            String status,
            String createdAt,
            String error) {
    }

    public record ModuleDistributionData(String moduleCode, String moduleName, long enabledCount) {
    }
}
