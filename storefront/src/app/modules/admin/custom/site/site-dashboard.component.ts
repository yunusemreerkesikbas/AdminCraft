import {
    ChangeDetectionStrategy,
    Component,
    OnDestroy,
    OnInit,
    computed,
    ViewChild,
    ViewEncapsulation,
    inject,
    signal,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { PageEvent } from '@angular/material/paginator';
import { MatTabsModule } from '@angular/material/tabs';
import { TranslocoModule } from '@jsverse/transloco';
import { NotificationService } from '@shared/notifications/notification.service';
import { UserService } from 'app/core/user/user.service';
import { Subject, catchError, forkJoin, of, takeUntil } from 'rxjs';
import { SiteService } from './site.service';
import {
    SecuritySettingsResponse,
    SITE_DASHBOARD_TABS,
    createSiteAnalyticsSummary,
    createSiteInsightsSummary,
    SiteAnalyticsSummaryResponse,
    SiteInsightsSummaryResponse,
    SiteActivityFeedResponse,
    SiteActivityTrendResponse,
    SiteDashboardTab,
    SiteOverviewResponse,
    SiteSettingsResponseDto,
    SiteTechnicalResponse,
} from './site.types';
import { SpaSiteOverviewComponent } from './tabs/overview/site-overview.component';
import { SpaSiteSecurityComponent } from './tabs/security/site-security.component';
import { SpaSiteSeoComponent } from './tabs/seo/site-seo.component';
import { SpaSiteSettingsComponent } from './tabs/settings/site-settings.component';
import { SpaSiteTechnicalComponent } from './tabs/technical/site-technical.component';

@Component({
    selector: 'spa-site-dashboard',
    templateUrl: './site-dashboard.component.html',
    styleUrls: ['./site-dashboard.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: true,
    imports: [
        MatButtonModule,
        MatIconModule,
        MatTabsModule,
        TranslocoModule,
        SpaSiteOverviewComponent,
        SpaSiteSettingsComponent,
        SpaSiteSeoComponent,
        SpaSiteTechnicalComponent,
        SpaSiteSecurityComponent,
    ],
})
export class SpaSiteDashboardComponent implements OnInit, OnDestroy {
    readonly #defaultActivitySort = 'createdAt,desc';
    readonly #defaultTrendSort = 'date,desc';
    readonly #trendPageSize = 7;
    readonly #siteService = inject(SiteService);
    readonly #userService = inject(UserService);
    readonly #destroy$ = new Subject<void>();
    #notificationService = inject(NotificationService);

    readonly userSig = this.#userService.user;
    readonly tabs = SITE_DASHBOARD_TABS;
    readonly selectedTabSig = signal<SiteDashboardTab>('overview');
    readonly overviewSig = signal<SiteOverviewResponse | null>(null);
    readonly analyticsSig = signal<SiteAnalyticsSummaryResponse>(
        createSiteAnalyticsSummary('ACCESS_ERROR')
    );
    readonly insightsSig = signal<SiteInsightsSummaryResponse>(
        createSiteInsightsSummary('ACCESS_ERROR')
    );
    readonly settingsSig = signal<SiteSettingsResponseDto | null>(null);
    readonly technicalSig = signal<SiteTechnicalResponse | null>(null);
    readonly securitySig = signal<SecuritySettingsResponse | null>(null);
    readonly activityFeedSig = signal<SiteActivityFeedResponse | null>(null);
    readonly trendSig = signal<SiteActivityTrendResponse | null>(null);
    readonly activityFeedPageIndexSig = signal<number>(0);
    readonly activityFeedPageSizeSig = signal<number>(10);
    readonly trendPageIndexSig = signal<number>(0);
    readonly activityFeedPageSizeOptions = [10, 20, 50];
    readonly activityFeedTotalElementsSig = computed(
        () => this.activityFeedSig()?.totalElements ?? 0
    );
    readonly tenantSig = this.#siteService.tenantSig;
    readonly modulesSig = this.#siteService.modulesSig;
    readonly loadingSig = signal<boolean>(true);

    @ViewChild(SpaSiteOverviewComponent)
    protected overviewComponent?: SpaSiteOverviewComponent;

    ngOnInit(): void {
        this.#loadData();
    }

    getTabIndex(tabId: SiteDashboardTab): number {
        return this.tabs.findIndex((tab) => tab.id === tabId);
    }

    onTabSelectionChange(index: number): void {
        const selectedTabItem = this.tabs[index];
        if (selectedTabItem) {
            this.selectedTabSig.set(selectedTabItem.id);
            this.#loadDataForTab(selectedTabItem.id);
        }
    }

    ngOnDestroy(): void {
        this.#destroy$.next();
        this.#destroy$.complete();
    }

    #loadDataForTab(tabId: SiteDashboardTab): void {
        switch (tabId) {
            case 'overview':
                if (!this.overviewSig()) {
                    this.#loadOverview();
                }
                break;
            case 'settings':
            case 'seo':
                if (!this.settingsSig()) {
                    this.#loadSettings();
                }
                break;
            case 'advanced':
                if (!this.technicalSig()) {
                    this.#loadTechnical();
                }
                if (!this.securitySig()) {
                    this.#loadSecurity();
                }
                break;
        }
    }

    onSettingsUpdated(settings: SiteSettingsResponseDto): void {
        this.settingsSig.set(settings);
    }

    onTechnicalUpdated(technical: SiteTechnicalResponse): void {
        this.technicalSig.set(technical);
    }

    onSecurityUpdated(security: SecuritySettingsResponse): void {
        this.securitySig.set(security);
    }

    refreshOverview(): void {
        this.#loadOverview();
        this.#loadAnalytics();
        this.#loadInsights();
        this.#loadActivityFeed();
        this.#loadTrend();
    }

    onActivityFeedPageChange(event: PageEvent): void {
        this.activityFeedPageIndexSig.set(event.pageIndex);
        this.activityFeedPageSizeSig.set(event.pageSize);
        this.#loadActivityFeed();
    }

    onTrendPageChange(direction: 'previous' | 'next'): void {
        const currentPageIndex = this.trendPageIndexSig();
        const totalPages = this.trendSig()?.totalPages ?? 0;

        if (direction === 'previous' && currentPageIndex + 1 < totalPages) {
            this.trendPageIndexSig.set(currentPageIndex + 1);
            this.#loadTrend();
        }

        if (direction === 'next' && currentPageIndex > 0) {
            this.trendPageIndexSig.set(currentPageIndex - 1);
            this.#loadTrend();
        }
    }

    onHeroPreview(): void {
        this.overviewComponent?.onPreview();
    }

    onHeroEnableMaintenance(): void {
        this.overviewComponent?.onEnableMaintenance();
    }

    onHeroDisableMaintenance(): void {
        this.overviewComponent?.onDisableMaintenance();
    }

    #loadData(): void {
        this.loadingSig.set(true);

        forkJoin({
            overview: this.#siteService.getOverview(),
            analytics: this.#siteService
                .getAnalyticsSummary()
                .pipe(
                    catchError(() =>
                        of(createSiteAnalyticsSummary('ACCESS_ERROR'))
                    )
                ),
            insights: this.#siteService
                .getInsightsSummary()
                .pipe(
                    catchError(() =>
                        of(createSiteInsightsSummary('ACCESS_ERROR'))
                    )
                ),
            settings: this.#siteService.getSiteSettings(),
            technical: this.#siteService.getTechnicalSettings(),
            security: this.#siteService.getSecuritySettings(),
            activityFeed: this.#siteService
                .getActivityFeed(this.#getActivityFeedRequest())
                .pipe(catchError(() => of(null))),
            trend: this.#siteService
                .getActivityTrend(this.#getTrendRequest())
                .pipe(catchError(() => of(null))),
            tenant: this.#siteService
                .getTenantDetail()
                .pipe(catchError(() => of(null))),
            modules: this.#siteService
                .getTenantModules()
                .pipe(catchError(() => of([]))),
        })
            .pipe(takeUntil(this.#destroy$))
            .subscribe({
                next: ({
                    overview,
                    analytics,
                    insights,
                    settings,
                    technical,
                    security,
                    activityFeed,
                    trend,
                    tenant,
                    modules,
                }) => {
                    this.overviewSig.set(overview);
                    this.analyticsSig.set(analytics);
                    this.insightsSig.set(insights);
                    this.settingsSig.set(settings);
                    this.technicalSig.set(technical);
                    this.securitySig.set(security);
                    this.activityFeedSig.set(activityFeed);
                    this.trendSig.set(trend);
                    this.tenantSig.set(tenant);
                    this.modulesSig.set(modules);
                    this.loadingSig.set(false);
                },
                error: (err) => {
                    this.loadingSig.set(false);
                    this.#notificationService.alert(
                        'admin.site.dashboard.messages.loadFailed'
                    );
                },
            });
    }

    #loadAnalytics(): void {
        this.#siteService
            .getAnalyticsSummary()
            .pipe(takeUntil(this.#destroy$))
            .subscribe({
                next: (data) => {
                    this.analyticsSig.set(data);
                },
                error: () => {
                    this.analyticsSig.set(
                        createSiteAnalyticsSummary('ACCESS_ERROR')
                    );
                },
            });
    }

    #loadInsights(): void {
        this.#siteService
            .getInsightsSummary()
            .pipe(takeUntil(this.#destroy$))
            .subscribe({
                next: (data) => {
                    this.insightsSig.set(data);
                },
                error: () => {
                    this.insightsSig.set(
                        createSiteInsightsSummary('ACCESS_ERROR')
                    );
                },
            });
    }

    #loadActivityFeed(): void {
        this.#siteService
            .getActivityFeed(this.#getActivityFeedRequest())
            .pipe(takeUntil(this.#destroy$))
            .subscribe({
                next: (data) => {
                    this.activityFeedSig.set(data);
                },
                error: () => {
                    this.#notificationService.alert(
                        'admin.site.dashboard.messages.loadFailed'
                    );
                },
            });
    }

    #loadTrend(): void {
        this.#siteService
            .getActivityTrend(this.#getTrendRequest())
            .pipe(takeUntil(this.#destroy$))
            .subscribe({
                next: (data) => {
                    this.trendSig.set(data);
                },
                error: () => {
                    this.#notificationService.alert(
                        'admin.site.dashboard.messages.loadFailed'
                    );
                },
            });
    }

    #getActivityFeedRequest(): {
        page: number;
        size: number;
        sort: string;
    } {
        return {
            page: this.activityFeedPageIndexSig(),
            size: this.activityFeedPageSizeSig(),
            sort: this.#defaultActivitySort,
        };
    }

    #getTrendRequest(): {
        page: number;
        size: number;
        sort: string;
    } {
        return {
            page: this.trendPageIndexSig(),
            size: this.#trendPageSize,
            sort: this.#defaultTrendSort,
        };
    }

    #loadOverview(): void {
        this.loadingSig.set(true);
        this.#siteService
            .getOverview()
            .pipe(takeUntil(this.#destroy$))
            .subscribe({
                next: (data) => {
                    this.overviewSig.set(data);
                    this.loadingSig.set(false);
                },
                error: (err) => {
                    this.loadingSig.set(false);
                    this.#notificationService.alert(
                        'admin.site.dashboard.messages.loadFailed'
                    );
                },
            });
    }

    #loadSettings(): void {
        this.loadingSig.set(true);
        this.#siteService
            .getSiteSettings()
            .pipe(takeUntil(this.#destroy$))
            .subscribe({
                next: (data) => {
                    this.settingsSig.set(data);
                    this.loadingSig.set(false);
                },
                error: (err) => {
                    this.loadingSig.set(false);
                    this.#notificationService.alert(
                        'admin.site.dashboard.messages.loadFailed'
                    );
                },
            });
    }

    #loadTechnical(): void {
        this.loadingSig.set(true);
        this.#siteService
            .getTechnicalSettings()
            .pipe(takeUntil(this.#destroy$))
            .subscribe({
                next: (data) => {
                    this.technicalSig.set(data);
                    this.loadingSig.set(false);
                },
                error: (err) => {
                    this.loadingSig.set(false);
                    this.#notificationService.alert(
                        'admin.site.dashboard.messages.loadFailed'
                    );
                },
            });
    }

    #loadSecurity(): void {
        this.loadingSig.set(true);
        this.#siteService
            .getSecuritySettings()
            .pipe(takeUntil(this.#destroy$))
            .subscribe({
                next: (data) => {
                    this.securitySig.set(data);
                    this.loadingSig.set(false);
                },
                error: (err) => {
                    this.loadingSig.set(false);
                    this.#notificationService.alert(
                        'admin.site.dashboard.messages.loadFailed'
                    );
                },
            });
    }
}
