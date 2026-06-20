import { DatePipe, DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { AdminPageHeaderComponent } from '@shared/components/admin-page-header/admin-page-header.component';
import { SpaStatusBadgeComponent } from '@shared/components/custom-ui/spa-status-badge/spa-status-badge.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { EMPTY, Subject, switchMap, takeUntil } from 'rxjs';
import { CommerceOrderResolutionRequestRow } from '../models/commerce.types';
import { CommerceAdminOrderRequestService } from '../services/commerce-admin.service';

@Component({
    selector: 'spa-commerce-order-request-detail',
    standalone: true,
    imports: [
        DatePipe,
        DecimalPipe,
        TranslocoModule,
        RouterLink,
        ReactiveFormsModule,
        MatButtonModule,
        MatFormFieldModule,
        MatInputModule,
        MatProgressSpinnerModule,
        AdminPageHeaderComponent,
        SpaStatusBadgeComponent,
    ],
    templateUrl: './commerce-order-request-detail.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SpaCommerceOrderRequestDetailComponent implements OnInit, OnDestroy {
    readonly #route = inject(ActivatedRoute);
    readonly #service = inject(CommerceAdminOrderRequestService);
    readonly #notificationService = inject(NotificationService);
    readonly #fb = inject(FormBuilder);
    readonly #destroy$ = new Subject<void>();

    protected readonly requestSig = signal<CommerceOrderResolutionRequestRow | null>(null);
    protected readonly isLoadingSig = signal(false);
    protected readonly isUpdatingSig = signal(false);
    protected readonly decisionForm = this.#fb.group({
        decisionNote: ['', [Validators.maxLength(1000)]],
    });

    ngOnInit(): void {
        this.load();
    }

    ngOnDestroy(): void {
        this.#destroy$.next();
        this.#destroy$.complete();
    }

    protected decide(decision: 'APPROVE' | 'REJECT'): void {
        const request = this.requestSig();
        if (!request || request.status !== 'PENDING' || this.isUpdatingSig()) {
            return;
        }
        if (this.decisionForm.invalid) {
            this.decisionForm.markAllAsTouched();
            return;
        }
        this.isUpdatingSig.set(true);
        this.#service
            .decide(request.requestUid, {
                decision,
                decisionNote: this.decisionForm.controls.decisionNote.value || null,
            })
            .pipe(takeUntil(this.#destroy$))
            .subscribe({
                next: (updated) => {
                    this.requestSig.set(updated);
                    this.isUpdatingSig.set(false);
                    this.#notificationService.success(
                        'admin.commerce.messages.orderRequestDecided'
                    );
                },
                error: (error) => {
                    this.isUpdatingSig.set(false);
                    this.#notificationService.alert(
                        error?.error?.message || 'admin.commerce.messages.orderRequestDecisionFailed'
                    );
                },
            });
    }

    private load(): void {
        this.isLoadingSig.set(true);
        this.#route.paramMap
            .pipe(
                switchMap((params) => {
                    const requestUid = params.get('requestUid');
                    if (!requestUid) {
                        this.isLoadingSig.set(false);
                        this.#notificationService.alert(
                            'admin.commerce.messages.missingOrderRequestUid'
                        );
                        return EMPTY;
                    }
                    return this.#service.getRequest(requestUid);
                }),
                takeUntil(this.#destroy$)
            )
            .subscribe({
                next: (request) => {
                    this.requestSig.set(request);
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
