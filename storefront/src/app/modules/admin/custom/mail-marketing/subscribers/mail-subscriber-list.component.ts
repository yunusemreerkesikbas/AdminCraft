import { CommonModule } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    TemplateRef,
    ViewChild,
    computed,
    inject,
    signal,
} from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { BasePaginatedListComponent } from '@core/crud/base-paginated-list.component';
import { CrudHttpService } from '@core/crud/crud-http.service';
import { CrudStore } from '@core/crud/crud-store';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import {
    GridAction,
    GridColumn,
    SpaAdminGridComponent,
} from '@shared/components/spa-admin-grid';
import { SpaAdminPaginatorComponent } from '@shared/components/spa-admin-paginator/spa-admin-paginator.component';
import { SpaAdminSortDropdownComponent } from '@shared/components/spa-admin-sort-dropdown/spa-admin-sort-dropdown.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { AdminPageHeaderComponent } from 'app/shared/components/admin-page-header/admin-page-header.component';
import { take, takeUntil } from 'rxjs';
import {
    MailSubscriberAdminVm,
    MailSubscriberStatus,
    UpsertMailSubscriberPayload,
} from '../mail-marketing.types';
import { PlatformMailMarketingService } from '../platform-mail-marketing.service';
import { PlatformMailSubscriberAdminService } from '../platform-mail-subscriber-admin.service';
import { TenantMailMarketingService } from '../tenant-mail-marketing.service';
import { TenantMailSubscriberAdminService } from '../tenant-mail-subscriber-admin.service';
import { MailSubscriberEditDialogComponent } from './mail-subscriber-edit-dialog.component';

type MailScope = 'tenant' | 'platform';

@Component({
    selector: 'spa-mail-subscriber-list',
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
    templateUrl: './mail-subscriber-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MailSubscriberListComponent
    extends BasePaginatedListComponent<
        MailSubscriberAdminVm,
        UpsertMailSubscriberPayload,
        UpsertMailSubscriberPayload
    >
{
    @ViewChild('statusTemplate', { static: true })
    statusTemplate!: TemplateRef<any>;
    @ViewChild('subscriptionsTemplate', { static: true })
    subscriptionsTemplate!: TemplateRef<any>;

    readonly #route = inject(ActivatedRoute);
    readonly #dialog = inject(MatDialog);
    readonly #notify = inject(NotificationService);
    readonly #transloco = inject(TranslocoService);
    readonly #tenantService = inject(TenantMailSubscriberAdminService);
    readonly #platformService = inject(PlatformMailSubscriberAdminService);
    readonly #tenantMailMarketingService = inject(TenantMailMarketingService);
    readonly #platformMailMarketingService = inject(PlatformMailMarketingService);

    protected override service!: CrudHttpService<
        MailSubscriberAdminVm,
        UpsertMailSubscriberPayload,
        UpsertMailSubscriberPayload
    >;
    protected override store = new CrudStore<MailSubscriberAdminVm>();
    protected override defaultSort = 'createdAt,desc';
    protected override defaultPageSize = 20;
    protected scopeSig = signal<MailScope>('tenant');
    protected templateTypesSig = signal<string[]>([]);
    protected loadingTemplateTypesSig = signal(false);
    protected searchInputControl = new FormControl('');
    protected paginatedItemsSig = computed(() => this.store.items());
    protected readonly pageTitleKeySig = computed(() =>
        this.scopeSig() === 'platform'
            ? 'admin.mailMarketing.subscribers.platformTitle'
            : 'admin.mailMarketing.subscribers.tenantTitle'
    );
    protected readonly pageSubtitleKeySig = computed(() =>
        this.scopeSig() === 'platform'
            ? 'admin.mailMarketing.subscribers.platformSubtitle'
            : 'admin.mailMarketing.subscribers.tenantSubtitle'
    );

    protected columns: GridColumn<MailSubscriberAdminVm>[] = [];

    protected actions: GridAction<MailSubscriberAdminVm>[] = [
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

    constructor() {
        super();
        this.scopeSig.set(this.#resolveScope());
        this.service =
            this.scopeSig() === 'platform'
                ? this.#platformService
                : this.#tenantService;
    }

    protected override onInit(): void {
        this.columns = [
            {
                key: 'email',
                label: 'admin.mailMarketing.fields.email',
                type: 'text',
                width: '1fr',
            },
            {
                key: 'status',
                label: 'admin.mailMarketing.fields.status',
                type: 'custom',
                template: this.statusTemplate,
                width: '160px',
            },
            {
                key: 'subscriptions',
                label: 'admin.mailMarketing.subscribers.fields.bindings',
                type: 'custom',
                template: this.subscriptionsTemplate,
                width: '2fr',
            },
            {
                key: 'createdAt',
                label: 'admin.mailMarketing.fields.createdAt',
                type: 'date',
                width: '180px',
            },
        ];

        this.searchInputControl.valueChanges
            .pipe(takeUntil(this.destroy$))
            .subscribe((query) => this.onSearchInput(query || ''));

        this.#loadTemplateTypes();
    }

    protected override onLoadError(): void {
        this.#notify.alert('admin.mailMarketing.messages.subscribersLoadFailed');
    }

    protected onGridAction(event: { action: string; item: MailSubscriberAdminVm }): void {
        if (event.action === 'edit') {
            this.#editSubscriber(event.item.id);
            return;
        }
        if (event.action === 'delete') {
            this.#deleteSubscriber(event.item.id);
        }
    }

    protected createSubscriber(): void {
        this.#openDialog('create');
    }

    protected statusClass(status: MailSubscriberStatus): string {
        if (status === 'ACTIVE') {
            return 'bg-green-100 text-green-800 dark:bg-green-900/40 dark:text-green-200';
        }
        if (status === 'UNSUBSCRIBED') {
            return 'bg-red-100 text-red-800 dark:bg-red-900/40 dark:text-red-200';
        }
        return 'bg-blue-100 text-blue-800 dark:bg-blue-900/40 dark:text-blue-200';
    }

    #resolveScope(): MailScope {
        const scope = this.#route.snapshot.data['scope'];
        return scope === 'platform' ? 'platform' : 'tenant';
    }

    #loadTemplateTypes(): void {
        this.loadingTemplateTypesSig.set(true);
        const source$ =
            this.scopeSig() === 'platform'
                ? this.#platformMailMarketingService.getTemplateTypes()
                : this.#tenantMailMarketingService.getTemplateTypes();

        source$.pipe(take(1)).subscribe({
            next: (types) => {
                this.templateTypesSig.set(types.map((item) => item.templateType));
                this.loadingTemplateTypesSig.set(false);
            },
            error: () => {
                this.loadingTemplateTypesSig.set(false);
                this.#notify.alert('admin.mailMarketing.messages.templatesLoadFailed');
            },
        });
    }

    #editSubscriber(id: number): void {
        this.store.setLoading(true);
        this.service
            .getById(id)
            .pipe(take(1))
            .subscribe({
                next: (subscriber) => {
                    this.store.setLoading(false);
                    this.#openDialog('edit', subscriber);
                },
                error: () => {
                    this.store.setLoading(false);
                    this.#notify.alert('admin.mailMarketing.subscribers.messages.loadDetailFailed');
                },
            });
    }

    #deleteSubscriber(id: number): void {
        if (
            !confirm(
                this.#transloco.translate(
                    'admin.mailMarketing.subscribers.messages.confirmDelete'
                )
            )
        ) {
            return;
        }

        this.service
            .delete(id)
            .pipe(take(1))
            .subscribe({
                next: () => {
                    this.#notify.success(
                        'admin.mailMarketing.subscribers.messages.deleted'
                    );
                    this.loadItems();
                },
                error: () =>
                    this.#notify.alert(
                        'admin.mailMarketing.subscribers.messages.deleteFailed'
                    ),
            });
    }

    #openDialog(
        mode: 'create' | 'edit',
        subscriber?: MailSubscriberAdminVm
    ): void {
        const dialogRef = this.#dialog.open(MailSubscriberEditDialogComponent, {
            width: '920px',
            maxHeight: '90vh',
            panelClass: 'spa-compact-dialog',
            disableClose: true,
            data: {
                mode,
                scope: this.scopeSig(),
                templateTypes: this.templateTypesSig(),
                subscriber,
            },
        });

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
