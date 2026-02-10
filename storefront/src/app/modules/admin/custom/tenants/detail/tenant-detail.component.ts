import {
    ChangeDetectionStrategy,
    Component,
    OnDestroy,
    OnInit,
    inject,
    signal,
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { NotificationService } from '@shared/notifications/notification.service';
import { TenantModule } from 'app/core/tenant/tenant.types';
import { Subject, forkJoin, takeUntil } from 'rxjs';
import { TenantsService } from '../tenants.service';
import { ProvisioningJobResponse, TenantDetailResponse } from '../tenants.types';
import { TENANT_DETAIL_TABS, TenantDetailTab } from './tenant-detail.types';
import { SpaTenantOverviewComponent } from './tabs/tenant-overview.component';
import { SpaTenantModulesComponent } from './tabs/tenant-modules.component';
import { SpaTenantJobsComponent } from './tabs/tenant-jobs.component';

@Component({
    selector: 'spa-tenant-detail',
    templateUrl: './tenant-detail.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: true,
    imports: [
        MatButtonModule,
        MatIconModule,
        MatTabsModule,
        TranslocoModule,
        SpaTenantOverviewComponent,
        SpaTenantModulesComponent,
        SpaTenantJobsComponent,
    ],
})
export class SpaTenantDetailComponent implements OnInit, OnDestroy {
    readonly #route = inject(ActivatedRoute);
    readonly #router = inject(Router);
    readonly #service = inject(TenantsService);
    readonly #notify = inject(NotificationService);
    readonly #transloco = inject(TranslocoService);
    readonly #destroy$ = new Subject<void>();

    readonly tabs = TENANT_DETAIL_TABS;
    readonly selectedTabSig = signal<TenantDetailTab>('overview');
    readonly tenantSig = signal<TenantDetailResponse | null>(null);
    readonly modulesSig = signal<TenantModule[]>([]);
    readonly jobsSig = signal<ProvisioningJobResponse[]>([]);
    readonly loadingSig = signal<boolean>(true);

    ngOnInit(): void {
        const id = Number(this.#route.snapshot.paramMap.get('id'));
        if (!id || isNaN(id)) {
            this.goBack();
            return;
        }
        this.#loadData(id);
    }

    goBack(): void {
        const lang = this.#transloco.getActiveLang();
        this.#router.navigate(['/', lang, 'tenants']);
    }

    getTabIndex(tabId: TenantDetailTab): number {
        return this.tabs.findIndex((tab) => tab.id === tabId);
    }

    onTabSelectionChange(index: number): void {
        const selectedTabItem = this.tabs[index];
        if (selectedTabItem) {
            this.selectedTabSig.set(selectedTabItem.id);
        }
    }

    ngOnDestroy(): void {
        this.#destroy$.next();
        this.#destroy$.complete();
    }

    #loadData(tenantId: number): void {
        this.loadingSig.set(true);
        forkJoin({
            tenant: this.#service.getTenantDetail(tenantId),
            modules: this.#service.getTenantModules(tenantId),
            jobs: this.#service.getTenantProvisioningJobs(tenantId),
        })
            .pipe(takeUntil(this.#destroy$))
            .subscribe({
                next: ({ tenant, modules, jobs }) => {
                    this.tenantSig.set(tenant);
                    this.modulesSig.set(modules);
                    this.jobsSig.set(jobs);
                    this.loadingSig.set(false);
                },
                error: () => {
                    this.loadingSig.set(false);
                    this.#notify.alert('admin.common.errors.loadFailed');
                },
            });
    }
}
