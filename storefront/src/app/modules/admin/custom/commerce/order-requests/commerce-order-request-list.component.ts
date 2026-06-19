import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BasePaginatedListComponent } from '@core/crud/base-paginated-list.component';
import { CrudStore } from '@core/crud/crud-store';
import { TranslocoModule } from '@jsverse/transloco';
import { AdminPageHeaderComponent } from '@shared/components/admin-page-header/admin-page-header.component';
import {
    GridAction,
    GridActionEvent,
    GridColumn,
    SpaAdminGridComponent,
} from '@shared/components/spa-admin-grid';
import { SpaAdminPaginatorComponent } from '@shared/components/spa-admin-paginator/spa-admin-paginator.component';
import { SpaAdminSortDropdownComponent } from '@shared/components/spa-admin-sort-dropdown/spa-admin-sort-dropdown.component';
import { CommerceOrderResolutionRequestRow } from '../models/commerce.types';
import { CommerceAdminOrderRequestService } from '../services/commerce-admin.service';

@Component({
    selector: 'spa-commerce-order-request-list',
    standalone: true,
    imports: [
        TranslocoModule,
        AdminPageHeaderComponent,
        SpaAdminGridComponent,
        SpaAdminPaginatorComponent,
        SpaAdminSortDropdownComponent,
    ],
    templateUrl: './commerce-order-request-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SpaCommerceOrderRequestListComponent extends BasePaginatedListComponent<
    CommerceOrderResolutionRequestRow,
    Partial<CommerceOrderResolutionRequestRow>,
    Partial<CommerceOrderResolutionRequestRow>
> {
    protected override service = inject(CommerceAdminOrderRequestService);
    protected override store = new CrudStore<CommerceOrderResolutionRequestRow>();
    protected override defaultSort = 'createdAt,desc';
    protected override defaultPageSize = 20;

    readonly #router = inject(Router);
    readonly #route = inject(ActivatedRoute);

    protected readonly columnsSig = signal<GridColumn<CommerceOrderResolutionRequestRow>[]>([]);
    protected readonly actionsSig = signal<GridAction<CommerceOrderResolutionRequestRow>[]>([]);

    protected override onInit(): void {
        this.columnsSig.set([
            {
                key: 'orderNumber',
                label: 'admin.commerce.orderRequests.fields.orderNumber',
                type: 'text',
                getSecondaryValue: (item) => item.requestUid,
                width: '220px',
            },
            {
                key: 'customerEmail',
                label: 'admin.commerce.orderRequests.fields.customer',
                type: 'text',
                width: '1fr',
            },
            {
                key: 'type',
                label: 'admin.commerce.orderRequests.fields.type',
                type: 'badge',
                width: '130px',
            },
            {
                key: 'status',
                label: 'admin.commerce.orderRequests.fields.status',
                type: 'status',
                width: '130px',
            },
            {
                key: 'refundStatus',
                label: 'admin.commerce.orderRequests.fields.refundStatus',
                type: 'badge',
                width: '150px',
            },
            {
                key: 'orderTotal',
                label: 'admin.commerce.orderRequests.fields.total',
                type: 'text',
                getValue: (item) => `${item.orderTotal} ${item.currencyIso}`,
                width: '130px',
            },
            {
                key: 'createdAt',
                label: 'admin.commerce.orderRequests.fields.createdAt',
                type: 'date',
                width: '140px',
            },
        ]);
        this.actionsSig.set([
            {
                icon: 'heroicons_outline:eye',
                label: 'admin.common.grid.details',
                action: 'view',
            },
        ]);
    }

    protected onGridAction(event: GridActionEvent<CommerceOrderResolutionRequestRow>): void {
        if (event.action === 'view') {
            this.#router.navigate([event.item.requestUid], { relativeTo: this.#route });
        }
    }
}
