import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
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
import { SpaContactRequestDetailDialogComponent } from './dialogs/contact-request-detail-dialog/contact-request-detail-dialog.component';
import { ContactRequestAdminService } from './platform-contact-request-admin.service';
import { PlatformContactRequestRow } from './contact-request.types';

@Component({
    selector: 'spa-contact-requests-list',
    standalone: true,
    imports: [
        TranslocoModule,
        AdminPageHeaderComponent,
        SpaAdminGridComponent,
        SpaAdminPaginatorComponent,
        SpaAdminSortDropdownComponent,
    ],
    templateUrl: './contact-requests-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SpaContactRequestsListComponent extends BasePaginatedListComponent<
    PlatformContactRequestRow,
    Partial<PlatformContactRequestRow>,
    Partial<PlatformContactRequestRow>
> {
    protected override service = inject(ContactRequestAdminService);
    protected override store = new CrudStore<PlatformContactRequestRow>();
    readonly #matDialog = inject(MatDialog);
    protected override defaultSort = 'createdAt,desc';
    protected override defaultPageSize = 20;

    protected readonly columnsSig = signal<GridColumn<PlatformContactRequestRow>[]>([]);
    protected readonly actionsSig = signal<GridAction<PlatformContactRequestRow>[]>([]);

    protected override onInit(): void {
        this.actionsSig.set([
            {
                icon: 'heroicons_outline:eye',
                label: 'admin.common.grid.details',
                action: 'view',
            },
        ]);

        this.columnsSig.set([
            {
                key: 'fullName',
                label: 'admin.platform.contactRequests.fields.fullName',
                type: 'text',
                width: '160px',
            },
            {
                key: 'subject',
                label: 'admin.platform.contactRequests.fields.subject',
                type: 'text',
                width: '220px',
            },
            {
                key: 'messagePreview',
                label: 'admin.platform.contactRequests.fields.message',
                type: 'text',
                width: '1fr',
            },
            {
                key: 'locale',
                label: 'admin.platform.contactRequests.fields.locale',
                type: 'text',
                width: '72px',
                hideOn: 'md',
            },
            {
                key: 'createdAt',
                label: 'admin.platform.contactRequests.fields.createdAt',
                type: 'date',
                width: '160px',
            },
        ]);
    }

    protected onGridAction(event: GridActionEvent<PlatformContactRequestRow>): void {
        if (event.action !== 'view') {
            return;
        }
        this.#matDialog.open(SpaContactRequestDetailDialogComponent, {
            width: '760px',
            maxHeight: '90vh',
            panelClass: 'spa-compact-dialog',
            autoFocus: false,
            data: event.item,
        });
    }
}
