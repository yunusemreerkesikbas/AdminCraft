import { CommonModule } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    Injectable,
    TemplateRef,
    ViewChild,
    inject,
} from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { BasePaginatedListComponent } from '@core/crud/base-paginated-list.component';
import { CrudEndpoints, CrudHttpService } from '@core/crud/crud-http.service';
import { CrudStore } from '@core/crud/crud-store';
import {
    GridAction,
    GridColumn,
    SpaAdminGridComponent,
} from '@shared/components/spa-admin-grid';
import { SpaAdminPaginatorComponent } from '@shared/components/spa-admin-paginator/spa-admin-paginator.component';
import { SpaAdminSortDropdownComponent } from '@shared/components/spa-admin-sort-dropdown/spa-admin-sort-dropdown.component';
import { AdminPageHeaderComponent } from '@shared/components/admin-page-header/admin-page-header.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { take, takeUntil } from 'rxjs';
import {
    OutreachContactVm,
    OutreachContactStatus,
    CreateOutreachContactPayload,
    UpdateOutreachContactPayload,
} from './platform-outreach.types';
import { PlatformOutreachService } from './platform-outreach.service';
import { PlatformOutreachContactEditDialogComponent } from './platform-outreach-contact-edit-dialog.component';

@Injectable({ providedIn: 'root' })
class OutreachContactCrudService extends CrudHttpService<
    OutreachContactVm,
    CreateOutreachContactPayload,
    UpdateOutreachContactPayload
> {
    protected override endpoints: CrudEndpoints = {
        list: 'platformOutreachContacts',
        getById: 'platformOutreachContactById',
        create: 'platformOutreachContacts',
        update: 'platformOutreachContactById',
        delete: 'platformOutreachContactById',
    };
}

@Component({
    selector: 'spa-platform-outreach-contact-list',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatButtonModule,
        MatIconModule,
        TranslocoModule,
        AdminPageHeaderComponent,
        SpaAdminGridComponent,
        SpaAdminPaginatorComponent,
        SpaAdminSortDropdownComponent,
    ],
    templateUrl: './platform-outreach-contact-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SpaPlatformOutreachContactListComponent extends BasePaginatedListComponent<
    OutreachContactVm,
    CreateOutreachContactPayload,
    UpdateOutreachContactPayload
> {
    @ViewChild('statusTemplate', { static: true })
    statusTemplate!: TemplateRef<any>;

    readonly #outreachService = inject(PlatformOutreachService);
    readonly #dialog = inject(MatDialog);
    readonly #notify = inject(NotificationService);
    readonly #transloco = inject(TranslocoService);

    protected override service = inject(OutreachContactCrudService);
    protected override store = new CrudStore<OutreachContactVm>();
    protected override defaultSort = 'createdAt,desc';
    protected override defaultPageSize = 20;

    protected searchInputControl = new FormControl('');
    protected columns: GridColumn<OutreachContactVm>[] = [];

    protected actions: GridAction<OutreachContactVm>[] = [
        {
            icon: 'heroicons_outline:pencil-square',
            label: 'admin.common.edit',
            action: 'edit',
        },
        {
            icon: 'heroicons_outline:trash',
            label: 'admin.common.delete',
            action: 'delete',
            color: 'warn',
        },
    ];

    protected override onInit(): void {
        this.columns = [
            {
                key: 'fullName',
                label: 'admin.outreach.contacts.fields.fullName',
                type: 'text',
                width: '1fr',
            },
            {
                key: 'email',
                label: 'admin.outreach.contacts.fields.email',
                type: 'text',
                width: '1fr',
            },
            {
                key: 'companyName',
                label: 'admin.outreach.contacts.fields.companyName',
                type: 'text',
                width: '1fr',
            },
            {
                key: 'city',
                label: 'admin.outreach.contacts.fields.city',
                type: 'text',
                width: '120px',
            },
            {
                key: 'status',
                label: 'admin.outreach.contacts.fields.status',
                type: 'custom',
                template: this.statusTemplate,
                width: '140px',
            },
            {
                key: 'createdAt',
                label: 'admin.outreach.contacts.fields.createdAt',
                type: 'date',
                width: '180px',
            },
        ];

        this.searchInputControl.valueChanges
            .pipe(takeUntil(this.destroy$))
            .subscribe((query) => this.onSearchInput(query || ''));
    }

    protected override onLoadError(): void {}

    protected onGridAction(event: { action: string; item: OutreachContactVm }): void {
        if (event.action === 'edit') {
            this.#editContact(event.item.id);
            return;
        }
        if (event.action === 'delete') {
            this.#deleteContact(event.item.id);
        }
    }

    protected createContact(): void {
        this.#openDialog('create');
    }

    protected statusClass(status: OutreachContactStatus): string {
        if (status === 'ACTIVE') {
            return 'bg-green-100 text-green-800 dark:bg-green-900/40 dark:text-green-200';
        }
        return 'bg-red-100 text-red-800 dark:bg-red-900/40 dark:text-red-200';
    }

    #editContact(id: number): void {
        this.store.setLoading(true);
        this.#outreachService
            .getContactById(id)
            .pipe(take(1))
            .subscribe({
                next: (contact) => {
                    this.store.setLoading(false);
                    this.#openDialog('edit', contact);
                },
                error: (error: any) => {
                    this.store.setLoading(false);
                    this.#notify.alert(error?.error?.message ?? '');
                },
            });
    }

    #deleteContact(id: number): void {
        if (
            !confirm(
                this.#transloco.translate('admin.outreach.contacts.messages.confirmDelete')
            )
        ) {
            return;
        }

        this.#outreachService
            .deleteContact(id)
            .pipe(take(1))
            .subscribe({
                next: () => this.loadItems(),
                error: (error: any) =>
                    this.#notify.alert(error?.error?.message ?? ''),
            });
    }

    #openDialog(mode: 'create' | 'edit', contact?: OutreachContactVm): void {
        const dialogRef = this.#dialog.open(
            PlatformOutreachContactEditDialogComponent,
            {
                width: '600px',
                maxWidth: 'calc(100vw - 2rem)',
                maxHeight: '82vh',
                panelClass: 'spa-compact-dialog',
                disableClose: true,
                data: { mode, contact },
            }
        );

        dialogRef
            .afterClosed()
            .pipe(take(1))
            .subscribe((result) => {
                if (result) {
                    this.loadItems();
                }
            });
    }
}
