import { CommonModule } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    OnDestroy,
    OnInit,
    inject,
    signal,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { AdminPageHeaderComponent } from '@shared/components/admin-page-header/admin-page-header.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { Subject, takeUntil } from 'rxjs';
import { CommerceAdminDashboard } from '../models/commerce.types';
import { CommerceAdminService } from '../services/commerce-admin.service';

@Component({
    selector: 'spa-commerce-dashboard',
    standalone: true,
    imports: [
        CommonModule,
        TranslocoModule,
        RouterLink,
        MatButtonModule,
        MatIconModule,
        MatProgressSpinnerModule,
        AdminPageHeaderComponent,
    ],
    templateUrl: './commerce-dashboard.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SpaCommerceDashboardComponent implements OnInit, OnDestroy {
    readonly #commerceAdminService = inject(CommerceAdminService);
    readonly #notificationService = inject(NotificationService);
    readonly #destroy$ = new Subject<void>();

    protected readonly dashboardSig = signal<CommerceAdminDashboard | null>(null);
    protected readonly isLoadingSig = signal(false);

    ngOnInit(): void {
        this.loadDashboard();
    }

    ngOnDestroy(): void {
        this.#destroy$.next();
        this.#destroy$.complete();
    }

    protected loadDashboard(): void {
        this.isLoadingSig.set(true);
        this.#commerceAdminService
            .getDashboard()
            .pipe(takeUntil(this.#destroy$))
            .subscribe({
                next: (dashboard) => {
                    this.dashboardSig.set(dashboard);
                    this.isLoadingSig.set(false);
                },
                error: (error) => {
                    this.isLoadingSig.set(false);
                    this.#notificationService.alert(
                        error?.error?.message || 'admin.commerce.messages.loadFailed'
                    );
                },
            });
    }
}
