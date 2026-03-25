import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { BasePaginatedListComponent } from '@core/crud/base-paginated-list.component';
import { CrudStore } from '@core/crud/crud-store';
import { TranslocoModule } from '@jsverse/transloco';
import { AdminPageHeaderComponent } from 'app/shared/components/admin-page-header/admin-page-header.component';
import {
    GridAction,
    GridActionEvent,
    GridColumn,
    SpaAdminGridComponent,
} from '@shared/components/spa-admin-grid';
import { SpaAdminPaginatorComponent } from '@shared/components/spa-admin-paginator/spa-admin-paginator.component';
import { SpaAdminSortDropdownComponent } from '@shared/components/spa-admin-sort-dropdown/spa-admin-sort-dropdown.component';
import { SpaDemoRequestDetailDialogComponent } from './dialogs/demo-request-detail-dialog/demo-request-detail-dialog.component';
import { PlatformDemoRequestAdminService } from './platform-demo-request-admin.service';
import { PlatformDemoRequestRow } from './demo-request.types';

@Component({
    selector: 'spa-demo-requests-list',
    standalone: true,
    imports: [
        TranslocoModule,
        AdminPageHeaderComponent,
        SpaAdminGridComponent,
        SpaAdminPaginatorComponent,
        SpaAdminSortDropdownComponent,
    ],
    templateUrl: './demo-requests-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SpaDemoRequestsListComponent extends BasePaginatedListComponent<
    PlatformDemoRequestRow,
    Partial<PlatformDemoRequestRow>,
    Partial<PlatformDemoRequestRow>
> {
    protected override service = inject(PlatformDemoRequestAdminService);
    protected override store = new CrudStore<PlatformDemoRequestRow>();
    readonly #matDialog = inject(MatDialog);
    protected override defaultSort = 'createdAt,desc';
    protected override defaultPageSize = 20;

    protected readonly columnsSig = signal<GridColumn<PlatformDemoRequestRow>[]>([]);
    protected readonly actionsSig = signal<GridAction<PlatformDemoRequestRow>[]>([]);

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
                label: 'admin.platform.demoRequests.fields.fullName',
                type: 'text',
                width: '140px',
            },
            {
                key: 'email',
                label: 'admin.platform.demoRequests.fields.email',
                type: 'text',
                width: '180px',
            },
            {
                key: 'phone',
                label: 'admin.platform.demoRequests.fields.phone',
                type: 'text',
                width: '128px',
                hideOn: 'md',
            },
            {
                key: 'messagePreview',
                label: 'admin.platform.demoRequests.fields.message',
                type: 'text',
                width: '1fr',
            },
            {
                key: 'locale',
                label: 'admin.platform.demoRequests.fields.locale',
                type: 'text',
                width: '72px',
                hideOn: 'md',
            },
            {
                key: 'createdAt',
                label: 'admin.platform.demoRequests.fields.createdAt',
                type: 'date',
                width: '160px',
            },
        ]);
    }

    protected onGridAction(event: GridActionEvent<PlatformDemoRequestRow>): void {
        if (event.action !== 'view') {
            return;
        }
        this.#matDialog.open(SpaDemoRequestDetailDialogComponent, {
            width: '760px',
            maxHeight: '90vh',
            panelClass: 'spa-compact-dialog',
            autoFocus: false,
            data: event.item,
        });
    }
}
