import {
    ChangeDetectionStrategy,
    Component,
    computed,
    effect,
    EventEmitter,
    inject,
    Input,
    OnChanges,
    OnDestroy,
    OnInit,
    Output,
    signal,
    SimpleChanges,
    TemplateRef,
    ViewChild,
    ViewEncapsulation,
} from '@angular/core';
import { NgClass } from '@angular/common';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule } from '@angular/material/paginator';
import { BaseSelectablePaginatedListComponent, BulkDeleteRunnerService } from '@core/crud';
import { UserService } from '@core/user/user.service';
import { LanguageContextService } from '@core/services/language-context.service';
import { TranslocoModule } from '@jsverse/transloco';
import {
    GridAction,
    GridColumn,
    SpaAdminGridComponent,
} from '@shared/components/spa-admin-grid';
import { SpaAdminPaginatorComponent } from '@shared/components/spa-admin-paginator/spa-admin-paginator.component';
import { SpaAdminSortDropdownComponent } from '@shared/components/spa-admin-sort-dropdown/spa-admin-sort-dropdown.component';
import { ConfirmationService } from '@shared/services/confirmation.service';
import { NotificationService } from '@shared/notifications/notification.service';
import { VIEWER_ROLE } from '@shared/constants';
import { AdminPageHeaderComponent } from 'app/shared/components/admin-page-header/admin-page-header.component';
import { take, takeUntil } from 'rxjs';
import { finalize } from 'rxjs/operators';
import { ComponentEditDialogComponent } from '../component-edit-dialog/component-edit-dialog.component';
import {
    ComponentDetailDto,
    ComponentDto,
    ComponentTypeDto,
    CreateComponentRequest,
    UpdateComponentRequest,
} from '../models/component-library.types';
import { ComponentLibraryService } from '../services/component-library.service';
import { ComponentStore } from '../services/component.store';

const DIALOG_CONFIG = {
    COMPONENT_FORM: { width: '800px', height: 'auto' },
};

@Component({
    selector: 'spa-component-list',
    standalone: true,
    imports: [
        NgClass,
        FormsModule,
        ReactiveFormsModule,
        MatButtonModule,
        MatCheckboxModule,
        MatIconModule,
        MatPaginatorModule,
        TranslocoModule,
        AdminPageHeaderComponent,
        SpaAdminGridComponent,
        SpaAdminPaginatorComponent,
        SpaAdminSortDropdownComponent,
    ],
    templateUrl: './component-list.component.html',
    styleUrls: ['./component-list.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ComponentListComponent
    extends BaseSelectablePaginatedListComponent<
        ComponentDto,
        CreateComponentRequest,
        UpdateComponentRequest
    >
    implements OnInit, OnDestroy, OnChanges
{
    @Input() mode: 'admin' | 'picker' = 'admin';
    @Output() componentSelected = new EventEmitter<ComponentDto>();

    @ViewChild('infoTemplate', { static: true })
    infoTemplate!: TemplateRef<any>;
    @ViewChild('visibleTemplate', { static: true })
    visibleTemplate!: TemplateRef<any>;
    @ViewChild('pickerActionsTemplate', { static: true })
    pickerActionsTemplate!: TemplateRef<any>;
    @ViewChild('headerSelectAllTemplate', { static: true })
    headerSelectAllTemplate!: TemplateRef<void>;
    @ViewChild('rowCheckboxTemplate', { static: true })
    rowCheckboxTemplate!: TemplateRef<any>;

    #matDialog = inject(MatDialog);
    #confirmationService = inject(ConfirmationService);
    #notificationService = inject(NotificationService);
    #bulkDeleteRunner = inject(BulkDeleteRunnerService);
    #languageContextService = inject(LanguageContextService);
    #userService = inject(UserService);
    protected override service = inject(ComponentLibraryService);
    protected override store = inject(ComponentStore);
    protected override defaultSort = 'createdAt,desc';
    protected override defaultPageSize = 20;

    protected readonly isViewerSig = computed(
        () => this.#userService.user()?.role === VIEWER_ROLE
    );

    readonly #syncColumnsWithRole = effect(() => {
        void this.isViewerSig();
        this.#rebuildColumns();
    });

    protected componentTypesSig = signal<ComponentTypeDto[]>([]);
    protected typesLoadingSig = signal<boolean>(false);
    protected supportedLanguagesSig = computed(() =>
        this.#languageContextService.supportedLanguages()
    );
    protected searchInputControl = new FormControl('');
    protected paginatedItemsSig = computed(() => this.store.items());

    protected columns: GridColumn<ComponentDto>[] = [];

    protected actions: GridAction<ComponentDto>[] = [];

    protected override onInit(): void {
        this.#rebuildColumns();
        this.#loadComponentTypes();
        this.#setupSearchDebounce();
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['mode']) {
            this.#rebuildColumns();
        }
    }

    #rebuildColumns(): void {
        const baseColumns: GridColumn<ComponentDto>[] = [
            {
                key: 'info',
                label: 'admin.common.grid.uid',
                type: 'custom',
                template: this.infoTemplate,
                width: '1fr',
            },
            {
                key: 'componentTypeName',
                label: 'admin.components.filters.type',
                type: 'badge',
                hideOn: 'sm',
                width: '150px',
            },
            {
                key: 'isVisible',
                label: 'admin.common.fields.isVisible',
                type: 'custom',
                template: this.visibleTemplate,
                hideOn: 'sm',
                width: '120px',
            },
        ];

        if (this.mode === 'admin' && !this.isViewerSig()) {
            this.columns = [
                {
                    key: 'select',
                    label: '',
                    type: 'custom',
                    width: '52px',
                    cssClass: 'flex items-center justify-center',
                    headerTemplate: this.headerSelectAllTemplate,
                    template: this.rowCheckboxTemplate,
                },
                ...baseColumns,
            ];
        } else {
            this.columns = baseColumns;
        }

        this.actions = [
            {
                icon: 'heroicons_outline:pencil-square',
                label: 'admin.common.edit',
                action: 'edit',
                show: () => !this.isViewerSig(),
            },
            {
                icon: 'heroicons_outline:trash',
                label: 'admin.common.delete',
                action: 'delete',
                color: 'warn',
                show: () => !this.isViewerSig(),
            },
        ];
    }

    protected showSelectionUi(): boolean {
        return this.mode === 'admin' && !this.isViewerSig();
    }

    protected pageSelectionState(): {
        allSelected: boolean;
        indeterminate: boolean;
    } {
        return super.pageSelectionState();
    }

    protected onToggleSelectAllOnPage(event: {
        checked: boolean;
    }): void {
        this.toggleSelectAllOnPage(event.checked);
    }

    protected onRowCheckboxChange(component: ComponentDto): void {
        this.store.toggleSelected(component.id);
    }

    protected deleteSelectedComponents(): void {
        if (this.isViewerSig()) {
            return;
        }
        const ids = [...this.store.selectedIdsSig()];
        if (ids.length === 0) {
            return;
        }
        this.#confirmationService
            .confirm(
                'admin.components.bulkDeleteConfirmTitle',
                'admin.components.bulkDeleteConfirmMessage',
                'admin.common.actions.confirm',
                'warning',
                { count: ids.length }
            )
            .pipe(take(1))
            .subscribe((confirmed) => {
                if (!confirmed) {
                    return;
                }
                this.store.setLoading(true);
                this.#bulkDeleteRunner
                    .run(ids, {
                        bulkDelete$: () =>
                            this.service.bulkDeleteComponentsWithResponse(ids).pipe(take(1)),
                        deleteOne$: (id) =>
                            this.service.deleteComponentWithResponse(id),
                    })
                    .pipe(
                        finalize(() => this.store.setLoading(false)),
                        takeUntil(this.destroy$)
                    )
                    .subscribe({
                        next: (result) => {
                            if (result.message) {
                                this.#notificationService.success(result.message);
                            }
                            this.store.clearSelection();
                            this.loadItems();
                        },
                        error: (error) => {
                            const msg =
                                error?.error?.message ??
                                error?.message ??
                                '';
                            this.#notificationService.alert(msg);
                        },
                    });
            });
    }

    #setupSearchDebounce(): void {
        this.searchInputControl.valueChanges
            .pipe(takeUntil(this.destroy$))
            .subscribe((query) => {
                this.onSearchInput(query || '');
            });
    }

    #loadComponentTypes(): void {
        this.typesLoadingSig.set(true);
        this.service
            .listComponentTypes()
            .pipe(take(1))
            .subscribe({
                next: (types) => {
                    this.componentTypesSig.set(types);
                    this.typesLoadingSig.set(false);
                },
                error: (error) => {
                    this.#notificationService.alert(error?.error?.message ?? '');
                    this.typesLoadingSig.set(false);
                },
            });
    }

    protected override onLoadSuccess(_items: ComponentDto[]): void {
        this.store.clearSelection();
    }

    protected override onLoadError(error: any): void {
        this.#notificationService.alert(error?.error?.message ?? '');
    }

    protected onGridAction(event: {
        action: string;
        item: ComponentDto;
    }): void {
        switch (event.action) {
            case 'edit':
                this.editComponent(event.item.id);
                break;
            case 'delete':
                this.deleteComponent(event.item.id);
                break;
        }
    }

    canCreateComponent(): boolean {
        return this.componentTypesSig().length > 0;
    }

    createComponent(): void {
        if (this.isViewerSig()) {
            return;
        }
        if (!this.canCreateComponent()) {
            if (this.typesLoadingSig()) {
                this.#notificationService.info(
                    'admin.components.info.loadingTypes'
                );
            } else {
                this.#notificationService.warning(
                    'admin.components.errors.noTypes'
                );
            }
            return;
        }

        const dialogRef = this.#matDialog.open(ComponentEditDialogComponent, {
            width: DIALOG_CONFIG.COMPONENT_FORM.width,
            height: '90vh',
            maxHeight: '90vh',
            panelClass: 'spa-compact-dialog',
            disableClose: true,
            data: {
                mode: 'create',
                componentTypes: this.componentTypesSig(),
                languages: this.supportedLanguagesSig(),
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

    protected editComponent(componentId: number): void {
        if (this.isViewerSig()) {
            return;
        }
        this.store.setLoading(true);
        this.service
            .getComponentDetail(componentId)
            .pipe(take(1))
            .subscribe({
                next: (detail) => {
                    this.store.setLoading(false);
                    this.#openEditDialog(detail);
                },
                error: (error) => {
                    this.store.setLoading(false);
                    this.#notificationService.alert(error?.error?.message ?? '');
                },
            });
    }

    #openEditDialog(detail: ComponentDetailDto): void {
        const dialogRef = this.#matDialog.open(ComponentEditDialogComponent, {
            width: '900px',
            height: '90vh',
            maxHeight: '90vh',
            panelClass: 'spa-compact-dialog',
            disableClose: true,
            data: {
                mode: 'edit',
                component: detail,
                languages: this.supportedLanguagesSig(),
                componentTypes: this.componentTypesSig(),
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

    protected deleteComponent(componentId: number): void {
        if (this.isViewerSig()) {
            return;
        }
        this.#confirmationService
            .confirm('admin.common.delete', 'admin.components.confirmDelete')
            .pipe(take(1))
            .subscribe((confirmed) => {
                if (!confirmed) {
                    return;
                }

                this.service
                    .deleteComponentWithResponse(componentId)
                    .pipe(take(1))
                    .subscribe({
                        next: (response) => {
                            this.#notificationService.success(
                                response.message ?? ''
                            );
                            this.loadItems();
                        },
                        error: (error) =>
                            this.#notificationService.alert(
                                error?.error?.message ?? ''
                            ),
                    });
            });
    }

    selectComponent(component: ComponentDto): void {
        this.componentSelected.emit(component);
    }
}
