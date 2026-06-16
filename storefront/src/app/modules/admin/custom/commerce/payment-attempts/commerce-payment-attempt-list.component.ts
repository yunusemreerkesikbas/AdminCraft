import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { BasePaginatedListComponent } from '@core/crud/base-paginated-list.component';
import { CrudStore } from '@core/crud/crud-store';
import { TranslocoModule } from '@jsverse/transloco';
import { AdminPageHeaderComponent } from '@shared/components/admin-page-header/admin-page-header.component';
import {
    GridColumn,
    SpaAdminGridComponent,
} from '@shared/components/spa-admin-grid';
import { SpaAdminPaginatorComponent } from '@shared/components/spa-admin-paginator/spa-admin-paginator.component';
import { SpaAdminSortDropdownComponent } from '@shared/components/spa-admin-sort-dropdown/spa-admin-sort-dropdown.component';
import { CommerceAdminPaymentAttemptRow } from '../models/commerce.types';
import { CommerceAdminPaymentAttemptService } from '../services/commerce-admin.service';

@Component({
    selector: 'spa-commerce-payment-attempt-list',
    standalone: true,
    imports: [
        TranslocoModule,
        AdminPageHeaderComponent,
        SpaAdminGridComponent,
        SpaAdminPaginatorComponent,
        SpaAdminSortDropdownComponent,
    ],
    templateUrl: './commerce-payment-attempt-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SpaCommercePaymentAttemptListComponent extends BasePaginatedListComponent<
    CommerceAdminPaymentAttemptRow,
    Partial<CommerceAdminPaymentAttemptRow>,
    Partial<CommerceAdminPaymentAttemptRow>
> {
    protected override service = inject(CommerceAdminPaymentAttemptService);
    protected override store = new CrudStore<CommerceAdminPaymentAttemptRow>();
    protected override defaultSort = 'createdAt,desc';
    protected override defaultPageSize = 20;

    protected readonly columnsSig = signal<GridColumn<CommerceAdminPaymentAttemptRow>[]>([]);

    protected override onInit(): void {
        this.columnsSig.set([
            {
                key: 'attemptUid',
                label: 'admin.commerce.paymentAttempts.fields.attempt',
                type: 'text',
                getSecondaryValue: (item) => item.checkoutUid,
                width: '220px',
            },
            {
                key: 'customerName',
                label: 'admin.commerce.paymentAttempts.fields.customer',
                type: 'text',
                getSecondaryValue: (item) => item.customerEmail,
                width: '1fr',
            },
            {
                key: 'status',
                label: 'admin.commerce.paymentAttempts.fields.status',
                type: 'status',
                width: '130px',
            },
            {
                key: 'provider',
                label: 'admin.commerce.paymentAttempts.fields.provider',
                type: 'badge',
                width: '110px',
            },
            {
                key: 'total',
                label: 'admin.commerce.paymentAttempts.fields.total',
                type: 'text',
                getValue: (item) => `${item.totals.total} ${item.currencyIso}`,
                width: '140px',
            },
            {
                key: 'failureCode',
                label: 'admin.commerce.paymentAttempts.fields.failureCode',
                type: 'text',
                getValue: (item) => item.failureCode || '-',
                width: '150px',
                hideOn: 'lg',
            },
            {
                key: 'createdAt',
                label: 'admin.commerce.paymentAttempts.fields.createdAt',
                type: 'date',
                width: '140px',
            },
        ]);
    }
}
