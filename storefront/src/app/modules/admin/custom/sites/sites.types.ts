import { Language } from '@shared/types/platform.types';

export { Language };

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

// ----- Site Settings (Sprint 9)

export interface SiteSettingsGlobalDto {
    contactEmail?: string | null;
    contactPhone?: string | null;
    whatsappPhone?: string | null;
    address?: AddressDto | null;
    businessHours?: BusinessHoursDto | null;
    social?: SocialLinksDto | null;
    canonicalBaseUrl?: string | null;
    robots?: RobotsMetaTag | null;
    logoMediaUid?: string | null;
    logoDarkMediaUid?: string | null;
    logoMedia?: SiteMediaSummaryDto | null;
    logoDarkMedia?: SiteMediaSummaryDto | null;
}

export interface SiteMediaSummaryDto {
    id: number;
    uid: string;
    fileName?: string | null;
    originalName?: string | null;
    mimeType?: string | null;
    publicUrl?: string | null;
    width?: number | null;
    height?: number | null;
    fileSizeFormatted?: string | null;
}

export enum RobotsMetaTag {
    INDEX_FOLLOW = 'index,follow',
    NOINDEX_NOFOLLOW = 'noindex,nofollow',
    INDEX_NOFOLLOW = 'index,nofollow',
    NOINDEX_FOLLOW = 'noindex,follow',
}

export interface SiteSettingsI18nDto {
    siteName?: string | null;
    tagline?: string | null;
    seo?: SeoDefaultsDto | null;
    footerText?: string | null;
    headerTopbarText?: string | null;
    addressLocalized?: AddressLocalizedDto | null;
}

export interface SiteSettingsResponseDto {
    global: SiteSettingsGlobalDto;
    languages: Record<string, SiteSettingsI18nDto>;
}

export interface AddressDto {
    line1: string;
    line2?: string | null;
    city: string;
    state?: string | null;
    postalCode: string;
    country: string;
    geo?: { lat: number; lng: number } | null;
    mapEmbedUrl?: string | null;
}

export interface AddressLocalizedDto {
    line1?: string | null;
    line2?: string | null;
    city?: string | null;
    state?: string | null;
    postalCode?: string | null;
    country?: string | null;
}

export interface BusinessHoursDto {
    monday?: DayHoursDto | null;
    tuesday?: DayHoursDto | null;
    wednesday?: DayHoursDto | null;
    thursday?: DayHoursDto | null;
    friday?: DayHoursDto | null;
    saturday?: DayHoursDto | null;
    sunday?: DayHoursDto | null;
}

export interface DayHoursDto {
    open: string; // HH:mm
    close: string; // HH:mm
    closed?: boolean;
}

export interface SocialLinksDto {
    facebook?: string | null;
    instagram?: string | null;
    x?: string | null;
    linkedin?: string | null;
    youtube?: string | null;
    tiktok?: string | null;
}

export interface SeoDefaultsDto {
    title?: string | null;
    description?: string | null;
    keywords?: string[] | null;
    ogTitle?: string | null;
    ogDescription?: string | null;
    ogImageMediaId?: number | null;
    twitterCard?: string | null;
}

export interface SiteSettingsPatchRequest {
    global?: Partial<SiteSettingsGlobalDto>;
    languages?: Record<string, Partial<SiteSettingsI18nDto>>;
}
