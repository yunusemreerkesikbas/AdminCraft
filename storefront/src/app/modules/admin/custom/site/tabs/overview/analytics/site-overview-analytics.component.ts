import { NgClass } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
    ChangeDetectionStrategy,
    Component,
    DestroyRef,
    Input,
    OnChanges,
    ViewEncapsulation,
    inject,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { ApexOptions, NgApexchartsModule } from 'ng-apexcharts';
import { SpaGenericModalComponent } from '@shared/components/spa-generic-modal';
import {
    ModalConfig,
    ModalSection,
    SPA_GENERIC_MODAL_DIALOG_OPTIONS,
} from '@shared/components/spa-generic-modal/spa-generic-modal.types';
import { SiteAnalyticsSummaryResponse } from '../../../site.types';

type OverviewStateTone = 'primary' | 'neutral' | 'warning' | 'critical';
type OverviewDeltaTone = 'positive' | 'negative' | 'neutral';

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

@Component({
    selector: 'spa-site-overview-analytics',
    templateUrl: './site-overview-analytics.component.html',
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: true,
    imports: [
        NgClass,
        MatButtonModule,
        MatIconModule,
        MatTooltipModule,
        TranslocoModule,
        NgApexchartsModule,
    ],
})
export class SpaSiteOverviewAnalyticsComponent implements OnChanges {
    readonly #destroyRef = inject(DestroyRef);
    readonly #dialog = inject(MatDialog);
    readonly #transloco = inject(TranslocoService);

    @Input({ required: true }) analytics!: SiteAnalyticsSummaryResponse;
    @Input() tenantSubdomain: string | null = null;

    protected vm!: OverviewAnalyticsViewModel;

    constructor() {
        this.#transloco.langChanges$
            .pipe(takeUntilDestroyed(this.#destroyRef))
            .subscribe(() => {
                if (this.analytics) {
                    this.#rebuildViewModel();
                }
            });
    }

    ngOnChanges(): void {
        this.#rebuildViewModel();
    }

    protected openSetup(): void {
        window.open(this.vm.setupUrl, '_blank', 'noopener,noreferrer');
    }

    protected getDeltaClasses(tone: OverviewDeltaTone): string {
        switch (tone) {
            case 'positive':
                return 'bg-emerald-50 text-emerald-700';
            case 'negative':
                return 'bg-rose-50 text-rose-700';
            default:
                return 'bg-slate-100 text-slate-600';
        }
    }

    protected openMetricInfo(metric: OverviewAnalyticsMetricCard['id']): void {
        const dialogConfig = this.#buildMetricInfoModal(metric);

        this.#dialog.open(SpaGenericModalComponent, {
            ...SPA_GENERIC_MODAL_DIALOG_OPTIONS,
            width: '560px',
            maxWidth: 'calc(100vw - 2rem)',
            data: dialogConfig,
        });
    }

    #rebuildViewModel(): void {
        this.vm = this.#createViewModel();
    }

    #createViewModel(): OverviewAnalyticsViewModel {
        const status = this.analytics.status;
        const trend = this.#normalizeTrend(this.analytics.trend);

        return {
            cards: this.#buildCards(),
            chartOptions: this.#buildTrendChartOptions(trend),
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
            setupUrl: this.#buildSetupUrl(),
            stateTitle: this.#buildStateTitle(status),
            stateDescription: this.#buildStateDescription(status),
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
            stateIcon: this.#buildStateIcon(status),
            isReady: status === 'READY',
        };
    }

    #buildCards(): OverviewAnalyticsMetricCard[] {
        return this.analytics.cards.map((card) => ({
            id: card.metric,
            label: this.#translate(
                `admin.site.dashboard.overview.analytics.metrics.${card.metric}.label`
            ),
            value: this.#formatMetricValue(card.metric, card.value),
            detail: this.#translate(
                `admin.site.dashboard.overview.analytics.metrics.${card.metric}.detail`
            ),
            deltaLabel: this.#formatDelta(card.deltaPercentage),
            deltaTone: this.#resolveDeltaTone(
                card.deltaPercentage,
                card.deltaDirection
            ),
            icon: this.#metricIcon(card.metric),
        }));
    }

    #buildTrendChartOptions(
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
                        this.#formatMetricValue('activeUsers', value),
                },
            },
            legend: {
                show: false,
            },
        };
    }

    #normalizeTrend(
        trend: SiteAnalyticsSummaryResponse['trend']
    ): SiteAnalyticsSummaryResponse['trend'] {
        return trend
            .filter((point) => Boolean(point?.date))
            .map((point) => ({
                ...point,
                value: this.#sanitizeNumber(point.value),
            }));
    }

    #buildSetupUrl(): string {
        const subdomain = this.tenantSubdomain?.trim();
        if (subdomain) {
            return `${window.location.origin}/config?subdomain=${encodeURIComponent(subdomain)}`;
        }

        return `${window.location.origin}/config`;
    }

    #buildStateTitle(status: SiteAnalyticsSummaryResponse['status']): string {
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

    #buildStateDescription(
        status: SiteAnalyticsSummaryResponse['status']
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

    #buildStateIcon(status: SiteAnalyticsSummaryResponse['status']): string {
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

    #formatMetricValue(
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

    #formatDelta(deltaPercentage: number | null): string {
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

    #resolveDeltaTone(
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

    #metricIcon(metric: string): string {
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

    #buildMetricInfoModal(
        metric: OverviewAnalyticsMetricCard['id']
    ): ModalConfig<null> {
        return {
            type: 'info',
            title: this.#translate(
                `admin.site.dashboard.overview.analytics.metrics.${metric}.info.title`
            ),
            icon: 'info',
            data: null,
            sections: this.#buildMetricInfoSections(metric),
            actions: [
                {
                    label: this.#translate('admin.common.actions.close'),
                },
            ],
        };
    }

    #buildMetricInfoSections(
        metric: OverviewAnalyticsMetricCard['id']
    ): ModalSection[] {
        return ['summary', 'source', 'interpretation'].map((section) => ({
            type: 'info-box',
            title: this.#translate(
                `admin.site.dashboard.overview.analytics.infoSections.${section}`
            ),
            content: this.#translate(
                `admin.site.dashboard.overview.analytics.metrics.${metric}.info.${section}`
            ),
        }));
    }

    #translate(key: string, params?: Record<string, string | number>): string {
        return this.#transloco.translate(key, params);
    }

    #formatDate(value: string, options?: Intl.DateTimeFormatOptions): string {
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
