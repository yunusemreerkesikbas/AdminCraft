export interface Tenant {
    id: number;
    subdomain: string;
    companyName: string;
    databaseName: string;
    status: TenantStatus;
    defaultLanguage: Language;
    supportedLanguages: LanguageResponse[];
    customDomain?: string;
    notes?: string;
    hasAdminUser: boolean;
    createdAt: string;
    updatedAt?: string;
    activatedAt?: string;
    suspendedAt?: string;
}

export enum TenantStatus {
    PENDING = 'PENDING',
    ACTIVE = 'ACTIVE',
    SUSPENDED = 'SUSPENDED',
    MAINTENANCE = 'MAINTENANCE'
}

export enum Language {
    TR = 'TR',
    EN = 'EN',
    ES = 'ES',
    RU = 'RU',
    AR = 'AR'
}

export const LANGUAGE_LABELS: Record<Language, string> = {
    [Language.TR]: 'Türkçe',
    [Language.EN]: 'English',
    [Language.ES]: 'Español',
    [Language.RU]: 'Русский',
    [Language.AR]: 'العربية'
};

export interface LanguageResponse {
    code: string;
    nativeName: string;
    englishName: string;
    rtl: boolean;
}

export interface CreateTenantRequest {
    companyName: string;
    subdomain: string;
    defaultLanguage: Language;
    supportedLanguages: Language[];
    customDomain?: string;
    notes?: string;
}

export interface UpdateTenantRequest {
    companyName?: string;
    defaultLanguage?: Language;
    supportedLanguages?: Language[];
    customDomain?: string;
    notes?: string;
}

export interface TenantLanguagesDto {
    defaultLanguage: Language;
    supportedLanguages: Language[];
}

export interface UpdateTenantLanguagesRequest {
    defaultLanguage: Language;
    supportedLanguages: Language[];
}

export interface ProvisionLanguagesRequest {
    languages: Language[];
}

export interface ProvisioningJobDto {
    uuid: string;
    tenantId: number;
    status: 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED';
    type: string;
    totalItems: number;
    processedItems: number;
    failedItems: number;
    createdAt: string;
    startedAt?: string | null;
    completedAt?: string | null;
    errorMessage?: string | null;
}

export interface TenantListResponse {
    id: number;
    subdomain: string;
    companyName: string;
    status: TenantStatus;
    defaultLanguage: Language;
    supportedLanguages: LanguageResponse[];
    hasAdminUser: boolean;

    provisioningStatus: 'idle' | 'provisioning' | 'failed';
    provisionedModulesCount: number;

    createdAt: string;
    activatedAt?: string;
    customDomain?: string;
}

export interface TenantDetailResponse extends TenantListResponse {
    databaseName: string;
    storageUsedMb?: number;
    fullDomain?: string;
    updatedAt?: string;
    lastBackupAt?: string;
    notes?: string;
}

/**
 * Response from generate admin user endpoint
 * Sprint 20: Tenant Admin User Generator
 */
export interface AdminUserResponse {
    email: string;
    temporaryPassword: string;
    subdomain: string;
    loginUrl: string;
}

/**
 * Response from sync migrations and provisioning job endpoints
 * Uses jobId (number) and lowercase status values
 */
export interface SyncJobDto {
    jobId: number;
    tenantId: number;
    type: string;
    status: 'pending' | 'running' | 'succeeded' | 'failed';
    progress: number;
    error?: string | null;
    createdAt: string;
    startedAt?: string | null;
    completedAt?: string | null;
}

