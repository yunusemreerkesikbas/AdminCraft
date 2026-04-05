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
import { MatIconModule } from '@angular/material/icon';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { ApexOptions, NgApexchartsModule } from 'ng-apexcharts';
import { SiteInsightsSummaryResponse } from '../../../site.types';

type OverviewStateTone = 'primary' | 'neutral' | 'warning' | 'critical';

interface OverviewInsightStateViewModel {
    title: string;
    description: string;
    actionLabel: string | null;
    tone: OverviewStateTone;
    icon: string;
}

interface OverviewPerformanceMetricViewModel {
    id: string;
    label: string;
    value: string;
    detail: string;
    tone: OverviewStateTone;
}

interface OverviewPerformanceViewModel {
    isReady: boolean;
    scoreLabel: string;
    scoreChartOptions: ApexOptions;
    metrics: OverviewPerformanceMetricViewModel[];
    trendOptions: ApexOptions;
    hasTrend: boolean;
    scopeLabel: string;
    targetLabel: string;
    lastSyncedLabel: string;
    state: OverviewInsightStateViewModel;
}

@Component({
    selector: 'spa-site-overview-performance',
    templateUrl: './site-overview-performance.component.html',
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: true,
    imports: [NgClass, MatIconModule, TranslocoModule, NgApexchartsModule],
})
export class SpaSiteOverviewPerformanceComponent implements OnChanges {
    readonly #destroyRef = inject(DestroyRef);
    readonly #transloco = inject(TranslocoService);

    @Input({ required: true }) insights!: SiteInsightsSummaryResponse;

    protected vm!: OverviewPerformanceViewModel;

    constructor() {
        this.#transloco.langChanges$
            .pipe(takeUntilDestroyed(this.#destroyRef))
            .subscribe(() => {
                if (this.insights) {
                    this.#rebuildViewModel();
                }
            });
    }

    ngOnChanges(): void {
        this.#rebuildViewModel();
    }

    protected getInspectionPillClasses(tone: OverviewStateTone): string {
        switch (tone) {
            case 'critical':
                return 'spa-site-overview__inspection-pill--critical';
            case 'warning':
                return 'spa-site-overview__inspection-pill--warning';
            case 'primary':
                return 'spa-site-overview__inspection-pill--primary';
            default:
                return 'spa-site-overview__inspection-pill--neutral';
        }
    }

    protected getContextIconClasses(tone: OverviewStateTone): string {
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

    #rebuildViewModel(): void {
        this.vm = this.#createViewModel();
    }

    #createViewModel(): OverviewPerformanceViewModel {
        const performance = this.insights.performance;
        const scoreTone = this.#scoreTone(performance.score?.label);

        return {
            isReady: performance.status === 'READY',
            scoreLabel: this.#translate(
                `admin.site.dashboard.overview.insights.performance.score.${(
                    performance.score?.label ?? 'ATTENTION'
                ).toLowerCase()}`
            ),
            scoreChartOptions: this.#buildScoreChartOptions(
                performance.score?.value ?? 0,
                scoreTone
            ),
            metrics: this.#buildMetrics(),
            trendOptions: this.#buildTrendChartOptions(performance.trend),
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
            state: this.#buildState(performance.status),
        };
    }

    #buildMetrics(): OverviewPerformanceMetricViewModel[] {
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

    #buildState(
        status: SiteInsightsSummaryResponse['performance']['status']
    ): OverviewInsightStateViewModel {
        return {
            title: this.#buildStateTitle(status),
            description: this.#buildStateDescription(status),
            actionLabel: null,
            tone: status === 'ACCESS_ERROR' ? 'warning' : 'neutral',
            icon: this.#buildStateIcon(status),
        };
    }

    #buildScoreChartOptions(
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
            labels: [
                this.#translate(
                    'admin.site.dashboard.overview.insights.performance.score.label'
                ),
            ],
        };
    }

    #buildTrendChartOptions(
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

    #buildStateTitle(
        status: SiteInsightsSummaryResponse['performance']['status']
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

    #buildStateDescription(
        status: SiteInsightsSummaryResponse['performance']['status']
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

    #buildStateIcon(
        status: SiteInsightsSummaryResponse['performance']['status']
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

    #scoreTone(label?: string | null): OverviewStateTone {
        switch (label) {
            case 'HEALTHY':
                return 'primary';
            case 'CRITICAL':
                return 'critical';
            default:
                return 'warning';
        }
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
        const localeMap: Record<string, string> = { tr: 'tr-TR', en: 'en-US' };
        const lang = this.#transloco.getActiveLang().toLowerCase().split('-')[0];
        return localeMap[lang] ?? 'en-US';
    }
}
