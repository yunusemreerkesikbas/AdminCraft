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
import { SpaAdminPaginatorComponent } from '@shared/components/spa-admin-paginator/spa-admin-paginator.component';
import {
    ActivityDto,
    SiteAnalyticsSummaryResponse,
    SiteActivityFeedResponse,
    SiteActivityTrendResponse,
    SiteInsightsSummaryResponse,
    SiteOverviewResponse,
    SiteOverviewSpotlightContextCardDto,
    SiteOverviewSpotlightRecommendationDto,
    SiteOverviewSpotlightStatusCode,
    SiteOverviewSpotlightTone,
} from '../../site.types';
import { SpaSiteOverviewAnalyticsComponent } from './analytics/site-overview-analytics.component';
import { SpaSiteOverviewPerformanceComponent } from './performance/site-overview-performance.component';
import { SpaSiteOverviewSeoComponent } from './seo/site-overview-seo.component';

type OverviewMetricTone = 'primary' | 'neutral';
type OverviewStateTone = SiteOverviewSpotlightTone;

interface OverviewMetricCard {
    id: string;
    label: string;
    value: string;
    detail: string;
    icon: string;
    numericValue: number;
    tone: OverviewMetricTone;
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

interface OverviewSpotlightViewModel {
    visible: boolean;
    operationalScore: number;
    statusLabel: string;
    statusTone: OverviewStateTone;
    summary: string;
    contextCards: OverviewSpotlightContextCard[];
    recommendations: OverviewSpotlightRecommendation[];
    chartOptions: ApexOptions;
}

interface OverviewSpotlightContextCard {
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

interface OverviewInventoryViewModel {
    cards: OverviewMetricCard[];
    hasCards: boolean;
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
    spotlight: OverviewSpotlightViewModel;
    inventory: OverviewInventoryViewModel;
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
        SpaSiteOverviewAnalyticsComponent,
        SpaSiteOverviewSeoComponent,
        SpaSiteOverviewPerformanceComponent,
    ],
})
export class SpaSiteOverviewComponent implements OnChanges {
    readonly #destroyRef = inject(DestroyRef);
    readonly #transloco = inject(TranslocoService);

    @Input() overview: SiteOverviewResponse | null = null;
    @Input({ required: true }) analytics!: SiteAnalyticsSummaryResponse;
    @Input({ required: true }) insights!: SiteInsightsSummaryResponse;
    @Input() tenant: TenantDetailResponse | null = null;
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

    protected getContextIconClasses(tone: OverviewStateTone): string {
        switch (tone) {
            case 'PRIMARY':
                return 'bg-blue-50 text-blue-500 ring-1 ring-blue-200/60';
            case 'CRITICAL':
                return 'bg-rose-50 text-rose-500 ring-1 ring-rose-200/70';
            case 'WARNING':
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

    protected getSpotlightStatusClasses(tone: OverviewStateTone): string {
        switch (tone) {
            case 'PRIMARY':
                return 'bg-emerald-400/16 text-emerald-50 ring-1 ring-emerald-300/25';
            case 'CRITICAL':
                return 'bg-rose-400/16 text-rose-50 ring-1 ring-rose-300/25';
            case 'WARNING':
                return 'bg-amber-400/16 text-amber-50 ring-1 ring-amber-300/25';
            default:
                return 'bg-white/10 text-white/80 ring-1 ring-white/10';
        }
    }

    protected getSpotlightContextCardClasses(tone: OverviewStateTone): string {
        switch (tone) {
            case 'PRIMARY':
                return 'spa-site-overview__spotlight-context-card--primary';
            case 'CRITICAL':
                return 'spa-site-overview__spotlight-context-card--critical';
            case 'WARNING':
                return 'spa-site-overview__spotlight-context-card--warning';
            default:
                return 'spa-site-overview__spotlight-context-card--neutral';
        }
    }

    protected getSpotlightProgressClasses(tone: OverviewStateTone): string {
        switch (tone) {
            case 'PRIMARY':
                return 'text-sky-200';
            case 'CRITICAL':
                return 'text-rose-200';
            case 'WARNING':
                return 'text-amber-200';
            default:
                return 'text-white/55';
        }
    }

    protected getSpotlightRecommendationClasses(tone: OverviewStateTone): string {
        switch (tone) {
            case 'PRIMARY':
                return 'border-sky-300/18 bg-sky-400/[0.10] text-white';
            case 'CRITICAL':
                return 'border-rose-300/18 bg-rose-400/[0.12] text-white';
            case 'WARNING':
                return 'border-amber-300/18 bg-amber-400/[0.12] text-white';
            default:
                return 'border-white/8 bg-white/[0.06] text-white';
        }
    }

    protected getSpotlightProgressBarFillClasses(tone: OverviewStateTone): string {
        switch (tone) {
            case 'PRIMARY':
                return 'spa-site-overview__spotlight-progress-bar-fill--primary';
            case 'CRITICAL':
                return 'spa-site-overview__spotlight-progress-bar-fill--critical';
            case 'WARNING':
                return 'spa-site-overview__spotlight-progress-bar-fill--warning';
            default:
                return 'spa-site-overview__spotlight-progress-bar-fill--neutral';
        }
    }

    #rebuildViewModel(): void {
        this.vm = this.#createViewModel();
    }

    #createViewModel(): OverviewViewModel {
        const inventoryCards = this.#buildInventoryCards();
        const trendDays = this.#buildTrendDays();
        const enabledModules = this.modules.filter(
            (module) => module.status === 'enabled'
        );
        const activityFeedItems = this.activityFeed?.content ?? [];

        return {
            heroVisible: Boolean(this.tenant || this.overview),
            identity: this.#buildIdentityViewModel(),
            modules: {
                enabledCount: enabledModules.length,
                items: enabledModules,
            },
            spotlight: this.#buildSpotlightViewModel(),
            inventory: {
                cards: inventoryCards,
                hasCards: inventoryCards.length > 0,
            },
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

    #buildSpotlightViewModel(): OverviewSpotlightViewModel {
        const spotlight = this.overview?.spotlight;
        const contextCards =
            spotlight?.contextCards.map((card) =>
                this.#buildSpotlightContextCard(card)
            ) ?? [];
        const recommendations =
            spotlight?.recommendations.map((recommendation) =>
                this.#buildSpotlightRecommendation(recommendation)
            ) ?? [];
        const operationalScore = spotlight?.operationalScore ?? 0;
        const statusCode = spotlight?.status?.code ?? null;

        return {
            visible: Boolean(spotlight),
            operationalScore,
            statusLabel: statusCode
                ? this.#translate(
                      `admin.site.dashboard.overview.spotlight.status.${statusCode}`
                  )
                : '',
            statusTone: spotlight?.status?.tone ?? 'NEUTRAL',
            summary: this.#buildSpotlightSummary(statusCode, recommendations),
            contextCards,
            recommendations,
            chartOptions: this.#buildOperationalHealthChartOptions(
                contextCards,
                operationalScore
            ),
        };
    }

    #buildSpotlightSummary(
        statusCode: SiteOverviewSpotlightStatusCode | null,
        recommendations: OverviewSpotlightRecommendation[]
    ): string {
        if (recommendations.length > 0) {
            return recommendations[0].detail;
        }

        return statusCode
            ? this.#translate(
                  `admin.site.dashboard.overview.spotlight.summary.${statusCode}`
              )
            : '';
    }

    #buildSpotlightContextCard(
        card: SiteOverviewSpotlightContextCardDto
    ): OverviewSpotlightContextCard {
        return {
            id: card.id,
            label: this.#buildSpotlightContextLabel(card.id),
            value: this.#buildSpotlightContextValue(card),
            detail: this.#buildSpotlightContextDetail(card),
            icon: card.icon,
            progress: card.progress,
            tone: card.tone,
        };
    }

    #buildSpotlightContextLabel(id: SiteOverviewSpotlightContextCardDto['id']): string {
        switch (id) {
            case 'status':
                return this.#translate(
                    'admin.site.dashboard.overview.labels.publishing'
                );
            case 'search':
                return this.#translate(
                    'admin.site.dashboard.overview.labels.searchVisibility'
                );
            case 'security':
                return this.#translate(
                    'admin.site.dashboard.overview.labels.security'
                );
        }
    }

    #buildSpotlightContextValue(
        card: SiteOverviewSpotlightContextCardDto
    ): string {
        if (card.id === 'status') {
            return this.#formatState(card.valueCode);
        }

        return this.#translate(
            `admin.site.dashboard.overview.meta.${card.valueCode}`
        );
    }

    #buildSpotlightContextDetail(
        card: SiteOverviewSpotlightContextCardDto
    ): string {
        if (card.detailCode === 'updatedOn') {
            if (!card.detailDate) {
                return this.#translate(
                    'admin.site.dashboard.overview.meta.noRecentUpdates'
                );
            }

            return this.#translate('admin.site.dashboard.overview.meta.updatedOn', {
                date: this.#formatDate(card.detailDate, {
                    day: 'numeric',
                    month: 'short',
                    year: 'numeric',
                }),
            });
        }

        return this.#translate(
            `admin.site.dashboard.overview.meta.${card.detailCode}`
        );
    }

    #buildSpotlightRecommendation(
        recommendation: SiteOverviewSpotlightRecommendationDto
    ): OverviewSpotlightRecommendation {
        const key = this.#resolveSpotlightRecommendationKey(recommendation.id);

        return {
            id: recommendation.id,
            title: this.#translate(
                `admin.site.dashboard.overview.spotlight.recommendations.${key}.title`
            ),
            detail: this.#translate(
                `admin.site.dashboard.overview.spotlight.recommendations.${key}.detail`,
                recommendation.count !== null
                    ? { count: recommendation.count }
                    : undefined
            ),
            icon: recommendation.icon,
            tone: recommendation.tone,
        };
    }

    #resolveSpotlightRecommendationKey(
        id: SiteOverviewSpotlightRecommendationDto['id']
    ): string {
        switch (id) {
            case 'maintenance':
                return 'reviewMaintenance';
            case 'indexing':
                return 'enableIndexing';
            case 'sitemap':
                return 'enableSitemap';
            case 'publish-pages':
                return 'publishPages';
            case 'two-factor':
                return 'requireTwoFactor';
            case 'healthy':
                return 'healthy';
        }
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

    #buildOperationalHealthChartOptions(
        metrics: OverviewSpotlightContextCard[],
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
                metric.tone === 'CRITICAL'
                    ? '#FB7185'
                    : metric.tone === 'WARNING'
                      ? '#FBBF24'
                      : metric.tone === 'PRIMARY'
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
