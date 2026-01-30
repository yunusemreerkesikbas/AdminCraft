import {
    ChangeDetectionStrategy,
    Component,
    OnDestroy,
    OnInit,
    ViewEncapsulation,
    inject,
    signal,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { TranslocoModule } from '@jsverse/transloco';
import { UserService } from 'app/core/user/user.service';
import { Subject, forkJoin, takeUntil } from 'rxjs';
import { SiteService } from './site.service';
import {
    SITE_DASHBOARD_TABS,
    SiteDashboardTab,
    SiteOverviewResponse,
    SiteSettingsResponseDto,
    SiteTechnicalResponse,
} from './site.types';
import { SpaSiteAddressComponent } from './tabs/address/site-address.component';
import { SpaSiteGeneralComponent } from './tabs/general/site-general.component';
import { SpaSiteOverviewComponent } from './tabs/overview/site-overview.component';
import { SpaSiteSeoComponent } from './tabs/seo/site-seo.component';
import { SpaSiteSocialComponent } from './tabs/social/site-social.component';
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
        SpaSiteGeneralComponent,
        SpaSiteAddressComponent,
        SpaSiteSocialComponent,
        SpaSiteSeoComponent,
        SpaSiteTechnicalComponent,
    ],
})
export class SpaSiteDashboardComponent implements OnInit, OnDestroy {
    readonly #siteService = inject(SiteService);
    readonly #userService = inject(UserService);
    readonly #destroy$ = new Subject<void>();

    readonly userSig = this.#userService.user;
    readonly tabs = SITE_DASHBOARD_TABS;
    readonly selectedTabSig = signal<SiteDashboardTab>('overview');
    readonly overviewSig = signal<SiteOverviewResponse | null>(null);
    readonly settingsSig = signal<SiteSettingsResponseDto | null>(null);
    readonly technicalSig = signal<SiteTechnicalResponse | null>(null);
    readonly tenantSig = this.#siteService.tenantSig;
    readonly modulesSig = this.#siteService.modulesSig;
    readonly loadingSig = signal<boolean>(true);

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
            case 'general':
            case 'address':
            case 'social':
            case 'seo':
                if (!this.settingsSig()) {
                    this.#loadSettings();
                }
                break;
            case 'technical':
                if (!this.technicalSig()) {
                    this.#loadTechnical();
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

    refreshOverview(): void {
        this.#loadOverview();
    }

    #loadData(): void {
        this.loadingSig.set(true);

        forkJoin({
            overview: this.#siteService.getOverview(),
            settings: this.#siteService.getSiteSettings(),
            technical: this.#siteService.getTechnicalSettings(),
        })
            .pipe(takeUntil(this.#destroy$))
            .subscribe({
                next: ({ overview, settings, technical }) => {
                    this.overviewSig.set(overview);
                    this.settingsSig.set(settings);
                    this.technicalSig.set(technical);
                    this.loadingSig.set(false);
                },
                error: (err) => {
                    this.loadingSig.set(false);
                },
            });
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
                error: (err) => {},
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
                error: (err) => {},
            });
    }
}
