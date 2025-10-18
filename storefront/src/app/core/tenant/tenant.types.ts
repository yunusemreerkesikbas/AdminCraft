export interface TenantModule {
    id?: number;
    moduleCode: string;
    moduleName?: string;
    status: 'enabled' | 'disabled' | 'pending';
    installedAt?: string;
}
