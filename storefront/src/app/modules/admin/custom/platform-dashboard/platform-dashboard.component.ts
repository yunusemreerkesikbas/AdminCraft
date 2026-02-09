import {
    ChangeDetectionStrategy,
    Component,
    OnDestroy,
    OnInit,
    inject,
    signal,
} from '@angular/core';
import { DatePipe, NgClass } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { NotificationService } from '@shared/notifications/notification.service';
import { Subject, catchError, retry, takeUntil, throwError } from 'rxjs';
import { StatusUtils, TenantStatus, SyncJobStatus } from '@shared/types/platform.types';
import { PlatformDashboardService } from './platform-dashboard.service';
import { PlatformDashboardResponse, RecentJobDto } from './platform-dashboard.types';
import { JobErrorDialogComponent, JobErrorDialogData } from './job-error-dialog.component';

@Component({
    selector: 'spa-platform-dashboard',
    templateUrl: './platform-dashboard.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: true,
    imports: [
        MatButtonModule,
        MatIconModule,
        MatTooltipModule,
        TranslocoModule,
        DatePipe,
        NgClass,
    ],
})
export class SpaPlatformDashboardComponent implements OnInit, OnDestroy {
    readonly #service = inject(PlatformDashboardService);
    readonly #notify = inject(NotificationService);
    readonly #router = inject(Router);
    readonly #transloco = inject(TranslocoService);
    readonly #dialog = inject(MatDialog);
    readonly #destroy$ = new Subject<void>();

    readonly dashboardSig = signal<PlatformDashboardResponse | null>(null);
    readonly loadingSig = signal<boolean>(true);

    ngOnInit(): void {
        this.#loadData();
    }

    onRefresh(): void {
        this.#notify.info('admin.common.messages.refreshing');
        this.#loadData();
    }

    viewTenant(tenantId: number): void {
        const lang = this.#transloco.getActiveLang();
        this.#router.navigate(['/', lang, 'tenants', tenantId]);
    }

    getStatusClass(status: TenantStatus): string {
        return StatusUtils.getTenantStatusClass(status);
    }

    getJobStatusClass(status: SyncJobStatus): string {
        return StatusUtils.getJobStatusClass(status);
    }

    showJobError(job: RecentJobDto): void {
        if (!job.error) return;

        this.#dialog.open<JobErrorDialogComponent, JobErrorDialogData>(JobErrorDialogComponent, {
            width: '600px',
            maxWidth: '90vw',
            data: {
                tenantSubdomain: job.tenantSubdomain,
                type: job.type,
                error: job.error,
                createdAt: job.createdAt,
            },
        });
    }

    ngOnDestroy(): void {
        this.#destroy$.next();
        this.#destroy$.complete();
    }

    #loadData(): void {
        this.loadingSig.set(true);
        this.#service
            .getDashboard()
            .pipe(
                retry({ count: 2, delay: 1000 }),
                takeUntil(this.#destroy$),
                catchError((err) => {
                    console.error('[PlatformDashboard] Load failed:', err);
                    this.loadingSig.set(false);
                    this.#notify.alert('admin.platform.dashboard.messages.loadFailed');
                    return throwError(() => err);
                })
            )
            .subscribe({
                next: (data) => {
                    this.dashboardSig.set(data);
                    this.loadingSig.set(false);
                },
            });
    }
}
