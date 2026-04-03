import { DecimalPipe, NgClass } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
    ChangeDetectionStrategy,
    Component,
    DestroyRef,
    EventEmitter,
    Input,
    OnChanges,
    Output,
    ViewEncapsulation,
    inject,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { PageEvent } from '@angular/material/paginator';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { ApexOptions, NgApexchartsModule } from 'ng-apexcharts';
import { TenantModule } from '@core/tenant/tenant.types';
import { TenantDetailResponse } from '@modules/admin/custom/tenants/tenants.types';
import { NotificationService } from '@shared/notifications/notification.service';
import { SpaAdminPaginatorComponent } from '@shared/components/spa-admin-paginator/spa-admin-paginator.component';
import { ConfirmationService } from '@shared/services/confirmation.service';
import { UrlValidator } from '@shared/utils/url-validator';
import { take } from 'rxjs';
import { SiteService } from '../../site.service';
import {
    ActivityDto,
    SecuritySettingsResponse,
    createSiteAnalyticsSummary,
    createSiteInsightsSummary,
    SiteAnalyticsSummaryResponse,
    SiteActivityFeedResponse,
    SiteActivityTrendResponse,
    SiteInsightsSummaryResponse,
    SiteOverviewResponse,
    SiteSettingsResponseDto,
    SiteTechnicalResponse,
} from '../../site.types';

type OverviewMetricTone = 'primary' | 'neutral';
type OverviewStateTone = OverviewMetricTone | 'warning' | 'critical';
type OverviewDeltaTone = 'positive' | 'negative' | 'neutral';

interface OverviewMetricCard {
    id: string;
    label: string;
    value: string;
    detail: string;
    icon: string;
    numericValue: number;
    tone: OverviewMetricTone;
}

interface OverviewContextCard {
    id: string;
    label: string;
    value: string;
    detail: string;
    icon: string;
    progress: number;
    tone: OverviewStateTone;
}

interface OverviewSpotlightRecommendation {
    id: string;
    title: string;
    detail: string;
    icon: string;
    tone: OverviewStateTone;
}

interface OverviewHealthMetric {
    id: string;
    label: string;
    value: string;
    detail: string;
    progress: number;
    icon: string;
    tone: OverviewStateTone;
}

interface OverviewContentMixItem extends OverviewMetricCard {
    percentage: number;
}

interface OverviewLanguageItem {
    code: string;
    label: string;
}

interface OverviewIdentityViewModel {
    companyInitial: string;
    companyName: string;
    statusLabel: string;
    primaryDomainLabel: string;
    lastUpdatedLabel: string;
    domainTypeLabel: string;
    storageUsedMb: number;
    defaultLanguage: string | null;
    currency: string | null;
    languages: OverviewLanguageItem[];
}

interface OverviewModulesViewModel {
    enabledCount: number;
    items: TenantModule[];
}

interface OverviewAnalyticsMetricCard {
    id: string;
    label: string;
    value: string;
    detail: string;
    deltaLabel: string;
    deltaTone: OverviewDeltaTone;
    icon: string;
}

interface OverviewAnalyticsViewModel {
    status: SiteAnalyticsSummaryResponse['status'];
    cards: OverviewAnalyticsMetricCard[];
    chartOptions: ApexOptions;
    hasTrend: boolean;
    propertyId: string | null;
    lastSyncedLabel: string;
    setupUrl: string;
    stateTitle: string;
    stateDescription: string;
    stateActionLabel: string | null;
    stateTone: OverviewStateTone;
    stateIcon: string;
    isReady: boolean;
}

interface OverviewInsightStateViewModel {
    title: string;
    description: string;
    actionLabel: string | null;
    tone: OverviewStateTone;
    icon: string;
}

interface OverviewSeoInspectionItem {
    id: string;
    label: string;
    value: string;
    detail: string;
    tone: OverviewStateTone;
}

interface OverviewSeoMetricCard {
    id: string;
    label: string;
    value: string;
    detail: string;
    deltaLabel: string;
    deltaTone: OverviewDeltaTone;
    icon: string;
}

interface OverviewSeoViewModel {
    status: SiteInsightsSummaryResponse['seo']['status'];
    isReady: boolean;
    cards: OverviewSeoMetricCard[];
    chartOptions: ApexOptions;
    hasTrend: boolean;
    propertyUrl: string | null;
    lastSyncedLabel: string;
    inspectionItems: OverviewSeoInspectionItem[];
    state: OverviewInsightStateViewModel;
    setupUrl: string;
}

interface OverviewPerformanceMetricViewModel {
    id: string;
    label: string;
    value: string;
    detail: string;
    tone: OverviewStateTone;
}

interface OverviewPerformanceViewModel {
    status: SiteInsightsSummaryResponse['performance']['status'];
    isReady: boolean;
    scoreValue: number;
    scoreLabel: string;
    scoreTone: OverviewStateTone;
    scoreChartOptions: ApexOptions;
    metrics: OverviewPerformanceMetricViewModel[];
    trendOptions: ApexOptions;
    hasTrend: boolean;
    scopeLabel: string;
    targetLabel: string;
    lastSyncedLabel: string;
    state: OverviewInsightStateViewModel;
}

interface OverviewSpotlightViewModel {
    operationalScore: number;
    statusLabel: string;
    statusTone: OverviewStateTone;
    summary: string;
    contextCards: OverviewContextCard[];
    recommendations: OverviewSpotlightRecommendation[];
    chartOptions: ApexOptions;
}

interface OverviewInventoryViewModel {
    cards: OverviewMetricCard[];
    contentMixItems: OverviewContentMixItem[];
    contentMixTotal: number;
    hasCards: boolean;
    hasContentMixData: boolean;
    chartOptions: ApexOptions;
}

interface OverviewHealthViewModel {
    metrics: OverviewHealthMetric[];
}

interface OverviewTrendViewModel {
    visible: boolean;
    hasData: boolean;
    windowLabel: string;
    sliceLabel: string;
    canNavigatePrevious: boolean;
    canNavigateNext: boolean;
    chartOptions: ApexOptions;
}

interface OverviewActivityFeedViewModel {
    items: ActivityDto[];
    countLabel: string;
    hasItems: boolean;
    isScrollable: boolean;
    hasCount: boolean;
    hasPagination: boolean;
}

interface OverviewViewModel {
    heroVisible: boolean;
    identity: OverviewIdentityViewModel;
    modules: OverviewModulesViewModel;
    analytics: OverviewAnalyticsViewModel;
    spotlight: OverviewSpotlightViewModel;
    inventory: OverviewInventoryViewModel;
    health: OverviewHealthViewModel;
    seo: OverviewSeoViewModel;
    performance: OverviewPerformanceViewModel;
    trend: OverviewTrendViewModel;
    activityFeed: OverviewActivityFeedViewModel;
}

@Component({
    selector: 'spa-site-overview',
    templateUrl: './site-overview.component.html',
    styleUrls: ['./site-overview.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: true,
    imports: [
        NgClass,
        DecimalPipe,
        MatButtonModule,
        MatIconModule,
        MatTooltipModule,
        TranslocoModule,
        NgApexchartsModule,
        SpaAdminPaginatorComponent,
    ],
})
export class SpaSiteOverviewComponent implements OnChanges {
    readonly #destroyRef = inject(DestroyRef);
    readonly #confirmation = inject(ConfirmationService);
    readonly #notification = inject(NotificationService);
    readonly #siteService = inject(SiteService);
    readonly #transloco = inject(TranslocoService);

    @Input() overview: SiteOverviewResponse | null = null;
    @Input() analytics: SiteAnalyticsSummaryResponse =
        createSiteAnalyticsSummary('ACCESS_ERROR');
    @Input() insights: SiteInsightsSummaryResponse =
        createSiteInsightsSummary('ACCESS_ERROR');
    @Input() tenant: TenantDetailResponse | null = null;
    @Input() settings: SiteSettingsResponseDto | null = null;
    @Input() technical: SiteTechnicalResponse | null = null;
    @Input() security: SecuritySettingsResponse | null = null;
    @Input() modules: TenantModule[] = [];
    @Input() activityFeed: SiteActivityFeedResponse | null = null;
    @Input() activityFeedPageIndex = 0;
    @Input() activityFeedPageSize = 10;
    @Input() activityFeedPageSizeOptions: number[] = [10, 20, 50];
    @Input() activityFeedTotalElements = 0;
    @Input() trendPageIndex = 0;
    @Input() trend: SiteActivityTrendResponse | null = null;
    @Output() refresh = new EventEmitter<void>();
    @Output() activityFeedPageChange = new EventEmitter<PageEvent>();
    @Output() trendPageChange = new EventEmitter<'previous' | 'next'>();

    protected vm: OverviewViewModel = this.#createViewModel();

    constructor() {
        this.#transloco.langChanges$
            .pipe(takeUntilDestroyed(this.#destroyRef))
            .subscribe(() => this.#rebuildViewModel());
    }

    ngOnChanges(): void {
        this.#rebuildViewModel();
    }

    protected formatActivityTimestamp(timestamp: string): string {
        return this.#formatDate(timestamp, {
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
        });
    }

    protected getActivityIcon(entityType: string): string {
        switch (entityType) {
            case 'PAGE':
                return 'heroicons_outline:document-text';
            case 'COMPONENT':
                return 'heroicons_outline:cube';
            case 'MEDIA':
                return 'heroicons_outline:photo';
            case 'PRODUCT':
                return 'heroicons_outline:shopping-bag';
            case 'SITE':
            case 'SITE_SETTINGS':
                return 'heroicons_outline:cog-6-tooth';
            case 'NAVIGATION':
                return 'heroicons_outline:bars-3';
            default:
                return 'heroicons_outline:document';
        }
    }

    protected getActivityClass(action: string): string {
        switch (action) {
            case 'CREATED':
            case 'UPDATED':
            case 'PUBLISHED':
                return 'text-primary';
            case 'DELETED':
            case 'UNPUBLISHED':
                return 'text-rose-400';
            default:
                return 'text-slate-400';
        }
    }

    protected onActivityFeedPaginatorChange(event: PageEvent): void {
        this.activityFeedPageChange.emit(event);
    }

    protected onTrendPrevious(): void {
        if (!this.vm.trend.canNavigatePrevious) return;
        this.trendPageChange.emit('previous');
    }

    protected onTrendNext(): void {
        if (!this.vm.trend.canNavigateNext) return;
        this.trendPageChange.emit('next');
    }

    onPreview(): void {
        const previewUrl = this.overview?.actions?.previewUrl;
        if (!previewUrl) return;

        if (UrlValidator.isValidPreviewUrl(previewUrl)) {
            window.open(previewUrl, '_blank', 'noopener,noreferrer');
        } else {
            this.#notification.alert(
                'admin.site.dashboard.overview.actions.invalidPreviewUrl'
            );
        }
    }

    onEnableMaintenance(): void {
        if (!this.overview?.actions?.canEnableMaintenance || !this.overview?.id) {
            return;
        }

        this.#confirmation
            .confirm(
                'admin.site.dashboard.overview.confirmMaintenance.title',
                'admin.site.dashboard.overview.confirmMaintenance.message',
                'admin.site.dashboard.overview.actions.maintenance',
                'warning'
            )
            .pipe(take(1))
            .subscribe((confirmed) => {
                if (confirmed && this.overview?.id) {
                    this.#siteService
                        .enableMaintenanceMode(this.overview.id)
                        .pipe(take(1))
                        .subscribe({
                            next: (response) => {
                                this.#notification.success(response.message);
                                this.refresh.emit();
                            },
                            error: (error) => {
                                this.#notification.alert(error?.error?.message);
                            },
                        });
                }
            });
    }

    onDisableMaintenance(): void {
        if (!this.overview?.id) return;

        this.#confirmation
            .confirm(
                'admin.site.dashboard.overview.confirmDisableMaintenance.title',
                'admin.site.dashboard.overview.confirmDisableMaintenance.message',
                'admin.site.dashboard.overview.actions.disableMaintenance',
                'info'
            )
            .pipe(take(1))
            .subscribe((confirmed) => {
                if (confirmed && this.overview?.id) {
                    this.#siteService
                        .disableMaintenanceMode(this.overview.id)
                        .pipe(take(1))
                        .subscribe({
                            next: (response) => {
                                this.#notification.success(response.message);
                                this.refresh.emit();
                            },
                            error: (error) => {
                                this.#notification.alert(error?.error?.message);
                            },
                        });
                }
            });
    }

    protected openAnalyticsSetup(): void {
        window.open(this.vm.analytics.setupUrl, '_blank', 'noopener,noreferrer');
    }

    protected getContextIconClasses(tone: OverviewContextCard['tone']): string {
        switch (tone) {
            case 'primary':
                return 'bg-blue-50 text-blue-500 ring-1 ring-blue-200/60';
            case 'critical':
                return 'bg-rose-50 text-rose-500 ring-1 ring-rose-200/70';
            case 'warning':
                return 'bg-amber-50 text-amber-500 ring-1 ring-amber-200/60';
            default:
                return 'bg-slate-100 text-slate-500 ring-1 ring-slate-200/80';
        }
    }

    protected getMetricToneClasses(tone: OverviewMetricCard['tone']): string {
        return tone === 'primary'
            ? 'border-blue-100 bg-blue-50/50 shadow-[0_12px_28px_-20px_rgba(91,115,200,0.18)]'
            : 'border-slate-200 bg-white';
    }

    protected getMetricIconClasses(tone: OverviewMetricCard['tone']): string {
        return tone === 'primary'
            ? 'bg-blue-50 text-blue-500 ring-1 ring-blue-200/60'
            : 'bg-slate-100 text-slate-500 ring-1 ring-slate-200/80';
    }

    protected getAnalyticsDeltaClasses(tone: OverviewDeltaTone): string {
        switch (tone) {
            case 'positive':
                return 'bg-emerald-50 text-emerald-700';
            case 'negative':
                return 'bg-rose-50 text-rose-700';
            default:
                return 'bg-slate-100 text-slate-600';
        }
    }

    protected getSpotlightStatusClasses(tone: OverviewStateTone): string {
        switch (tone) {
            case 'primary':
                return 'bg-emerald-400/16 text-emerald-50 ring-1 ring-emerald-300/25';
            case 'critical':
                return 'bg-rose-400/16 text-rose-50 ring-1 ring-rose-300/25';
            case 'warning':
                return 'bg-amber-400/16 text-amber-50 ring-1 ring-amber-300/25';
            default:
                return 'bg-white/10 text-white/80 ring-1 ring-white/10';
        }
    }

    protected getSpotlightContextCardClasses(tone: OverviewStateTone): string {
        switch (tone) {
            case 'primary':
                return 'spa-site-overview__spotlight-context-card--primary';
            case 'critical':
                return 'spa-site-overview__spotlight-context-card--critical';
            case 'warning':
                return 'spa-site-overview__spotlight-context-card--warning';
            default:
                return 'spa-site-overview__spotlight-context-card--neutral';
        }
    }

    protected getSpotlightProgressClasses(tone: OverviewStateTone): string {
        switch (tone) {
            case 'primary':
                return 'text-sky-200';
            case 'critical':
                return 'text-rose-200';
            case 'warning':
                return 'text-amber-200';
            default:
                return 'text-white/55';
        }
    }

    protected getSpotlightRecommendationClasses(tone: OverviewStateTone): string {
        switch (tone) {
            case 'primary':
                return 'border-sky-300/18 bg-sky-400/[0.10] text-white';
            case 'critical':
                return 'border-rose-300/18 bg-rose-400/[0.12] text-white';
            case 'warning':
                return 'border-amber-300/18 bg-amber-400/[0.12] text-white';
            default:
                return 'border-white/8 bg-white/[0.06] text-white';
        }
    }

    #rebuildViewModel(): void {
        this.vm = this.#createViewModel();
    }

    #createViewModel(): OverviewViewModel {
        const inventoryCards = this.#buildInventoryCards();
        const spotlightCards = this.#buildContextCards();
        const operationalScore = this.#calculateOperationalScore(spotlightCards);
        const spotlightRecommendations =
            this.#buildSpotlightRecommendations(spotlightCards);
        const spotlightStatus = this.#buildSpotlightStatus(
            operationalScore,
            spotlightCards,
            spotlightRecommendations
        );
        const contentMixItems = this.#buildContentMixItems(inventoryCards);
        const healthMetrics = this.#buildHealthMetrics();
        const trendDays = this.#buildTrendDays();
        const enabledModules = this.modules.filter(
            (module) => module.status === 'enabled'
        );
        const activityFeedItems = this.activityFeed?.content ?? [];

        return {
            heroVisible: Boolean(
                this.tenant || this.overview || this.technical || this.security
            ),
            identity: this.#buildIdentityViewModel(),
            modules: {
                enabledCount: enabledModules.length,
                items: enabledModules,
            },
            analytics: this.#buildAnalyticsViewModel(),
            spotlight: {
                operationalScore,
                statusLabel: spotlightStatus.label,
                statusTone: spotlightStatus.tone,
                summary: spotlightStatus.summary,
                contextCards: spotlightCards,
                recommendations: spotlightRecommendations,
                chartOptions: this.#buildOperationalHealthChartOptions(
                    spotlightCards,
                    operationalScore
                ),
            },
            inventory: {
                cards: inventoryCards,
                contentMixItems,
                contentMixTotal: contentMixItems.reduce(
                    (total, item) => total + item.numericValue,
                    0
                ),
                hasCards: inventoryCards.length > 0,
                hasContentMixData: contentMixItems.some(
                    (item) => item.numericValue > 0
                ),
                chartOptions: this.#buildContentMixChartOptions(contentMixItems),
            },
            health: {
                metrics: healthMetrics,
            },
            seo: this.#buildSeoViewModel(),
            performance: this.#buildPerformanceViewModel(),
            trend: this.#buildTrendViewModel(trendDays),
            activityFeed: {
                items: activityFeedItems,
                countLabel: this.#translate(
                    'admin.site.dashboard.overview.meta.eventsCount',
                    { count: this.activityFeedTotalElements }
                ),
                hasItems: activityFeedItems.length > 0,
                isScrollable: activityFeedItems.length > 5,
                hasCount: this.activityFeedTotalElements > 0,
                hasPagination:
                    this.activityFeedTotalElements > this.activityFeedPageSize,
            },
        };
    }

    #buildAnalyticsViewModel(): OverviewAnalyticsViewModel {
        const status = this.analytics.status;
        const trend = this.#normalizeAnalyticsTrend(this.analytics.trend);
        const cards = this.#buildAnalyticsCards();

        return {
            status,
            cards,
            chartOptions: this.#buildAnalyticsTrendChartOptions(trend),
            hasTrend: trend.some((point) => point.value > 0),
            propertyId: this.analytics.propertyId,
            lastSyncedLabel: this.analytics.lastSyncedAt
                ? this.#translate(
                      'admin.site.dashboard.overview.analytics.lastSynced',
                      {
                          date: this.#formatDate(this.analytics.lastSyncedAt, {
                              month: 'short',
                              day: 'numeric',
                              hour: '2-digit',
                              minute: '2-digit',
                          }),
                      }
                  )
                : this.#translate(
                      'admin.site.dashboard.overview.analytics.last7Days'
                  ),
            setupUrl: this.#buildAnalyticsSetupUrl(),
            stateTitle: this.#buildAnalyticsStateTitle(status),
            stateDescription: this.#buildAnalyticsStateDescription(status),
            stateActionLabel:
                status === 'NOT_CONFIGURED'
                    ? this.#translate(
                          'admin.site.dashboard.overview.analytics.states.notConfigured.action'
                      )
                    : null,
            stateTone:
                status === 'ACCESS_ERROR'
                    ? 'warning'
                    : status === 'NOT_CONFIGURED'
                      ? 'primary'
                      : 'neutral',
            stateIcon: this.#buildAnalyticsStateIcon(status),
            isReady: status === 'READY',
        };
    }

    #buildAnalyticsCards(): OverviewAnalyticsMetricCard[] {
        return this.analytics.cards.map((card) => ({
            id: card.metric,
            label: this.#translate(
                `admin.site.dashboard.overview.analytics.metrics.${card.metric}.label`
            ),
            value: this.#formatAnalyticsMetricValue(
                card.metric,
                this.#sanitizeNumber(card.value)
            ),
            detail: this.#translate(
                `admin.site.dashboard.overview.analytics.metrics.${card.metric}.detail`
            ),
            deltaLabel: this.#formatAnalyticsDelta(card.deltaPercentage),
            deltaTone: this.#resolveAnalyticsDeltaTone(
                card.deltaPercentage,
                card.deltaDirection
            ),
            icon: this.#analyticsMetricIcon(card.metric),
        }));
    }

    #buildSeoViewModel(): OverviewSeoViewModel {
        const seo = this.insights.seo;
        const cards = this.#buildSeoCards();
        const inspectionItems = this.#buildSeoInspectionItems();

        return {
            status: seo.status,
            isReady: seo.status === 'READY',
            cards,
            chartOptions: this.#buildSeoTrendChartOptions(seo.trend),
            hasTrend: seo.trend.some(
                (point) => point.clicks > 0 || point.impressions > 0
            ),
            propertyUrl: seo.propertyUrl,
            lastSyncedLabel: seo.lastSyncedAt
                ? this.#translate(
                      'admin.site.dashboard.overview.insights.lastSynced',
                      {
                          date: this.#formatDate(seo.lastSyncedAt, {
                              month: 'short',
                              day: 'numeric',
                              hour: '2-digit',
                              minute: '2-digit',
                          }),
                      }
                  )
                : this.#translate(
                      'admin.site.dashboard.overview.insights.seo.last28Days'
                  ),
            inspectionItems,
            state: this.#buildSeoState(seo.status),
            setupUrl: this.#buildAnalyticsSetupUrl(),
        };
    }

    #buildSeoCards(): OverviewSeoMetricCard[] {
        return this.insights.seo.cards.map((card) => ({
            id: card.metric,
            label: this.#translate(
                `admin.site.dashboard.overview.insights.seo.metrics.${card.metric}.label`
            ),
            value: this.#formatSeoMetricValue(card.metric, card.value),
            detail: this.#translate(
                `admin.site.dashboard.overview.insights.seo.metrics.${card.metric}.detail`
            ),
            deltaLabel: this.#formatAnalyticsDelta(card.deltaPercentage),
            deltaTone: this.#resolveAnalyticsDeltaTone(
                card.deltaPercentage,
                card.deltaDirection
            ),
            icon: this.#seoMetricIcon(card.metric),
        }));
    }

    #buildSeoInspectionItems(): OverviewSeoInspectionItem[] {
        const inspection = this.insights.seo.inspection;
        if (!inspection) {
            return [];
        }

        const canonicalMismatch =
            inspection.googleCanonical &&
            inspection.userCanonical &&
            inspection.googleCanonical !== inspection.userCanonical;

        return [
            {
                id: 'verdict',
                label: this.#translate(
                    'admin.site.dashboard.overview.insights.seo.inspection.verdict'
                ),
                value: this.#humanizeEnumValue(inspection.verdict),
                detail: inspection.coverageState
                    ? this.#humanizeEnumValue(inspection.coverageState)
                    : this.#translate(
                          'admin.site.dashboard.overview.meta.notConfigured'
                      ),
                tone:
                    inspection.verdict === 'PASS'
                        ? 'primary'
                        : inspection.verdict === 'FAIL'
                          ? 'critical'
                          : 'warning',
            },
            {
                id: 'indexing',
                label: this.#translate(
                    'admin.site.dashboard.overview.insights.seo.inspection.indexing'
                ),
                value: this.#humanizeEnumValue(inspection.indexingState),
                detail: this.#humanizeEnumValue(inspection.pageFetchState),
                tone: inspection.indexingState?.includes('BLOCKED')
                    ? 'critical'
                    : inspection.indexingState?.includes('ALLOWED')
                      ? 'primary'
                      : 'warning',
            },
            {
                id: 'robots',
                label: this.#translate(
                    'admin.site.dashboard.overview.insights.seo.inspection.robots'
                ),
                value: this.#humanizeEnumValue(inspection.robotsTxtState),
                detail: inspection.lastCrawlTime
                    ? this.#translate(
                          'admin.site.dashboard.overview.insights.seo.inspection.lastCrawl',
                          {
                              date: this.#formatDate(inspection.lastCrawlTime, {
                                  month: 'short',
                                  day: 'numeric',
                              }),
                          }
                      )
                    : this.#translate(
                          'admin.site.dashboard.overview.meta.notConfigured'
                      ),
                tone: inspection.robotsTxtState === 'ALLOWED'
                    ? 'primary'
                    : inspection.robotsTxtState?.includes('BLOCKED')
                      ? 'critical'
                      : 'warning',
            },
            {
                id: 'canonical',
                label: this.#translate(
                    'admin.site.dashboard.overview.insights.seo.inspection.canonical'
                ),
                value: canonicalMismatch
                    ? this.#translate(
                          'admin.site.dashboard.overview.insights.seo.inspection.mismatch'
                      )
                    : this.#translate(
                          'admin.site.dashboard.overview.insights.seo.inspection.aligned'
                      ),
                detail: inspection.googleCanonical
                    ? this.#trimUrlDisplay(inspection.googleCanonical)
                    : this.#translate(
                          'admin.site.dashboard.overview.meta.notConfigured'
                      ),
                tone: canonicalMismatch ? 'warning' : 'primary',
            },
            {
                id: 'sitemaps',
                label: this.#translate(
                    'admin.site.dashboard.overview.insights.seo.inspection.sitemaps'
                ),
                value: String(inspection.sitemaps.length),
                detail:
                    inspection.sitemaps[0] ||
                    this.#translate(
                        'admin.site.dashboard.overview.insights.seo.inspection.none'
                    ),
                tone: inspection.sitemaps.length > 0 ? 'primary' : 'warning',
            },
        ];
    }

    #buildSeoState(status: OverviewSeoViewModel['status']): OverviewInsightStateViewModel {
        return {
            title: this.#buildSeoStateTitle(status),
            description: this.#buildSeoStateDescription(status),
            actionLabel:
                status === 'NOT_CONFIGURED'
                    ? this.#translate(
                          'admin.site.dashboard.overview.insights.seo.states.notConfigured.action'
                      )
                    : null,
            tone:
                status === 'ACCESS_ERROR'
                    ? 'warning'
                    : status === 'NOT_CONFIGURED'
                      ? 'primary'
                      : 'neutral',
            icon: this.#buildSeoStateIcon(status),
        };
    }

    #buildPerformanceViewModel(): OverviewPerformanceViewModel {
        const performance = this.insights.performance;
        const scoreTone = this.#performanceScoreTone(performance.score?.label);

        return {
            status: performance.status,
            isReady: performance.status === 'READY',
            scoreValue: performance.score?.value ?? 0,
            scoreLabel: this.#translate(
                `admin.site.dashboard.overview.insights.performance.score.${(
                    performance.score?.label ?? 'ATTENTION'
                ).toLowerCase()}`
            ),
            scoreTone,
            scoreChartOptions: this.#buildPerformanceScoreChartOptions(
                performance.score?.value ?? 0,
                scoreTone
            ),
            metrics: this.#buildPerformanceMetrics(),
            trendOptions: this.#buildPerformanceTrendChartOptions(
                performance.trend
            ),
            hasTrend: performance.trend.some(
                (point) =>
                    point.lcp !== null ||
                    point.inp !== null ||
                    point.cls !== null
            ),
            scopeLabel:
                performance.targetScope === 'ORIGIN'
                    ? this.#translate(
                          'admin.site.dashboard.overview.insights.performance.scope.origin'
                      )
                    : this.#translate(
                          'admin.site.dashboard.overview.insights.performance.scope.url'
                      ),
            targetLabel:
                performance.target ||
                this.insights.resolvedUrl ||
                this.#translate(
                    'admin.site.dashboard.overview.meta.notConfigured'
                ),
            lastSyncedLabel: performance.lastSyncedAt
                ? this.#translate(
                      'admin.site.dashboard.overview.insights.lastSynced',
                      {
                          date: this.#formatDate(performance.lastSyncedAt, {
                              month: 'short',
                              day: 'numeric',
                              hour: '2-digit',
                              minute: '2-digit',
                          }),
                      }
                  )
                : this.#translate(
                      'admin.site.dashboard.overview.insights.performance.desktopOnly'
                  ),
            state: this.#buildPerformanceState(performance.status),
        };
    }

    #buildPerformanceMetrics(): OverviewPerformanceMetricViewModel[] {
        return this.insights.performance.metrics.map((metric) => ({
            id: metric.metric,
            label: this.#translate(
                `admin.site.dashboard.overview.insights.performance.metrics.${metric.metric}.label`
            ),
            value:
                metric.displayValue ??
                this.#translate('admin.site.dashboard.overview.meta.notConfigured'),
            detail: this.#translate(
                `admin.site.dashboard.overview.insights.performance.assessment.${metric.assessment.toLowerCase()}`
            ),
            tone:
                metric.assessment === 'GOOD'
                    ? 'primary'
                    : metric.assessment === 'POOR'
                      ? 'critical'
                      : metric.assessment === 'NEEDS_IMPROVEMENT'
                        ? 'warning'
                        : 'neutral',
        }));
    }

    #buildPerformanceState(
        status: OverviewPerformanceViewModel['status']
    ): OverviewInsightStateViewModel {
        return {
            title: this.#buildPerformanceStateTitle(status),
            description: this.#buildPerformanceStateDescription(status),
            actionLabel: null,
            tone: status === 'ACCESS_ERROR' ? 'warning' : 'neutral',
            icon: this.#buildPerformanceStateIcon(status),
        };
    }

    #buildIdentityViewModel(): OverviewIdentityViewModel {
        const supportedLanguages = this.tenant?.supportedLanguages ?? [];

        return {
            companyInitial: this.tenant?.companyName?.charAt(0) || 'S',
            companyName:
                this.tenant?.companyName ||
                this.#translate('admin.site.dashboard.overview.labels.siteOverview'),
            statusLabel: this.#formatState(this.overview?.status?.state || 'draft'),
            primaryDomainLabel:
                this.tenant?.customDomain ||
                this.tenant?.fullDomain ||
                this.#translate('admin.site.dashboard.overview.meta.notConfigured'),
            lastUpdatedLabel: this.overview?.status?.lastUpdatedAt
                ? this.#formatDate(this.overview.status.lastUpdatedAt)
                : this.#translate(
                      'admin.site.dashboard.overview.meta.noRecentUpdates'
                  ),
            domainTypeLabel: this.#translate(
                this.tenant?.customDomain
                    ? 'admin.site.dashboard.overview.meta.custom'
                    : 'admin.site.dashboard.overview.meta.platform'
            ),
            storageUsedMb: this.tenant?.storageUsedMb ?? 0,
            defaultLanguage: this.tenant?.defaultLanguage ?? null,
            currency: this.tenant?.currency ?? null,
            languages: supportedLanguages.map((language) => ({
                code: language.code,
                label: language.nativeName || language.code,
            })),
        };
    }

    #buildInventoryCards(): OverviewMetricCard[] {
        const stats = this.overview?.stats;
        if (!stats) return [];

        const cards: OverviewMetricCard[] = [
            {
                id: 'pages',
                label: this.#translate('admin.site.dashboard.overview.stats.pages'),
                value: String(stats.pages.total),
                detail: this.#translate(
                    'admin.site.dashboard.overview.meta.publishedCount',
                    { count: stats.pages.published }
                ),
                icon: 'heroicons_outline:document-text',
                numericValue: stats.pages.total,
                tone: 'primary',
            },
            {
                id: 'components',
                label: this.#translate(
                    'admin.site.dashboard.overview.stats.components'
                ),
                value: String(stats.components.total),
                detail:
                    stats.components.weeklyChange > 0
                        ? `+${stats.components.weeklyChange} ${this.#translate(
                              'admin.site.dashboard.overview.thisWeek'
                          )}`
                        : this.#translate(
                              'admin.site.dashboard.overview.meta.noRecentChanges'
                          ),
                icon: 'heroicons_outline:cube',
                numericValue: stats.components.total,
                tone: 'neutral',
            },
            {
                id: 'media',
                label: this.#translate('admin.site.dashboard.overview.stats.media'),
                value: String(stats.media.total),
                detail: this.#translate(
                    'admin.site.dashboard.overview.meta.storedMb',
                    { value: stats.media.totalSizeMb.toFixed(1) }
                ),
                icon: 'heroicons_outline:photo',
                numericValue: stats.media.total,
                tone: 'neutral',
            },
        ];

        if (stats.products) {
            cards.push({
                id: 'products',
                label: this.#translate(
                    'admin.site.dashboard.overview.stats.products'
                ),
                value: String(stats.products.total),
                detail: this.#translate(
                    'admin.site.dashboard.overview.meta.activeCount',
                    { count: stats.products.published }
                ),
                icon: 'heroicons_outline:shopping-bag',
                numericValue: stats.products.total,
                tone: 'neutral',
            });
        }

        return cards;
    }

    #buildContentMixItems(
        cards: OverviewMetricCard[]
    ): OverviewContentMixItem[] {
        const total = cards.reduce((sum, card) => sum + card.numericValue, 0);

        return cards.map((card) => ({
            ...card,
            percentage: total
                ? Math.round((card.numericValue * 100) / total)
                : 0,
        }));
    }

    #buildContextCards(): OverviewContextCard[] {
        const status = this.overview?.status?.state || 'draft';
        const indexingEnabled = Boolean(this.technical?.searchEngine?.indexingEnabled);
        const sitemapEnabled = Boolean(this.technical?.searchEngine?.sitemapEnabled);
        const twoFactorRequired = this.security?.twoFactor?.policy === 'REQUIRED';
        const cookieConsentEnabled = Boolean(this.technical?.cookieConsent?.enabled);
        const lastUpdated = this.overview?.status?.lastUpdatedAt
            ? this.#translate('admin.site.dashboard.overview.meta.updatedOn', {
                  date: this.#formatDate(this.overview.status.lastUpdatedAt),
              })
            : this.#translate(
                  'admin.site.dashboard.overview.meta.noRecentUpdates'
              );

        return [
            {
                id: 'status',
                label: this.#translate(
                    'admin.site.dashboard.overview.labels.publishing'
                ),
                value: this.#formatState(status),
                detail: lastUpdated,
                icon: 'heroicons_outline:shield-check',
                progress:
                    status === 'published'
                        ? 100
                        : status === 'draft'
                          ? 58
                          : 24,
                tone:
                    status === 'maintenance'
                        ? 'critical'
                        : status === 'published'
                          ? 'primary'
                          : 'warning',
            },
            {
                id: 'search',
                label: this.#translate(
                    'admin.site.dashboard.overview.labels.searchVisibility'
                ),
                value: indexingEnabled
                    ? this.#translate(
                          'admin.site.dashboard.overview.meta.indexable'
                      )
                    : this.#translate('admin.site.dashboard.overview.meta.hidden'),
                detail: sitemapEnabled
                    ? this.#translate(
                          'admin.site.dashboard.overview.meta.sitemapEnabled'
                      )
                    : this.#translate(
                          'admin.site.dashboard.overview.meta.sitemapDisabled'
                      ),
                icon: 'heroicons_outline:magnifying-glass',
                progress: indexingEnabled
                    ? sitemapEnabled
                        ? 100
                        : 68
                    : 18,
                tone: indexingEnabled
                    ? sitemapEnabled
                        ? 'primary'
                        : 'warning'
                    : 'critical',
            },
            {
                id: 'security',
                label: this.#translate(
                    'admin.site.dashboard.overview.labels.security'
                ),
                value: twoFactorRequired
                    ? this.#translate(
                          'admin.site.dashboard.overview.meta.twoFactorRequired'
                      )
                    : this.#translate(
                          'admin.site.dashboard.overview.meta.twoFactorDisabled'
                      ),
                detail: cookieConsentEnabled
                    ? this.#translate(
                          'admin.site.dashboard.overview.meta.cookieConsentEnabled'
                      )
                    : this.#translate(
                          'admin.site.dashboard.overview.meta.cookieConsentDisabled'
                      ),
                icon: 'heroicons_outline:lock-closed',
                progress: twoFactorRequired ? 100 : cookieConsentEnabled ? 66 : 42,
                tone: twoFactorRequired ? 'primary' : 'warning',
            },
        ];
    }

    #buildSpotlightRecommendations(
        cards: OverviewContextCard[]
    ): OverviewSpotlightRecommendation[] {
        const recommendations: OverviewSpotlightRecommendation[] = [];
        const statusCard = cards.find((card) => card.id === 'status');
        const searchCard = cards.find((card) => card.id === 'search');
        const securityCard = cards.find((card) => card.id === 'security');
        const publishedPages = this.overview?.stats?.pages?.published ?? 0;
        const totalPages = this.overview?.stats?.pages?.total ?? 0;
        const draftPages = Math.max(totalPages - publishedPages, 0);
        const indexingEnabled = Boolean(this.technical?.searchEngine?.indexingEnabled);
        const sitemapEnabled = Boolean(this.technical?.searchEngine?.sitemapEnabled);
        const twoFactorRequired = this.security?.twoFactor?.policy === 'REQUIRED';

        if (statusCard?.tone === 'critical') {
            recommendations.push({
                id: 'maintenance',
                title: this.#translate(
                    'admin.site.dashboard.overview.spotlight.recommendations.reviewMaintenance.title'
                ),
                detail: this.#translate(
                    'admin.site.dashboard.overview.spotlight.recommendations.reviewMaintenance.detail'
                ),
                icon: 'heroicons_outline:pause-circle',
                tone: 'critical',
            });
        }

        if (!indexingEnabled) {
            recommendations.push({
                id: 'indexing',
                title: this.#translate(
                    'admin.site.dashboard.overview.spotlight.recommendations.enableIndexing.title'
                ),
                detail: this.#translate(
                    'admin.site.dashboard.overview.spotlight.recommendations.enableIndexing.detail'
                ),
                icon: 'heroicons_outline:magnifying-glass',
                tone: 'critical',
            });
        } else if (!sitemapEnabled) {
            recommendations.push({
                id: 'sitemap',
                title: this.#translate(
                    'admin.site.dashboard.overview.spotlight.recommendations.enableSitemap.title'
                ),
                detail: this.#translate(
                    'admin.site.dashboard.overview.spotlight.recommendations.enableSitemap.detail'
                ),
                icon: 'heroicons_outline:map',
                tone: 'warning',
            });
        }

        if (draftPages > 0) {
            recommendations.push({
                id: 'publish-pages',
                title: this.#translate(
                    'admin.site.dashboard.overview.spotlight.recommendations.publishPages.title'
                ),
                detail: this.#translate(
                    'admin.site.dashboard.overview.spotlight.recommendations.publishPages.detail',
                    { count: draftPages }
                ),
                icon: 'heroicons_outline:rocket-launch',
                tone: statusCard?.tone === 'warning' ? 'warning' : 'neutral',
            });
        }

        if (!twoFactorRequired) {
            recommendations.push({
                id: 'two-factor',
                title: this.#translate(
                    'admin.site.dashboard.overview.spotlight.recommendations.requireTwoFactor.title'
                ),
                detail: this.#translate(
                    'admin.site.dashboard.overview.spotlight.recommendations.requireTwoFactor.detail'
                ),
                icon: 'heroicons_outline:lock-closed',
                tone: 'warning',
            });
        }

        if (
            recommendations.length === 0 &&
            searchCard?.tone === 'primary' &&
            securityCard?.tone === 'primary'
        ) {
            recommendations.push({
                id: 'healthy',
                title: this.#translate(
                    'admin.site.dashboard.overview.spotlight.recommendations.healthy.title'
                ),
                detail: this.#translate(
                    'admin.site.dashboard.overview.spotlight.recommendations.healthy.detail'
                ),
                icon: 'heroicons_outline:sparkles',
                tone: 'primary',
            });
        }

        return recommendations.slice(0, 2);
    }

    #buildHealthMetrics(): OverviewHealthMetric[] {
        const stats = this.overview?.stats;
        const technical = this.technical;
        const metrics: OverviewHealthMetric[] = [];

        if (stats?.pages) {
            const totalPages = Math.max(stats.pages.total, 1);
            const progress = Math.round((stats.pages.published / totalPages) * 100);

            metrics.push({
                id: 'pages-live',
                label: this.#translate(
                    'admin.site.dashboard.overview.labels.pagesLive'
                ),
                value: `${progress}%`,
                detail: this.#translate(
                    'admin.site.dashboard.overview.meta.liveCount',
                    {
                        published: stats.pages.published,
                        total: stats.pages.total,
                    }
                ),
                progress,
                icon: 'heroicons_outline:document-text',
                tone:
                    progress >= 100
                        ? 'primary'
                        : progress > 0
                          ? 'neutral'
                          : 'warning',
            });
        }

        if (stats?.products) {
            const totalProducts = Math.max(stats.products.total, 1);
            const progress = Math.round(
                (stats.products.published / totalProducts) * 100
            );

            metrics.push({
                id: 'products-live',
                label: this.#translate(
                    'admin.site.dashboard.overview.labels.productsLive'
                ),
                value: `${progress}%`,
                detail: this.#translate(
                    'admin.site.dashboard.overview.meta.liveActiveCount',
                    {
                        published: stats.products.published,
                        total: stats.products.total,
                    }
                ),
                progress,
                icon: 'heroicons_outline:shopping-bag',
                tone:
                    progress >= 100
                        ? 'primary'
                        : progress > 0
                          ? 'neutral'
                          : 'warning',
            });
        }

        metrics.push({
            id: 'search-index',
            label: this.#translate(
                'admin.site.dashboard.overview.labels.searchIndex'
            ),
            value: technical?.searchEngine?.indexingEnabled
                ? this.#translate('admin.common.enabled')
                : this.#translate('admin.common.disabled'),
            detail: technical?.searchEngine?.indexingEnabled
                ? this.#translate(
                      'admin.site.dashboard.overview.meta.searchCrawlEnabled'
                  )
                : this.#translate(
                      'admin.site.dashboard.overview.meta.searchCrawlBlocked'
                  ),
            progress: technical?.searchEngine?.indexingEnabled ? 100 : 0,
            icon: 'heroicons_outline:magnifying-glass',
            tone: technical?.searchEngine?.indexingEnabled ? 'primary' : 'warning',
        });

        metrics.push({
            id: 'sitemap',
            label: this.#translate(
                'admin.site.dashboard.overview.labels.sitemap'
            ),
            value: technical?.searchEngine?.sitemapEnabled
                ? this.#translate('admin.common.enabled')
                : this.#translate('admin.common.disabled'),
            detail: technical?.searchEngine?.sitemapEnabled
                ? this.#translate(
                      'admin.site.dashboard.overview.meta.sitemapAvailable'
                  )
                : this.#translate(
                      'admin.site.dashboard.overview.meta.sitemapUnavailable'
                  ),
            progress: technical?.searchEngine?.sitemapEnabled ? 100 : 0,
            icon: 'heroicons_outline:map',
            tone: technical?.searchEngine?.sitemapEnabled ? 'primary' : 'warning',
        });

        return metrics;
    }

    #buildSeoTrendChartOptions(
        trend: SiteInsightsSummaryResponse['seo']['trend']
    ): ApexOptions {
        const labels = trend.map((point) =>
            this.#formatDate(point.date, {
                month: 'short',
                day: 'numeric',
            })
        );

        return {
            chart: {
                animations: { speed: 320, animateGradually: { enabled: false } },
                fontFamily: 'inherit',
                foreColor: 'inherit',
                type: 'line',
                height: 210,
                toolbar: { show: false },
                sparkline: { enabled: false },
            },
            series: [
                {
                    name: this.#translate(
                        'admin.site.dashboard.overview.insights.seo.metrics.clicks.label'
                    ),
                    data: trend.map((point) => point.clicks),
                },
                {
                    name: this.#translate(
                        'admin.site.dashboard.overview.insights.seo.metrics.impressions.label'
                    ),
                    data: trend.map((point) => point.impressions),
                },
            ],
            colors: ['#2563EB', '#94A3B8'],
            stroke: {
                curve: trend.length > 1 ? 'smooth' : 'straight',
                width: [3, 2],
            },
            markers: {
                size: trend.length === 1 ? 5 : 0,
                strokeWidth: 0,
            },
            xaxis: {
                categories: labels,
                labels: {
                    style: { fontSize: '11px', fontWeight: 600, colors: '#94A3B8' },
                },
                axisBorder: { show: false },
                axisTicks: { show: false },
            },
            yaxis: {
                labels: {
                    style: { fontSize: '11px', colors: '#94A3B8' },
                    formatter: (value: number) => Math.round(value).toString(),
                },
                min: 0,
            },
            grid: {
                borderColor: '#E2E8F0',
                strokeDashArray: 4,
                xaxis: { lines: { show: false } },
            },
            dataLabels: { enabled: false },
            legend: {
                show: true,
                position: 'top',
                horizontalAlign: 'left',
                fontSize: '12px',
                fontWeight: 600,
                labels: { colors: '#64748B' },
            },
            tooltip: {
                theme: 'dark',
            },
        };
    }

    #buildPerformanceScoreChartOptions(
        scoreValue: number,
        tone: OverviewStateTone
    ): ApexOptions {
        const color =
            tone === 'critical'
                ? '#FB7185'
                : tone === 'warning'
                  ? '#F59E0B'
                  : '#2563EB';

        return {
            chart: {
                fontFamily: 'inherit',
                foreColor: 'inherit',
                type: 'radialBar',
                sparkline: { enabled: true },
                height: 240,
            },
            series: [scoreValue],
            colors: [color],
            plotOptions: {
                radialBar: {
                    hollow: {
                        size: '64%',
                        background: '#F8FAFC',
                    },
                    track: {
                        background: '#E2E8F0',
                        strokeWidth: '100%',
                        margin: 0,
                    },
                    dataLabels: {
                        name: { show: false },
                        value: {
                            offsetY: 6,
                            fontSize: '34px',
                            fontWeight: '800',
                            formatter: (value: number): string =>
                                `${Math.round(value)}%`,
                        },
                    },
                },
            },
            stroke: { lineCap: 'round' },
            labels: [this.#translate('admin.site.dashboard.overview.insights.performance.score.label')],
        };
    }

    #buildPerformanceTrendChartOptions(
        trend: SiteInsightsSummaryResponse['performance']['trend']
    ): ApexOptions {
        const labels = trend.map((point) =>
            point.endDate
                ? this.#formatDate(point.endDate, {
                      month: 'short',
                      day: 'numeric',
                  })
                : ''
        );

        return {
            chart: {
                animations: { speed: 320, animateGradually: { enabled: false } },
                fontFamily: 'inherit',
                foreColor: 'inherit',
                type: 'line',
                height: 190,
                toolbar: { show: false },
                sparkline: { enabled: false },
            },
            series: [
                {
                    name: this.#translate(
                        'admin.site.dashboard.overview.insights.performance.metrics.lcp.label'
                    ),
                    data: trend.map((point) => point.lcp),
                },
                {
                    name: this.#translate(
                        'admin.site.dashboard.overview.insights.performance.metrics.inp.label'
                    ),
                    data: trend.map((point) => point.inp),
                },
                {
                    name: this.#translate(
                        'admin.site.dashboard.overview.insights.performance.metrics.cls.label'
                    ),
                    data: trend.map((point) => point.cls),
                },
            ],
            colors: ['#2563EB', '#0F766E', '#F59E0B'],
            stroke: {
                curve: trend.length > 1 ? 'smooth' : 'straight',
                width: [3, 3, 2],
            },
            markers: { size: 0, strokeWidth: 0 },
            xaxis: {
                categories: labels,
                labels: {
                    style: { fontSize: '11px', fontWeight: 600, colors: '#94A3B8' },
                },
                axisBorder: { show: false },
                axisTicks: { show: false },
            },
            yaxis: {
                labels: {
                    style: { fontSize: '11px', colors: '#94A3B8' },
                    formatter: (value: number) => Math.round(value).toString(),
                },
            },
            grid: {
                borderColor: '#E2E8F0',
                strokeDashArray: 4,
                xaxis: { lines: { show: false } },
            },
            dataLabels: { enabled: false },
            legend: {
                show: true,
                position: 'top',
                horizontalAlign: 'left',
                fontSize: '12px',
                fontWeight: 600,
                labels: { colors: '#64748B' },
            },
            tooltip: {
                theme: 'dark',
            },
        };
    }

    #buildSpotlightStatus(
        operationalScore: number,
        cards: OverviewContextCard[],
        recommendations: OverviewSpotlightRecommendation[]
    ): {
        label: string;
        tone: OverviewStateTone;
        summary: string;
    } {
        const hasCritical = cards.some((card) => card.tone === 'critical');
        const hasWarning = cards.some((card) => card.tone === 'warning');
        const topRecommendation = recommendations[0];

        if (hasCritical || operationalScore < 45) {
            return {
                label: this.#translate(
                    'admin.site.dashboard.overview.spotlight.status.critical'
                ),
                tone: 'critical',
                summary:
                    topRecommendation?.detail ||
                    this.#translate(
                        'admin.site.dashboard.overview.spotlight.summary.critical'
                    ),
            };
        }

        if (hasWarning || operationalScore < 80) {
            return {
                label: this.#translate(
                    'admin.site.dashboard.overview.spotlight.status.attention'
                ),
                tone: 'warning',
                summary:
                    topRecommendation?.detail ||
                    this.#translate(
                        'admin.site.dashboard.overview.spotlight.summary.attention'
                    ),
            };
        }

        return {
            label: this.#translate(
                'admin.site.dashboard.overview.spotlight.status.healthy'
            ),
            tone: 'primary',
            summary: this.#translate(
                'admin.site.dashboard.overview.spotlight.summary.healthy'
            ),
        };
    }

    #calculateOperationalScore(
        metrics: Array<{ progress: number }>
    ): number {
        if (!metrics.length) return 0;

        return Math.round(
            metrics.reduce((total, metric) => total + metric.progress, 0) /
                metrics.length
        );
    }

    #buildContentMixChartOptions(
        items: OverviewContentMixItem[]
    ): ApexOptions {
        const labels = items.map((item) => item.label);
        const series = items.map((item) => item.numericValue);

        return {
            chart: {
                animations: {
                    speed: 400,
                    animateGradually: {
                        enabled: false,
                    },
                },
                fontFamily: 'inherit',
                foreColor: 'inherit',
                type: 'donut',
                sparkline: {
                    enabled: true,
                },
            },
            colors: ['#5B73C8', '#7D8FD1', '#A3AEDD', '#C8D0EC'],
            labels,
            plotOptions: {
                pie: {
                    customScale: 0.92,
                    expandOnClick: false,
                    donut: {
                        size: '72%',
                    },
                },
            },
            series,
            dataLabels: {
                enabled: false,
            },
            legend: {
                show: false,
            },
            stroke: {
                width: 0,
            },
            states: {
                hover: {
                    filter: {
                        type: 'none',
                    },
                },
                active: {
                    filter: {
                        type: 'none',
                    },
                },
            },
            tooltip: {
                enabled: true,
                fillSeriesColor: false,
                theme: 'dark',
                custom: ({ seriesIndex, w }): string =>
                    `<div class="flex items-center h-8 min-h-8 max-h-8 px-3">
                        <div class="w-3 h-3 rounded-full" style="background-color: ${w.config.colors[seriesIndex]};"></div>
                        <div class="ml-2 text-md leading-none">${w.config.labels[seriesIndex]}:</div>
                        <div class="ml-2 text-md font-bold leading-none">${w.config.series[seriesIndex]}</div>
                    </div>`,
            },
        };
    }

    #buildOperationalHealthChartOptions(
        metrics: OverviewContextCard[],
        operationalScore: number
    ): ApexOptions {
        return {
            chart: {
                animations: {
                    speed: 400,
                    animateGradually: {
                        enabled: false,
                    },
                },
                fontFamily: 'inherit',
                foreColor: 'inherit',
                type: 'radialBar',
                sparkline: {
                    enabled: true,
                },
                height: 108,
                width: 108,
            },
            colors: metrics.map((metric) =>
                metric.tone === 'critical'
                    ? '#FB7185'
                    : metric.tone === 'warning'
                      ? '#FBBF24'
                      : metric.tone === 'primary'
                        ? '#60A5FA'
                        : '#94A3B8'
            ),
            labels: metrics.map((metric) => metric.label),
            series: metrics.map((metric) => metric.progress),
            plotOptions: {
                radialBar: {
                    hollow: {
                        size: '28%',
                        background: 'rgba(15, 23, 42, 0.36)',
                    },
                    track: {
                        background: 'rgba(255,255,255,0.08)',
                        strokeWidth: '80%',
                        margin: 6,
                    },
                    dataLabels: {
                        name: {
                            show: false,
                        },
                        value: {
                            offsetY: 4,
                            fontSize: '20px',
                            fontWeight: '800',
                            formatter: (): string => `${operationalScore}%`,
                        },
                    },
                },
            },
            stroke: {
                lineCap: 'round',
            },
            legend: {
                show: false,
            },
            tooltip: {
                enabled: true,
                followCursor: true,
                intersect: false,
                theme: 'dark',
                y: {
                    formatter: (value: number): string => `${value}%`,
                },
            },
        };
    }

    #buildTrendDays(): SiteActivityTrendResponse['content'] {
        if (!this.trend?.content?.length) return [];

        return [...this.trend.content].sort((left, right) =>
            left.date.localeCompare(right.date)
        );
    }

    #buildTrendViewModel(
        days: SiteActivityTrendResponse['content']
    ): OverviewTrendViewModel {
        const labels = days.map((day) =>
            this.#formatDate(day.date, {
                weekday: 'short',
                month: 'short',
                day: 'numeric',
            })
        );

        return {
            visible: Boolean(this.trend),
            hasData: days.some((day) => day.total > 0),
            windowLabel: days.length
                ? this.#translate(
                      'admin.site.dashboard.overview.meta.trendWindow',
                      { count: days.length }
                  )
                : '',
            sliceLabel: this.#buildTrendSliceLabel(days),
            canNavigatePrevious:
                this.trendPageIndex + 1 < (this.trend?.totalPages ?? 0),
            canNavigateNext: this.trendPageIndex > 0,
            chartOptions: this.#buildTrendChartOptions(days, labels),
        };
    }

    #buildTrendSliceLabel(days: SiteActivityTrendResponse['content']): string {
        if (!days.length) {
            return this.#translate(
                'admin.site.dashboard.overview.meta.noActivitySlice'
            );
        }

        return this.#translate('admin.site.dashboard.overview.meta.trendSlice', {
            start: this.#formatDate(days[0].date, {
                month: 'short',
                day: 'numeric',
            }),
            end: this.#formatDate(days[days.length - 1].date, {
                month: 'short',
                day: 'numeric',
            }),
        });
    }

    #buildTrendChartOptions(
        days: SiteActivityTrendResponse['content'],
        labels: string[]
    ): ApexOptions {
        return {
            chart: {
                animations: { speed: 350, animateGradually: { enabled: false } },
                fontFamily: 'inherit',
                foreColor: 'inherit',
                type: 'bar',
                stacked: true,
                toolbar: { show: false },
                sparkline: { enabled: false },
                height: 220,
            },
            colors: ['#2563EB', '#F59E0B', '#10B981'],
            series: [
                {
                    name: this.#translate(
                        'admin.site.dashboard.overview.series.created'
                    ),
                    data: days.map((day) => day.created),
                },
                {
                    name: this.#translate(
                        'admin.site.dashboard.overview.series.updated'
                    ),
                    data: days.map((day) => day.updated),
                },
                {
                    name: this.#translate(
                        'admin.site.dashboard.overview.series.published'
                    ),
                    data: days.map((day) => day.published),
                },
            ],
            xaxis: {
                categories: labels,
                labels: {
                    style: { fontSize: '11px', fontWeight: 600, colors: '#94A3B8' },
                    rotate: -30,
                },
                axisBorder: { show: false },
                axisTicks: { show: false },
            },
            yaxis: {
                labels: {
                    style: { fontSize: '11px', colors: '#94A3B8' },
                    formatter: (value: number) => Math.floor(value).toString(),
                },
                min: 0,
            },
            grid: {
                borderColor: '#F1F5F9',
                strokeDashArray: 4,
                xaxis: { lines: { show: false } },
            },
            plotOptions: {
                bar: { borderRadius: 4, columnWidth: '52%' },
            },
            dataLabels: { enabled: false },
            stroke: { width: 0 },
            legend: {
                show: true,
                position: 'top',
                horizontalAlign: 'left',
                fontSize: '12px',
                fontWeight: 600,
                labels: { colors: '#64748B' },
                markers: { shape: 'circle' as const },
            },
            tooltip: {
                theme: 'dark',
                y: {
                    formatter: (value: number) =>
                        this.#translate(
                            'admin.site.dashboard.overview.meta.eventsCount',
                            { count: value }
                        ),
                },
            },
        };
    }

    #buildAnalyticsTrendChartOptions(
        trend: SiteAnalyticsSummaryResponse['trend']
    ): ApexOptions {
        const labels = trend.map((point) =>
            this.#formatDate(point.date, {
                weekday: 'short',
                day: 'numeric',
            })
        );

        return {
            chart: {
                animations: {
                    speed: 320,
                    animateGradually: {
                        enabled: false,
                    },
                },
                fontFamily: 'inherit',
                foreColor: 'inherit',
                type: 'area',
                height: 240,
                toolbar: {
                    show: false,
                },
                sparkline: {
                    enabled: false,
                },
            },
            series: [
                {
                    name: this.#translate(
                        'admin.site.dashboard.overview.analytics.metrics.activeUsers.label'
                    ),
                    data: trend.map((point) => point.value),
                },
            ],
            colors: ['#2563eb'],
            stroke: {
                curve: trend.length > 1 ? 'smooth' : 'straight',
                width: 3,
            },
            markers: {
                size: trend.length === 1 ? 5 : 0,
                strokeWidth: 0,
                hover: {
                    sizeOffset: 2,
                },
            },
            fill: {
                type: 'gradient',
                gradient: {
                    shadeIntensity: 1,
                    opacityFrom: 0.28,
                    opacityTo: 0.02,
                    stops: [0, 95, 100],
                },
            },
            xaxis: {
                categories: labels,
                labels: {
                    style: {
                        fontSize: '11px',
                        fontWeight: 600,
                        colors: '#94A3B8',
                    },
                },
                axisBorder: { show: false },
                axisTicks: { show: false },
            },
            yaxis: {
                min: 0,
                labels: {
                    style: {
                        fontSize: '11px',
                        colors: '#94A3B8',
                    },
                    formatter: (value: number) => Math.round(value).toString(),
                },
            },
            dataLabels: {
                enabled: false,
            },
            grid: {
                borderColor: '#E2E8F0',
                strokeDashArray: 4,
                xaxis: { lines: { show: false } },
            },
            tooltip: {
                theme: 'dark',
                y: {
                    formatter: (value: number) =>
                        this.#formatAnalyticsMetricValue('activeUsers', value),
                },
            },
            legend: {
                show: false,
            },
        };
    }

    #normalizeAnalyticsTrend(
        trend: SiteAnalyticsSummaryResponse['trend']
    ): SiteAnalyticsSummaryResponse['trend'] {
        return trend
            .filter((point) => Boolean(point?.date))
            .map((point) => ({
                ...point,
                value: this.#sanitizeNumber(point.value),
            }));
    }

    #buildAnalyticsSetupUrl(): string {
        const subdomain = this.tenant?.subdomain?.trim();
        if (subdomain) {
            return `${window.location.origin}/config?subdomain=${encodeURIComponent(subdomain)}`;
        }
        return `${window.location.origin}/config`;
    }

    #buildSeoStateTitle(status: OverviewSeoViewModel['status']): string {
        switch (status) {
            case 'READY':
                return this.#translate(
                    'admin.site.dashboard.overview.insights.seo.title'
                );
            case 'NOT_CONFIGURED':
                return this.#translate(
                    'admin.site.dashboard.overview.insights.seo.states.notConfigured.title'
                );
            case 'DISABLED':
                return this.#translate(
                    'admin.site.dashboard.overview.insights.seo.states.disabled.title'
                );
            case 'NO_DATA':
                return this.#translate(
                    'admin.site.dashboard.overview.insights.seo.states.noData.title'
                );
            default:
                return this.#translate(
                    'admin.site.dashboard.overview.insights.seo.states.accessError.title'
                );
        }
    }

    #buildSeoStateDescription(status: OverviewSeoViewModel['status']): string {
        switch (status) {
            case 'NOT_CONFIGURED':
                return this.#translate(
                    'admin.site.dashboard.overview.insights.seo.states.notConfigured.description'
                );
            case 'DISABLED':
                return this.#translate(
                    'admin.site.dashboard.overview.insights.seo.states.disabled.description'
                );
            case 'NO_DATA':
                return this.#translate(
                    'admin.site.dashboard.overview.insights.seo.states.noData.description'
                );
            default:
                return this.#translate(
                    'admin.site.dashboard.overview.insights.seo.states.accessError.description'
                );
        }
    }

    #buildSeoStateIcon(status: OverviewSeoViewModel['status']): string {
        switch (status) {
            case 'NOT_CONFIGURED':
                return 'heroicons_outline:wrench-screwdriver';
            case 'DISABLED':
                return 'heroicons_outline:pause-circle';
            case 'NO_DATA':
                return 'heroicons_outline:magnifying-glass';
            default:
                return 'heroicons_outline:signal-slash';
        }
    }

    #buildPerformanceStateTitle(
        status: OverviewPerformanceViewModel['status']
    ): string {
        switch (status) {
            case 'READY':
                return this.#translate(
                    'admin.site.dashboard.overview.insights.performance.title'
                );
            case 'NOT_CONFIGURED':
                return this.#translate(
                    'admin.site.dashboard.overview.insights.performance.states.notConfigured.title'
                );
            case 'DISABLED':
                return this.#translate(
                    'admin.site.dashboard.overview.insights.performance.states.disabled.title'
                );
            case 'NO_DATA':
                return this.#translate(
                    'admin.site.dashboard.overview.insights.performance.states.noData.title'
                );
            default:
                return this.#translate(
                    'admin.site.dashboard.overview.insights.performance.states.accessError.title'
                );
        }
    }

    #buildPerformanceStateDescription(
        status: OverviewPerformanceViewModel['status']
    ): string {
        switch (status) {
            case 'NOT_CONFIGURED':
                return this.#translate(
                    'admin.site.dashboard.overview.insights.performance.states.notConfigured.description'
                );
            case 'DISABLED':
                return this.#translate(
                    'admin.site.dashboard.overview.insights.performance.states.disabled.description'
                );
            case 'NO_DATA':
                return this.#translate(
                    'admin.site.dashboard.overview.insights.performance.states.noData.description'
                );
            default:
                return this.#translate(
                    'admin.site.dashboard.overview.insights.performance.states.accessError.description'
                );
        }
    }

    #buildPerformanceStateIcon(
        status: OverviewPerformanceViewModel['status']
    ): string {
        switch (status) {
            case 'NOT_CONFIGURED':
                return 'heroicons_outline:globe-alt';
            case 'DISABLED':
                return 'heroicons_outline:pause-circle';
            case 'NO_DATA':
                return 'heroicons_outline:chart-bar-square';
            default:
                return 'heroicons_outline:signal-slash';
        }
    }

    #buildAnalyticsStateTitle(
        status: OverviewAnalyticsViewModel['status']
    ): string {
        switch (status) {
            case 'READY':
                return this.#translate(
                    'admin.site.dashboard.overview.analytics.title'
                );
            case 'NOT_CONFIGURED':
                return this.#translate(
                    'admin.site.dashboard.overview.analytics.states.notConfigured.title'
                );
            case 'DISABLED':
                return this.#translate(
                    'admin.site.dashboard.overview.analytics.states.disabled.title'
                );
            case 'NO_DATA':
                return this.#translate(
                    'admin.site.dashboard.overview.analytics.states.noData.title'
                );
            default:
                return this.#translate(
                    'admin.site.dashboard.overview.analytics.states.accessError.title'
                );
        }
    }

    #buildAnalyticsStateDescription(
        status: OverviewAnalyticsViewModel['status']
    ): string {
        switch (status) {
            case 'NOT_CONFIGURED':
                return this.#translate(
                    'admin.site.dashboard.overview.analytics.states.notConfigured.description'
                );
            case 'DISABLED':
                return this.#translate(
                    'admin.site.dashboard.overview.analytics.states.disabled.description'
                );
            case 'NO_DATA':
                return this.#translate(
                    'admin.site.dashboard.overview.analytics.states.noData.description'
                );
            default:
                return this.#translate(
                    'admin.site.dashboard.overview.analytics.states.accessError.description'
                );
        }
    }

    #buildAnalyticsStateIcon(
        status: OverviewAnalyticsViewModel['status']
    ): string {
        switch (status) {
            case 'NOT_CONFIGURED':
                return 'heroicons_outline:wrench-screwdriver';
            case 'DISABLED':
                return 'heroicons_outline:pause-circle';
            case 'NO_DATA':
                return 'heroicons_outline:chart-bar-square';
            default:
                return 'heroicons_outline:signal-slash';
        }
    }

    #formatAnalyticsMetricValue(
        metric: OverviewAnalyticsMetricCard['id'],
        value: number
    ): string {
        const safeValue = this.#sanitizeNumber(value);
        switch (metric) {
            case 'engagementRate':
                return `${(safeValue * 100).toFixed(1)}%`;
            default:
                return new Intl.NumberFormat(this.#localeTag(), {
                    maximumFractionDigits: 0,
                }).format(Math.round(safeValue));
        }
    }

    #formatSeoMetricValue(metric: OverviewSeoMetricCard['id'], value: number): string {
        const safeValue = this.#sanitizeNumber(value);
        switch (metric) {
            case 'ctr':
                return `${(safeValue * 100).toFixed(1)}%`;
            case 'position':
                return safeValue.toFixed(1);
            default:
                return new Intl.NumberFormat(this.#localeTag(), {
                    maximumFractionDigits: 0,
                }).format(Math.round(safeValue));
        }
    }

    #formatAnalyticsDelta(deltaPercentage: number | null): string {
        if (
            deltaPercentage === null ||
            deltaPercentage === undefined ||
            !Number.isFinite(deltaPercentage)
        ) {
            return this.#translate(
                'admin.site.dashboard.overview.analytics.delta.unavailable'
            );
        }

        const rounded = Math.abs(deltaPercentage).toFixed(0);
        const prefix = deltaPercentage > 0 ? '+' : deltaPercentage < 0 ? '-' : '';
        return this.#translate(
            'admin.site.dashboard.overview.analytics.delta.vsPrevious',
            {
                value: `${prefix}${rounded}%`,
            }
        );
    }

    #resolveAnalyticsDeltaTone(
        deltaPercentage: number | null,
        direction: string
    ): OverviewDeltaTone {
        if (
            deltaPercentage === null ||
            deltaPercentage === undefined ||
            !Number.isFinite(deltaPercentage)
        ) {
            return 'neutral';
        }

        return this.#toDeltaTone(direction);
    }

    #toDeltaTone(direction: string): OverviewDeltaTone {
        switch (direction) {
            case 'up':
                return 'positive';
            case 'down':
                return 'negative';
            default:
                return 'neutral';
        }
    }

    #sanitizeNumber(value: number | null | undefined): number {
        return Number.isFinite(value) ? value : 0;
    }

    #analyticsMetricIcon(metric: string): string {
        switch (metric) {
            case 'activeUsers':
                return 'heroicons_outline:user-group';
            case 'screenPageViews':
                return 'heroicons_outline:eye';
            case 'newUsers':
                return 'heroicons_outline:sparkles';
            case 'engagementRate':
                return 'heroicons_outline:bolt';
            default:
                return 'heroicons_outline:chart-bar';
        }
    }

    #seoMetricIcon(metric: string): string {
        switch (metric) {
            case 'clicks':
                return 'heroicons_outline:cursor-arrow-rays';
            case 'impressions':
                return 'heroicons_outline:eye';
            case 'ctr':
                return 'heroicons_outline:arrow-trending-up';
            case 'position':
                return 'heroicons_outline:trophy';
            default:
                return 'heroicons_outline:chart-bar';
        }
    }

    #performanceScoreTone(label?: string | null): OverviewStateTone {
        switch (label) {
            case 'HEALTHY':
                return 'primary';
            case 'CRITICAL':
                return 'critical';
            default:
                return 'warning';
        }
    }

    #humanizeEnumValue(value: string | null | undefined): string {
        if (!value) {
            return this.#translate('admin.site.dashboard.overview.meta.notConfigured');
        }
        return value
            .replace(/_/g, ' ')
            .replace(/\b\w/g, (char) => char.toUpperCase());
    }

    #trimUrlDisplay(value: string): string {
        return value
            .replace(/^https?:\/\//, '')
            .replace(/\/$/, '');
    }

    #formatState(value: string): string {
        switch (value) {
            case 'published':
                return this.#translate('admin.site.dashboard.overview.published');
            case 'maintenance':
                return this.#translate(
                    'admin.site.dashboard.overview.maintenance'
                );
            case 'draft':
                return this.#translate('admin.site.dashboard.overview.draft');
            default:
                return this.#translate(
                    'admin.site.dashboard.overview.meta.unknown'
                );
        }
    }

    #translate(key: string, params?: Record<string, string | number>): string {
        return this.#transloco.translate(key, params);
    }

    #formatDate(
        value: string,
        options?: Intl.DateTimeFormatOptions
    ): string {
        const normalized = /^\d{4}-\d{2}-\d{2}$/.test(value) ? `${value}T00:00:00` : value;
        return new Intl.DateTimeFormat(this.#localeTag(), options).format(
            new Date(normalized)
        );
    }

    #localeTag(): string {
        return this.#transloco.getActiveLang().toLowerCase().startsWith('tr')
            ? 'tr-TR'
            : 'en-US';
    }
}
