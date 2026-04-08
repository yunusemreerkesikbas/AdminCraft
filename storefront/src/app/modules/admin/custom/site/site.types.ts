import { PageWithSort } from '@core/crud/api.types';

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
    spotlight: SiteOverviewSpotlightDto | null;
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

export type SiteOverviewSpotlightTone =
    | 'PRIMARY'
    | 'WARNING'
    | 'CRITICAL'
    | 'NEUTRAL';

export type SiteOverviewSpotlightStatusCode =
    | 'healthy'
    | 'attention'
    | 'critical';

export type SiteOverviewSpotlightContextId = 'status' | 'search' | 'security';

export type SiteOverviewSpotlightContextValueCode =
    | 'published'
    | 'draft'
    | 'maintenance'
    | 'indexable'
    | 'hidden'
    | 'twoFactorRequired'
    | 'twoFactorDisabled';

export type SiteOverviewSpotlightContextDetailCode =
    | 'updatedOn'
    | 'noRecentUpdates'
    | 'sitemapEnabled'
    | 'sitemapDisabled'
    | 'cookieConsentEnabled'
    | 'cookieConsentDisabled';

export type SiteOverviewSpotlightRecommendationId =
    | 'maintenance'
    | 'indexing'
    | 'sitemap'
    | 'publish-pages'
    | 'two-factor'
    | 'healthy';

export interface SiteOverviewSpotlightDto {
    operationalScore: number;
    status: SiteOverviewSpotlightStatusDto | null;
    contextCards: SiteOverviewSpotlightContextCardDto[];
    recommendations: SiteOverviewSpotlightRecommendationDto[];
}

export interface SiteOverviewSpotlightStatusDto {
    tone: SiteOverviewSpotlightTone;
    code: SiteOverviewSpotlightStatusCode;
}

export interface SiteOverviewSpotlightContextCardDto {
    id: SiteOverviewSpotlightContextId;
    icon: string;
    progress: number;
    tone: SiteOverviewSpotlightTone;
    valueCode: SiteOverviewSpotlightContextValueCode;
    detailCode: SiteOverviewSpotlightContextDetailCode;
    detailDate: string | null;
}

export interface SiteOverviewSpotlightRecommendationDto {
    id: SiteOverviewSpotlightRecommendationId;
    icon: string;
    tone: SiteOverviewSpotlightTone;
    count: number | null;
}

// ========== Site Analytics Types ==========

export type SiteAnalyticsStatus =
    | 'READY'
    | 'NOT_CONFIGURED'
    | 'DISABLED'
    | 'ACCESS_ERROR'
    | 'NO_DATA';

export interface SiteAnalyticsSummaryResponse {
    status: SiteAnalyticsStatus;
    propertyId: string | null;
    range: 'LAST_7_DAYS';
    cards: SiteAnalyticsMetricCardDto[];
    trend: SiteAnalyticsTrendPointDto[];
    lastSyncedAt: string | null;
}

export function createSiteAnalyticsSummary(
    status: SiteAnalyticsStatus
): SiteAnalyticsSummaryResponse {
    return {
        status,
        propertyId: null,
        range: 'LAST_7_DAYS',
        cards: [],
        trend: [],
        lastSyncedAt: null,
    };
}

export interface SiteAnalyticsMetricCardDto {
    metric: 'activeUsers' | 'screenPageViews' | 'newUsers' | 'engagementRate';
    value: number;
    previousValue: number | null;
    deltaPercentage: number | null;
    deltaDirection: 'up' | 'down' | 'flat' | 'unknown';
}

export interface SiteAnalyticsTrendPointDto {
    date: string;
    value: number;
}

export type SiteInsightsStatus =
    | 'READY'
    | 'NOT_CONFIGURED'
    | 'DISABLED'
    | 'ACCESS_ERROR'
    | 'NO_DATA';

export interface SiteInsightsSummaryResponse {
    resolvedUrl: string | null;
    resolvedOrigin: string | null;
    lastSyncedAt: string | null;
    seo: SiteSeoInsightsResponse;
    performance: SitePerformanceInsightsResponse;
}

export interface SiteSeoInsightsResponse {
    status: SiteInsightsStatus;
    propertyUrl: string | null;
    range: 'LAST_28_DAYS';
    cards: SiteSeoMetricCardDto[];
    trend: SiteSeoTrendPointDto[];
    inspection: SiteSeoInspectionDto | null;
    lastSyncedAt: string | null;
}

export interface SiteSeoMetricCardDto {
    metric: 'clicks' | 'impressions' | 'ctr' | 'position';
    value: number;
    previousValue: number | null;
    deltaPercentage: number | null;
    deltaDirection: 'up' | 'down' | 'flat' | 'unknown';
}

export interface SiteSeoTrendPointDto {
    date: string;
    clicks: number;
    impressions: number;
}

export interface SiteSeoInspectionDto {
    verdict: string | null;
    coverageState: string | null;
    robotsTxtState: string | null;
    indexingState: string | null;
    pageFetchState: string | null;
    lastCrawlTime: string | null;
    googleCanonical: string | null;
    userCanonical: string | null;
    sitemaps: string[];
}

export interface SitePerformanceInsightsResponse {
    status: SiteInsightsStatus;
    targetScope: 'URL' | 'ORIGIN' | null;
    target: string | null;
    formFactor: 'DESKTOP';
    score: SitePerformanceScoreDto | null;
    metrics: SitePerformanceMetricDto[];
    trend: SitePerformanceTrendPointDto[];
    lastSyncedAt: string | null;
}

export interface SitePerformanceScoreDto {
    value: number;
    label: 'HEALTHY' | 'ATTENTION' | 'CRITICAL';
}

export interface SitePerformanceMetricDto {
    metric: 'lcp' | 'inp' | 'cls' | 'ttfb';
    value: number | null;
    displayValue: string | null;
    assessment: 'GOOD' | 'NEEDS_IMPROVEMENT' | 'POOR' | 'UNKNOWN';
}

export interface SitePerformanceTrendPointDto {
    startDate: string | null;
    endDate: string | null;
    lcp: number | null;
    inp: number | null;
    cls: number | null;
    ttfb: number | null;
}

export function createSiteInsightsSummary(
    status: SiteInsightsStatus
): SiteInsightsSummaryResponse {
    return {
        resolvedUrl: null,
        resolvedOrigin: null,
        lastSyncedAt: null,
        seo: {
            status,
            propertyUrl: null,
            range: 'LAST_28_DAYS',
            cards: [],
            trend: [],
            inspection: null,
            lastSyncedAt: null,
        },
        performance: {
            status,
            targetScope: null,
            target: null,
            formFactor: 'DESKTOP',
            score: null,
            metrics: [],
            trend: [],
            lastSyncedAt: null,
        },
    };
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
    texts?: Record<string, string> | null;
}

export interface SiteTechnicalPatchRequest {
    robotsTxt?: string | null;
    sitemapEnabled?: boolean;
    indexingEnabled?: boolean;
    cookieConsentEnabled?: boolean;
    cookieConsentTexts?: Record<string, string> | null;
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

// ========== Activity Trend Types ==========

export interface SiteActivityTrendDay {
    date: string;
    total: number;
    created: number;
    updated: number;
    published: number;
}

export type SiteActivityFeedResponse = PageWithSort<ActivityDto>;
export type SiteActivityTrendResponse = PageWithSort<SiteActivityTrendDay>;

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
