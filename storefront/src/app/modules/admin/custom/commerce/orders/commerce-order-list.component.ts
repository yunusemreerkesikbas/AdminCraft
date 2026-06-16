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
import { CommerceAdminOrderRow } from '../models/commerce.types';
import { CommerceAdminOrderService } from '../services/commerce-admin.service';

@Component({
    selector: 'spa-commerce-order-list',
    standalone: true,
    imports: [
        TranslocoModule,
        AdminPageHeaderComponent,
        SpaAdminGridComponent,
        SpaAdminPaginatorComponent,
        SpaAdminSortDropdownComponent,
    ],
    templateUrl: './commerce-order-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SpaCommerceOrderListComponent extends BasePaginatedListComponent<
    CommerceAdminOrderRow,
    Partial<CommerceAdminOrderRow>,
    Partial<CommerceAdminOrderRow>
> {
    protected override service = inject(CommerceAdminOrderService);
    protected override store = new CrudStore<CommerceAdminOrderRow>();
    protected override defaultSort = 'createdAt,desc';
    protected override defaultPageSize = 20;

    readonly #router = inject(Router);
    readonly #route = inject(ActivatedRoute);

    protected readonly columnsSig = signal<GridColumn<CommerceAdminOrderRow>[]>([]);
    protected readonly actionsSig = signal<GridAction<CommerceAdminOrderRow>[]>([]);

    protected override onInit(): void {
        this.columnsSig.set([
            {
                key: 'orderNumber',
                label: 'admin.commerce.orders.fields.orderNumber',
                type: 'text',
                getSecondaryValue: (item) => item.orderUid,
                width: '220px',
            },
            {
                key: 'customerName',
                label: 'admin.commerce.orders.fields.customer',
                type: 'text',
                getValue: (item) => item.customerName || item.customerEmail,
                getSecondaryValue: (item) => item.customerEmail,
                width: '1fr',
            },
            {
                key: 'status',
                label: 'admin.commerce.orders.fields.status',
                type: 'status',
                width: '120px',
            },
            {
                key: 'total',
                label: 'admin.commerce.orders.fields.total',
                type: 'text',
                getValue: (item) => `${item.totals.total} ${item.currencyIso}`,
                width: '140px',
            },
            {
                key: 'itemCount',
                label: 'admin.commerce.orders.fields.itemCount',
                type: 'text',
                width: '96px',
                hideOn: 'md',
            },
            {
                key: 'requiresAttention',
                label: 'admin.commerce.orders.fields.attention',
                type: 'badge',
                getValue: (item) => item.requiresAttention ? 'ATTENTION' : 'OK',
                width: '120px',
                hideOn: 'lg',
            },
            {
                key: 'createdAt',
                label: 'admin.commerce.orders.fields.createdAt',
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

    protected onGridAction(event: GridActionEvent<CommerceAdminOrderRow>): void {
        if (event.action === 'view') {
            this.openDetail(event.item);
        }
    }

    protected openDetail(item: CommerceAdminOrderRow): void {
        this.#router.navigate([item.orderUid], { relativeTo: this.#route });
    }
}
