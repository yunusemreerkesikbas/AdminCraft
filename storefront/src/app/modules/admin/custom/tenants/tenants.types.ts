export interface Tenant {
    id: number;
    subdomain: string;
    companyName: string;
    databaseName: string;
    status: TenantStatus;
    defaultLanguage: Language;
    supportedLanguages: Language[];
    adminEmail: string;
    adminName: string;
    phone?: string;
    customDomain?: string;
    timezone?: string;
    currency?: string;
    sslEnabled?: boolean;
    notes?: string;
    createdAt: string;
    updatedAt?: string;
    activatedAt?: string;
    suspendedAt?: string;
}

export interface TenantPagination {
    length: number;
    size: number;
    page: number;
    lastPage: number;
    startIndex: number;
    endIndex: number;
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

export interface CreateTenantRequest {
    companyName: string;
    subdomain: string;
    adminName: string;
    adminEmail: string;
    phone?: string;
    defaultLanguage: Language;
    supportedLanguages: Language[];
    customDomain?: string;
    timezone?: string;
    currency?: string;
    sslEnabled?: boolean;
    notes?: string;
}

export interface UpdateTenantRequest {
    companyName?: string;
    subdomain?: string;
    adminName?: string;
    adminEmail?: string;
    phone?: string;
    defaultLanguage?: Language;
    supportedLanguages?: Language[];
    customDomain?: string;
    timezone?: string;
    currency?: string;
    sslEnabled?: boolean;
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