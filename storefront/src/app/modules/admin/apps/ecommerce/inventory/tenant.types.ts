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
    sslEnabled: boolean;
    timezone: string;
    currency: string;
    storageUsedMb: number;
    createdAt: string;
    updatedAt: string;
    activatedAt?: string;
    lastBackupAt?: string;
    notes?: string;
}

export interface TenantPagination {
    length: number;
    size: number;
    page: number;
    lastPage: number;
    startIndex: number;
    endIndex: number;
}

export interface CreateTenantRequest {
    subdomain: string;
    companyName: string;
    databaseName?: string;
    defaultLanguage: Language;
    supportedLanguages: Language[];
    adminEmail: string;
    adminName: string;
    phone?: string;
    customDomain?: string;
    timezone?: string;
    currency?: string;
    notes?: string;
}

export interface UpdateTenantRequest {
    companyName?: string;
    defaultLanguage?: Language;
    supportedLanguages?: Language[];
    adminEmail?: string;
    adminName?: string;
    phone?: string;
    customDomain?: string;
    sslEnabled?: boolean;
    timezone?: string;
    currency?: string;
    notes?: string;
}

export interface TenantResponse {
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
    sslEnabled: boolean;
    timezone: string;
    currency: string;
    storageUsedMb: number;
    createdAt: string;
    updatedAt: string;
    activatedAt?: string;
    lastBackupAt?: string;
    notes?: string;
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

export interface ApiResponse<T> {
    success: boolean;
    message: string;
    data: T;
    timestamp: string;
}