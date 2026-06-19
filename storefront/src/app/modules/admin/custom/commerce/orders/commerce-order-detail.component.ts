import { DatePipe, DecimalPipe } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    OnDestroy,
    OnInit,
    inject,
    signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { AdminPageHeaderComponent } from '@shared/components/admin-page-header/admin-page-header.component';
import { SpaStatusBadgeComponent } from '@shared/components/custom-ui/spa-status-badge/spa-status-badge.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { EMPTY, Subject, switchMap, takeUntil } from 'rxjs';
import {
    ChangeCommerceOrderStatusRequest,
    CommerceAddressSnapshot,
    CommerceAdminOrderDetail,
} from '../models/commerce.types';
import { CommerceAdminOrderService } from '../services/commerce-admin.service';

@Component({
    selector: 'spa-commerce-order-detail',
    standalone: true,
    imports: [
        DecimalPipe,
        DatePipe,
        TranslocoModule,
        RouterLink,
        MatButtonModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule,
        MatProgressSpinnerModule,
        ReactiveFormsModule,
        AdminPageHeaderComponent,
        SpaStatusBadgeComponent,
    ],
    templateUrl: './commerce-order-detail.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SpaCommerceOrderDetailComponent implements OnInit, OnDestroy {
    readonly #route = inject(ActivatedRoute);
    readonly #orderService = inject(CommerceAdminOrderService);
    readonly #notificationService = inject(NotificationService);
    readonly #fb = inject(FormBuilder);
    readonly #destroy$ = new Subject<void>();

    protected readonly detailSig = signal<CommerceAdminOrderDetail | null>(null);
    protected readonly isLoadingSig = signal(false);
    protected readonly isUpdatingSig = signal(false);
    protected readonly operationForm = this.#fb.group({
        carrierName: ['', [Validators.maxLength(100)]],
        trackingNumber: ['', [Validators.maxLength(100)]],
        trackingUrl: ['', [Validators.maxLength(500)]],
        internalNote: ['', [Validators.maxLength(1000)]],
    });

    ngOnInit(): void {
        this.isLoadingSig.set(true);
        this.#route.paramMap
            .pipe(
                switchMap((params) => {
                    const orderUid = params.get('orderUid');
                    if (!orderUid) {
                        this.isLoadingSig.set(false);
                        this.#notificationService.alert(
                            'admin.commerce.messages.missingOrderUid'
                        );
                        return EMPTY;
                    }
                    return this.#orderService.getOrder(orderUid);
                }),
                takeUntil(this.#destroy$)
            )
            .subscribe({
                next: (detail) => {
                    this.detailSig.set(detail);
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

    ngOnDestroy(): void {
        this.#destroy$.next();
        this.#destroy$.complete();
    }

    protected addressLine(address: CommerceAddressSnapshot): string {
        return [
            address.addressLine1,
            address.addressLine2,
            address.district,
            address.city,
            address.postalCode,
            address.countryIso,
        ]
            .filter(Boolean)
            .join(', ');
    }

    protected nextStatus(status: string): ChangeCommerceOrderStatusRequest['status'] | null {
        switch (status) {
            case 'PAID':
                return 'PREPARING';
            case 'PREPARING':
                return 'SHIPPED';
            case 'SHIPPED':
                return 'DELIVERED';
            default:
                return null;
        }
    }

    protected actionLabel(status: string): string {
        switch (status) {
            case 'PAID':
                return 'admin.commerce.orderDetail.actions.markPreparing';
            case 'PREPARING':
                return 'admin.commerce.orderDetail.actions.markShipped';
            case 'SHIPPED':
                return 'admin.commerce.orderDetail.actions.markDelivered';
            default:
                return 'admin.commerce.orderDetail.actions.complete';
        }
    }

    protected requiresShipment(status: string): boolean {
        return this.nextStatus(status) === 'SHIPPED';
    }

    protected updateStatus(detail: CommerceAdminOrderDetail): void {
        const status = this.nextStatus(detail.summary.status);
        if (!status || this.isUpdatingSig()) {
            return;
        }

        if (status === 'SHIPPED') {
            this.operationForm.controls.carrierName.addValidators(Validators.required);
            this.operationForm.controls.trackingNumber.addValidators(Validators.required);
        } else {
            this.operationForm.controls.carrierName.removeValidators(Validators.required);
            this.operationForm.controls.trackingNumber.removeValidators(Validators.required);
        }
        this.operationForm.controls.carrierName.updateValueAndValidity();
        this.operationForm.controls.trackingNumber.updateValueAndValidity();

        if (this.operationForm.invalid) {
            this.operationForm.markAllAsTouched();
            return;
        }

        const raw = this.operationForm.getRawValue();
        const request: ChangeCommerceOrderStatusRequest = {
            status,
            carrierName: raw.carrierName || null,
            trackingNumber: raw.trackingNumber || null,
            trackingUrl: raw.trackingUrl || null,
            internalNote: raw.internalNote || null,
        };

        this.isUpdatingSig.set(true);
        this.#orderService
            .updateOrderStatus(detail.summary.orderUid, request)
            .pipe(takeUntil(this.#destroy$))
            .subscribe({
                next: (updated) => {
                    this.detailSig.set(updated);
                    this.operationForm.reset();
                    this.isUpdatingSig.set(false);
                    this.#notificationService.success(
                        'admin.commerce.messages.statusUpdated'
                    );
                },
                error: (error) => {
                    this.isUpdatingSig.set(false);
                    this.#notificationService.alert(
                        error?.error?.message || 'admin.commerce.messages.statusUpdateFailed'
                    );
                },
            });
    }
}
