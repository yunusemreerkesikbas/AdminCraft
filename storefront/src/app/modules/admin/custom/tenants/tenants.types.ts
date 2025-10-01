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
    EN = 'EN'
}

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