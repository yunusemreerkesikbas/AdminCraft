// Re-export existing types for compatibility
export {
    AddressDto,
    AddressLocalizedDto,
    BusinessHoursDto,
    CreateSiteRequest,
    DayHoursDto,
    Language,
    RobotsMetaTag,
    SeoDefaultsDto,
    Site,
    SitePagination,
    SiteSettingsGlobalDto,
    SiteSettingsI18nDto,
    SiteSettingsPatchRequest,
    SiteSettingsResponseDto,
    SocialLinksDto,
    UpdateSiteRequest
} from '../sites/sites.types';

// ========== Site Overview Types ==========

export interface SiteOverviewResponse {
    id: number;
    status: SiteStatusDto;
    stats: SiteStatsDto;
    recentActivity: ActivityDto[];
    actions: ActionsDto;
}

export interface SiteStatusDto {
    state: 'published' | 'draft' | 'maintenance';
    publishedAt: string | null;
    lastUpdatedAt: string | null;
    lastUpdatedBy: UserDto | null;
}

export interface UserDto {
    id: number;
    email: string;
    displayName: string;
}

export interface SiteStatsDto {
    pages: EntityStatsDto;
    components: EntityStatsDto;
    media: MediaStatsDto;
    products: EntityStatsDto | null;
}

export interface EntityStatsDto {
    total: number;
    published: number;
    draft: number;
    weeklyChange: number;
}

export interface MediaStatsDto {
    total: number;
    totalSizeMb: number;
    dailyChange: number;
}

export interface ActivityDto {
    id: number;
    action: string;
    entityType: string;
    entityId: number;
    entityName: string;
    description: string;
    user: UserDto | null;
    createdAt: string;
}

export interface ActionsDto {
    canPublish: boolean;
    canPreview: boolean;
    canEnableMaintenance: boolean;
    canDisableMaintenance: boolean;
    previewUrl: string | null;
}

// ========== Site Technical Types ==========

export interface SiteTechnicalResponse {
    searchEngine: SearchEngineDto;
    cookieConsent: CookieConsentDto;
}

export interface SearchEngineDto {
    robotsTxt: string | null;
    sitemapEnabled: boolean;
    indexingEnabled: boolean;
}

export interface CookieConsentDto {
    enabled: boolean;
    text: string | null;
}

export interface SiteTechnicalPatchRequest {
    robotsTxt?: string | null;
    sitemapEnabled?: boolean;
    indexingEnabled?: boolean;
    cookieConsentEnabled?: boolean;
    cookieConsentText?: string | null;
}

// ========== Site Security Types ==========

export type TwoFactorPolicy = 'DISABLED' | 'REQUIRED';

export interface SecuritySettingsResponse {
    twoFactor: TwoFactorPolicyDto;
}

export interface TwoFactorPolicyDto {
    policy: TwoFactorPolicy;
    policyDescription: string;
}

export interface UpdateSecuritySettingsRequest {
    twoFactorPolicy: TwoFactorPolicy;
}

// ========== Dashboard Tab Types ==========

export type SiteDashboardTab =
    | 'overview'
    | 'settings'
    | 'seo'
    | 'advanced';

export interface TabConfig {
    id: SiteDashboardTab;
    label: string;
    icon: string;
}

export const SITE_DASHBOARD_TABS: TabConfig[] = [
    {
        id: 'overview',
        label: 'admin.site.dashboard.tabs.overview',
        icon: 'heroicons_outline:chart-pie',
    },
    {
        id: 'settings',
        label: 'admin.site.dashboard.tabs.settings',
        icon: 'heroicons_outline:cog-6-tooth',
    },
    {
        id: 'seo',
        label: 'admin.site.dashboard.tabs.seo',
        icon: 'heroicons_outline:magnifying-glass',
    },
    {
        id: 'advanced',
        label: 'admin.site.dashboard.tabs.advanced',
        icon: 'heroicons_outline:code-bracket',
    },
];
