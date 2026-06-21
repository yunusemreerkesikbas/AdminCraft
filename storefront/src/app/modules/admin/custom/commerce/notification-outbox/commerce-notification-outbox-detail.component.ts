import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { AdminPageHeaderComponent } from '@shared/components/admin-page-header/admin-page-header.component';
import { SpaStatusBadgeComponent } from '@shared/components/custom-ui/spa-status-badge/spa-status-badge.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { EMPTY, Subject, switchMap, takeUntil } from 'rxjs';
import { CommerceNotificationOutboxRow } from '../models/commerce.types';
import { CommerceAdminNotificationOutboxService } from '../services/commerce-admin.service';

@Component({
    selector: 'spa-commerce-notification-outbox-detail',
    standalone: true,
    imports: [
        DatePipe,
        TranslocoModule,
        RouterLink,
        MatButtonModule,
        MatIconModule,
        MatProgressSpinnerModule,
        AdminPageHeaderComponent,
        SpaStatusBadgeComponent,
    ],
    templateUrl: './commerce-notification-outbox-detail.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SpaCommerceNotificationOutboxDetailComponent implements OnInit, OnDestroy {
    readonly #route = inject(ActivatedRoute);
    readonly #service = inject(CommerceAdminNotificationOutboxService);
    readonly #notificationService = inject(NotificationService);
    readonly #destroy$ = new Subject<void>();

    protected readonly outboxSig = signal<CommerceNotificationOutboxRow | null>(null);
    protected readonly isLoadingSig = signal(false);
    protected readonly isRetryingSig = signal(false);

    ngOnInit(): void {
        this.load();
    }

    ngOnDestroy(): void {
        this.#destroy$.next();
        this.#destroy$.complete();
    }

    protected retry(): void {
        const outbox = this.outboxSig();
        if (!outbox?.retryAllowed || this.isRetryingSig()) {
            return;
        }
        this.isRetryingSig.set(true);
        this.#service
            .retry(outbox.outboxUid)
            .pipe(takeUntil(this.#destroy$))
            .subscribe({
                next: (updated) => {
                    this.outboxSig.set(updated);
                    this.isRetryingSig.set(false);
                    this.#notificationService.success(
                        'admin.commerce.messages.notificationRetryCompleted'
                    );
                },
                error: (error) => {
                    this.isRetryingSig.set(false);
                    this.#notificationService.alert(
                        error?.error?.message || 'admin.commerce.messages.notificationRetryFailed'
                    );
                },
            });
    }

    private load(): void {
        this.isLoadingSig.set(true);
        this.#route.paramMap
            .pipe(
                switchMap((params) => {
                    const outboxUid = params.get('outboxUid');
                    if (!outboxUid) {
                        this.isLoadingSig.set(false);
                        this.#notificationService.alert(
                            'admin.commerce.messages.missingNotificationOutboxUid'
                        );
                        return EMPTY;
                    }
                    return this.#service.getOutbox(outboxUid);
                }),
                takeUntil(this.#destroy$)
            )
            .subscribe({
                next: (outbox) => {
                    this.outboxSig.set(outbox);
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
