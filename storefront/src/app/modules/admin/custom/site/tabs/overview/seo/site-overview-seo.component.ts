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
import { SiteInsightsSummaryResponse } from '../../../site.types';

type OverviewStateTone = 'primary' | 'neutral' | 'warning' | 'critical';
type OverviewDeltaTone = 'positive' | 'negative' | 'neutral';

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

@Component({
    selector: 'spa-site-overview-seo',
    templateUrl: './site-overview-seo.component.html',
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
export class SpaSiteOverviewSeoComponent implements OnChanges {
    readonly #destroyRef = inject(DestroyRef);
    readonly #dialog = inject(MatDialog);
    readonly #transloco = inject(TranslocoService);

    @Input({ required: true }) insights!: SiteInsightsSummaryResponse;
    @Input() tenantSubdomain: string | null = null;

    protected vm!: OverviewSeoViewModel;

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

    protected openMetricInfo(metric: OverviewSeoMetricCard['id']): void {
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

    #createViewModel(): OverviewSeoViewModel {
        const seo = this.insights.seo;

        return {
            isReady: seo.status === 'READY',
            cards: this.#buildCards(),
            chartOptions: this.#buildTrendChartOptions(seo.trend),
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
            inspectionItems: this.#buildInspectionItems(),
            state: this.#buildState(seo.status),
            setupUrl: this.#buildSetupUrl(),
        };
    }

    #buildCards(): OverviewSeoMetricCard[] {
        return this.insights.seo.cards.map((card) => ({
            id: card.metric,
            label: this.#translate(
                `admin.site.dashboard.overview.insights.seo.metrics.${card.metric}.label`
            ),
            value: this.#formatMetricValue(card.metric, card.value),
            detail: this.#translate(
                `admin.site.dashboard.overview.insights.seo.metrics.${card.metric}.detail`
            ),
            deltaLabel: this.#formatDelta(card.deltaPercentage),
            deltaTone: this.#resolveDeltaTone(
                card.deltaPercentage,
                card.deltaDirection
            ),
            icon: this.#metricIcon(card.metric),
        }));
    }

    #buildInspectionItems(): OverviewSeoInspectionItem[] {
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

    #buildState(
        status: SiteInsightsSummaryResponse['seo']['status']
    ): OverviewInsightStateViewModel {
        return {
            title: this.#buildStateTitle(status),
            description: this.#buildStateDescription(status),
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
            icon: this.#buildStateIcon(status),
        };
    }

    #buildTrendChartOptions(
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

    #buildSetupUrl(): string {
        const subdomain = this.tenantSubdomain?.trim();
        if (subdomain) {
            return `${window.location.origin}/config?subdomain=${encodeURIComponent(subdomain)}`;
        }

        return `${window.location.origin}/config`;
    }

    #buildStateTitle(status: SiteInsightsSummaryResponse['seo']['status']): string {
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

    #buildStateDescription(
        status: SiteInsightsSummaryResponse['seo']['status']
    ): string {
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

    #buildStateIcon(status: SiteInsightsSummaryResponse['seo']['status']): string {
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

    #formatMetricValue(metric: OverviewSeoMetricCard['id'], value: number): string {
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

    #buildMetricInfoModal(metric: OverviewSeoMetricCard['id']): ModalConfig<null> {
        return {
            type: 'info',
            title: this.#translate(
                `admin.site.dashboard.overview.insights.seo.metrics.${metric}.info.title`
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
        metric: OverviewSeoMetricCard['id']
    ): ModalSection[] {
        return ['summary', 'source', 'interpretation'].map((section) => ({
            type: 'info-box',
            title: this.#translate(
                `admin.site.dashboard.overview.insights.seo.infoSections.${section}`
            ),
            content: this.#translate(
                `admin.site.dashboard.overview.insights.seo.metrics.${metric}.info.${section}`
            ),
        }));
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
