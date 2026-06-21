import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { PageWithSort } from '@core/crud/api.types';
import { BasePaginatedListComponent } from '@core/crud/base-paginated-list.component';
import { CrudStore } from '@core/crud/crud-store';
import { TranslocoModule } from '@jsverse/transloco';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AdminPageHeaderComponent } from '@shared/components/admin-page-header/admin-page-header.component';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import {
    SpaSelectComponent,
    SpaSelectOption,
} from '@shared/components/custom-ui/spa-select/spa-select.component';
import {
    GridAction,
    GridActionEvent,
    GridColumn,
    SpaAdminGridComponent,
} from '@shared/components/spa-admin-grid';
import { SpaAdminPaginatorComponent } from '@shared/components/spa-admin-paginator/spa-admin-paginator.component';
import { SpaAdminSortDropdownComponent } from '@shared/components/spa-admin-sort-dropdown/spa-admin-sort-dropdown.component';
import { Observable } from 'rxjs';
import { CommerceNotificationOutboxRow } from '../models/commerce.types';
import { CommerceAdminNotificationOutboxService } from '../services/commerce-admin.service';

@Component({
    selector: 'spa-commerce-notification-outbox-list',
    standalone: true,
    imports: [
        TranslocoModule,
        FormsModule,
        MatButtonModule,
        MatIconModule,
        AdminPageHeaderComponent,
        SpaInputComponent,
        SpaSelectComponent,
        SpaAdminGridComponent,
        SpaAdminPaginatorComponent,
        SpaAdminSortDropdownComponent,
    ],
    templateUrl: './commerce-notification-outbox-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SpaCommerceNotificationOutboxListComponent extends BasePaginatedListComponent<
    CommerceNotificationOutboxRow,
    Partial<CommerceNotificationOutboxRow>,
    Partial<CommerceNotificationOutboxRow>
> {
    protected override service = inject(CommerceAdminNotificationOutboxService);
    protected override store = new CrudStore<CommerceNotificationOutboxRow>();
    protected override defaultSort = 'createdAt,desc';
    protected override defaultPageSize = 20;

    readonly #router = inject(Router);
    readonly #route = inject(ActivatedRoute);

    protected readonly columnsSig = signal<GridColumn<CommerceNotificationOutboxRow>[]>([]);
    protected readonly actionsSig = signal<GridAction<CommerceNotificationOutboxRow>[]>([]);
    protected readonly statusFilterSig = signal<string | null>(null);
    protected readonly eventTypeFilterSig = signal<string | null>(null);
    protected readonly aggregateUidFilterSig = signal<string>('');
    protected readonly aggregateUidDraftSig = signal<string>('');
    protected readonly statusOptions: SpaSelectOption<string>[] = [
        { value: 'FAILED', labelKey: 'admin.commerce.notificationOutbox.status.failed' },
        { value: 'PENDING', labelKey: 'admin.commerce.notificationOutbox.status.pending' },
        { value: 'SENT', labelKey: 'admin.commerce.notificationOutbox.status.sent' },
    ];
    protected readonly eventTypeOptions: SpaSelectOption<string>[] = [
        { value: 'ORDER_PAID', labelKey: 'admin.commerce.notificationOutbox.events.orderPaid' },
        { value: 'ORDER_SHIPPED', labelKey: 'admin.commerce.notificationOutbox.events.orderShipped' },
        {
            value: 'ORDER_REQUEST_CREATED',
            labelKey: 'admin.commerce.notificationOutbox.events.requestCreated',
        },
        {
            value: 'ORDER_REQUEST_APPROVED',
            labelKey: 'admin.commerce.notificationOutbox.events.requestApproved',
        },
        {
            value: 'ORDER_REQUEST_REJECTED',
            labelKey: 'admin.commerce.notificationOutbox.events.requestRejected',
        },
    ];

    protected override onInit(): void {
        this.columnsSig.set([
            {
                key: 'eventType',
                label: 'admin.commerce.notificationOutbox.fields.eventType',
                type: 'text',
                getSecondaryValue: (item) => item.outboxUid,
                width: '220px',
            },
            {
                key: 'recipientEmail',
                label: 'admin.commerce.notificationOutbox.fields.recipient',
                type: 'text',
                getSecondaryValue: (item) => item.subject,
                width: '1fr',
            },
            {
                key: 'status',
                label: 'admin.commerce.notificationOutbox.fields.status',
                type: 'status',
                width: '120px',
            },
            {
                key: 'attemptCount',
                label: 'admin.commerce.notificationOutbox.fields.attempts',
                type: 'text',
                getValue: (item) => `${item.attemptCount}/${item.maxRetryAttempts + 1}`,
                width: '110px',
            },
            {
                key: 'nextRetryAt',
                label: 'admin.commerce.notificationOutbox.fields.nextRetryAt',
                type: 'date',
                width: '150px',
                hideOn: 'lg',
            },
            {
                key: 'errorMessage',
                label: 'admin.commerce.notificationOutbox.fields.error',
                type: 'text',
                getValue: (item) => item.errorMessage || '-',
                width: '180px',
                hideOn: 'lg',
            },
            {
                key: 'createdAt',
                label: 'admin.commerce.notificationOutbox.fields.createdAt',
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

    protected onGridAction(event: GridActionEvent<CommerceNotificationOutboxRow>): void {
        if (event.action === 'view') {
            this.openDetail(event.item);
        }
    }

    protected openDetail(item: CommerceNotificationOutboxRow): void {
        this.#router.navigate([item.outboxUid], { relativeTo: this.#route });
    }

    protected onStatusFilterChange(status: string | null): void {
        this.statusFilterSig.set(status);
        this.reloadFromFirstPage();
    }

    protected onEventTypeFilterChange(eventType: string | null): void {
        this.eventTypeFilterSig.set(eventType);
        this.reloadFromFirstPage();
    }

    protected onAggregateUidFilterApply(value: string | number | null): void {
        this.aggregateUidFilterSig.set(String(value ?? this.aggregateUidDraftSig()).trim());
        this.reloadFromFirstPage();
    }

    protected clearFilters(): void {
        this.statusFilterSig.set(null);
        this.eventTypeFilterSig.set(null);
        this.aggregateUidFilterSig.set('');
        this.aggregateUidDraftSig.set('');
        this.reloadFromFirstPage();
    }

    protected override fetchItems(): Observable<PageWithSort<CommerceNotificationOutboxRow>> {
        const params: {
            page: number;
            size: number;
            sort: string;
            search?: string;
            status?: string;
            eventType?: string;
            aggregateUid?: string;
        } = {
            page: this.pageIndexSig(),
            size: this.pageSizeSig(),
            sort: this.sortCodeSig(),
        };
        const search = this.searchQuerySig().trim();
        if (search.length >= this.minSearchChars) {
            params.search = search;
        }
        if (this.statusFilterSig()) {
            params.status = this.statusFilterSig() ?? undefined;
        }
        if (this.eventTypeFilterSig()) {
            params.eventType = this.eventTypeFilterSig() ?? undefined;
        }
        if (this.aggregateUidFilterSig()) {
            params.aggregateUid = this.aggregateUidFilterSig();
        }
        return this.service.listPaged(params) as Observable<PageWithSort<CommerceNotificationOutboxRow>>;
    }

    private reloadFromFirstPage(): void {
        this.pageIndexSig.set(0);
        this.loadItems();
    }
}
