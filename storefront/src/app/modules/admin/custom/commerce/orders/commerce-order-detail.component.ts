import { DecimalPipe } from '@angular/common';
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
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { AdminPageHeaderComponent } from '@shared/components/admin-page-header/admin-page-header.component';
import { SpaStatusBadgeComponent } from '@shared/components/custom-ui/spa-status-badge/spa-status-badge.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { EMPTY, Subject, switchMap, takeUntil } from 'rxjs';
import {
    CommerceAddressSnapshot,
    CommerceAdminOrderDetail,
} from '../models/commerce.types';
import { CommerceAdminOrderService } from '../services/commerce-admin.service';

@Component({
    selector: 'spa-commerce-order-detail',
    standalone: true,
    imports: [
        DecimalPipe,
        TranslocoModule,
        RouterLink,
        MatButtonModule,
        MatIconModule,
        MatProgressSpinnerModule,
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
    readonly #destroy$ = new Subject<void>();

    protected readonly detailSig = signal<CommerceAdminOrderDetail | null>(null);
    protected readonly isLoadingSig = signal(false);

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
}
