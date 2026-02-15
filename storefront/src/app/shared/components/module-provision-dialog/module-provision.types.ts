export interface ModuleCatalog {
    code: string;
    name: string;
    type: 'core' | 'b2c';
    version: string;
    deps: string[];
    enabledByDefault: boolean;
    description: string;
}

export interface ModuleProvisionDialogData {
    tenantId: number;
    tenantName: string;
}

export interface ProvisioningJob {
    jobId: number;
    tenantId: number;
    type: string;
    status: 'pending' | 'running' | 'succeeded' | 'failed';
    progress: number;
    error?: string;
    createdAt: string;
    startedAt?: string;
    completedAt?: string;
}

export interface ApiResponse<T> {
    result: 'SUCCESS' | 'ERROR';
    message?: string;
    data: T;
    code?: number;
}

export interface ProvisionRequest {
    modules: string[];
}

export interface InstalledModule {
    moduleCode: string;
    moduleName: string;
    status: 'enabled' | 'disabled';
}
