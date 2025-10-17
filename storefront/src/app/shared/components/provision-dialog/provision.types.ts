import { Language } from '@modules/admin/custom/tenants/tenants.types';

export interface ModuleCatalog {
    code: string;
    name: string;
    type: 'core' | 'b2b' | 'b2c';
    version: string;
    deps: string[];
    enabledByDefault: boolean;
    description: string;
}

export type ProvisioningType = 'modules' | 'languages';

export interface ProvisionDialogData {
    type: ProvisioningType;
    tenantId: number;
    tenantName: string;
    newLanguages?: Language[];
    jobUuid?: string;
    status?: 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED';
    processedItems?: number;
    totalItems?: number;
    errorMessage?: string;
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

export interface LanguageProvisioningJob {
    uuid: string;
    status: 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED';
    processedItems: number;
    totalItems: number;
    errorMessage?: string;
}
