import {
    AfterViewInit,
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    inject,
    input,
    output,
    TemplateRef,
    viewChild,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { BaseCrudListComponent, CrudStore } from '@core/crud';
import { TranslocoModule } from '@jsverse/transloco';
import {
    GridAction,
    GridColumn,
    SpaAdminGridComponent,
} from '@shared/components/spa-admin-grid';
import { NotificationService } from '@shared/notifications/notification.service';
import { ConfirmationService } from '@shared/services/confirmation.service';
import { finalize, take } from 'rxjs';
import {
    ComponentEntry,
    CreateEntryRequest,
    UpdateEntryRequest,
} from '../../models/component-entry.types';
import { ComponentEntryService } from '../../services/component-entry.service';
import { ComponentEntryFormComponent } from '../component-entry-form/component-entry-form.component';

@Component({
    selector: 'spa-component-entry-list',
    templateUrl: './component-entry-list.component.html',
    styleUrls: ['./component-entry-list.component.scss'],
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        MatButtonModule,
        MatIconModule,
        MatTooltipModule,
        TranslocoModule,
        SpaAdminGridComponent,
    ],
})
export class ComponentEntryListComponent
    extends BaseCrudListComponent<
        ComponentEntry,
        CreateEntryRequest,
        UpdateEntryRequest
    >
    implements AfterViewInit
{
    protected service = inject(ComponentEntryService);
    protected store = new CrudStore<ComponentEntry>();

    #notify = inject(NotificationService);
    #dialog = inject(MatDialog);
    #confirmation = inject(ConfirmationService);
    #cdr = inject(ChangeDetectorRef);

    protected componentId = input.required<number>();
    protected componentTypeId = input.required<number>();
    protected languages = input<string[]>(['tr', 'en']);
    protected draftMode = input<boolean>(false);
    readonly draftSaved = output<void>();

    protected infoTemplate =
        viewChild.required<TemplateRef<any>>('infoTemplate');

    protected columns: GridColumn<ComponentEntry>[] = [];

    protected readonly liveActions: GridAction<ComponentEntry>[] = [
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

    protected readonly draftActions: GridAction<ComponentEntry>[] = [
        {
            icon: 'heroicons_outline:pencil-square',
            label: 'admin.common.edit',
            action: 'edit',
        },
    ];

    protected actions: GridAction<ComponentEntry>[] = this.liveActions;

    override ngOnInit(): void {
        super.ngOnInit();
    }

    ngAfterViewInit(): void {
        this.columns = [
            {
                key: 'info',
                label: 'admin.common.grid.name',
                type: 'custom',
                template: this.infoTemplate(),
                width: '1fr',
            },
            {
                key: 'isVisible',
                label: 'admin.common.fields.isVisible',
                type: 'text',
                width: '120px',
            },
        ];
        this.actions = this.draftMode() ? this.draftActions : this.liveActions;
        this.#cdr.markForCheck();
    }

    protected override fetchItems() {
        return this.service.listByComponentId(this.componentId());
    }

    protected override onLoadSuccess(items: ComponentEntry[]): void {
        const sortedItems = [...items].sort(
            (a, b) => a.sortOrder - b.sortOrder
        );
        this.store.setItems(sortedItems);
    }

    protected override onLoadError(error: any): void {
        this.#notify.alert(error?.error?.message ?? '');
    }

    protected onGridAction(event: {
        action: string;
        item: ComponentEntry;
    }): void {
        switch (event.action) {
            case 'edit':
                this.editEntry(event.item.id);
                break;
            case 'delete':
                this.deleteEntry(event.item);
                break;
        }
    }

    createEntry(): void {
        if (this.draftMode()) {
            return;
        }
        const sortOrder = this.#calculateNextSortOrder();

        const dialogRef = this.#dialog.open(ComponentEntryFormComponent, {
            width: '800px',
            maxHeight: '90vh',
            disableClose: true,
            data: {
                mode: 'create',
                componentId: this.componentId(),
                componentTypeId: this.componentTypeId(),
                languages: this.languages(),
                sortOrder,
            },
        });

        dialogRef
            .afterClosed()
            .pipe(take(1))
            .subscribe((success) => {
                if (success) {
                    this.loadItems();
                }
            });
    }

    #calculateNextSortOrder(): number {
        const items = this.store.items();
        if (items.length === 0) return 0;

        const maxSortOrder = Math.max(...items.map((item) => item.sortOrder));
        return maxSortOrder + 1;
    }

    protected editEntry(entryId: number): void {
        this.store.setLoading(true);
        this.service
            .getEntryDetail(entryId)
            .pipe(
                take(1),
                finalize(() => this.store.setLoading(false))
            )
            .subscribe({
                next: (detail) => {
                    const dialogRef = this.#dialog.open(
                        ComponentEntryFormComponent,
                        {
                            width: '800px',
                            maxHeight: '90vh',
                            disableClose: true,
                            data: {
                                mode: 'edit',
                                componentId: this.componentId(),
                                componentTypeId: this.componentTypeId(),
                                languages: this.languages(),
                                entryId: entryId,
                                entry: detail,
                                translations: detail.translations,
                                draftMode: this.draftMode(),
                            },
                        }
                    );

                    dialogRef
                        .afterClosed()
                        .pipe(take(1))
                        .subscribe((success) => {
                            if (success) {
                                this.loadItems();
                                if (this.draftMode()) {
                                    this.draftSaved.emit();
                                }
                            }
                        });
                },
                error: (err) => {
                    this.#notify.alert(err?.error?.message ?? '');
                },
            });
    }

    protected deleteEntry(entry: ComponentEntry): void {
        if (this.draftMode()) {
            return;
        }
        this.#confirmation
            .confirm(
                'admin.components.entries.deleteTitle',
                'admin.components.entries.confirmDelete'
            )
            .pipe(take(1))
            .subscribe((confirmed) => {
                if (!confirmed) return;

                this.service
                    .deleteWithResponse(entry.id)
                    .pipe(take(1))
                    .subscribe({
                        next: (response) => {
                            this.#notify.success(response.message ?? '');
                            this.loadItems();
                        },
                        error: (error) => {
                            this.#notify.alert(error?.error?.message ?? '');
                        },
                    });
            });
    }
}
