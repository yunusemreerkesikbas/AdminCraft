export interface Site {
    id: number;
    siteName: string;
    description?: string;
    enabledLanguages: Language[];
    defaultLanguage: Language;
    tenantId: number;
    tenantName?: string;
    domain?: string;
    isActive: boolean;
    theme?: string;
    logoUrl?: string;
    faviconUrl?: string;
    createdAt: string;
    updatedAt?: string;
    publishedAt?: string;
}

export interface SitePagination {
    length: number;
    size: number;
    page: number;
    lastPage: number;
    startIndex: number;
    endIndex: number;
}

export enum Language {
    TR = 'TR',
    EN = 'EN'
}

export interface Menu {
    id: number;
    name: string;
    language: Language;
    tenantId: number;
    siteId: number;
    items: MenuItem[];
    createdAt: string;
    updatedAt?: string;
}

export interface MenuItem {
    id: number;
    title: string;
    url: string;
    order: number;
    parentId?: number;
    isActive: boolean;
    target?: string; // _blank, _self, etc.
    children?: MenuItem[];
}

export interface CreateSiteRequest {
    siteName: string;
    description?: string;
    enabledLanguages: Language[];
    defaultLanguage: Language;
    tenantId?: number;
    domain?: string;
    theme?: string;
    logoUrl?: string;
    faviconUrl?: string;
}

export interface UpdateSiteRequest {
    siteName?: string;
    description?: string;
    enabledLanguages?: Language[];
    defaultLanguage?: Language;
    domain?: string;
    isActive?: boolean;
    theme?: string;
    logoUrl?: string;
    faviconUrl?: string;
}