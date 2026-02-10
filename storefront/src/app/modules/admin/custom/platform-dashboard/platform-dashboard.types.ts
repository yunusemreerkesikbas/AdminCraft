import { TenantStatus, SyncJobStatus } from '@shared/types/platform.types';

export interface PlatformDashboardResponse {
    summary: SummaryStats;
    recentTenants: RecentTenantDto[];
    recentJobs: RecentJobDto[];
    moduleDistribution: ModuleDistributionDto[];
}

export interface SummaryStats {
    total: number;
    active: number;
    pending: number;
    suspended: number;
    totalStorageMb: number;
}

export interface RecentTenantDto {
    id: number;
    companyName: string;
    subdomain: string;
    status: TenantStatus;
    createdAt: string;
}

export interface RecentJobDto {
    id: number;
    tenantId: number;
    tenantSubdomain: string;
    type: string;
    status: SyncJobStatus;
    createdAt: string;
    error?: string;
}

export interface ModuleDistributionDto {
    moduleCode: string;
    moduleName: string;
    enabledCount: number;
}
