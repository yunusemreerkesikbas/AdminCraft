import { Language } from '@modules/admin/custom/tenants/tenants.types';

export interface LanguageProvisionDialogData {
    tenantId: number;
    tenantName: string;
    newLanguages: Language[];
    jobUuid?: string;
    status?: 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED';
    processedItems?: number;
    totalItems?: number;
    errorMessage?: string;
}

export interface LanguageProvisioningJob {
    uuid: string;
    status: 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED';
    processedItems: number;
    totalItems: number;
    errorMessage?: string;
}
