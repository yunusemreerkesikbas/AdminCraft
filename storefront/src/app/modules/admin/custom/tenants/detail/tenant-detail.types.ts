export type TenantDetailTab = 'overview' | 'modules' | 'jobs';

export interface TabConfig {
    id: TenantDetailTab;
    label: string;
    icon: string;
}

export const TENANT_DETAIL_TABS: TabConfig[] = [
    { id: 'overview', label: 'admin.tenants.detail.tabs.overview', icon: 'heroicons_outline:information-circle' },
    { id: 'modules', label: 'admin.tenants.detail.tabs.modules', icon: 'heroicons_outline:cube' },
    { id: 'jobs', label: 'admin.tenants.detail.tabs.jobs', icon: 'heroicons_outline:queue-list' },
];
