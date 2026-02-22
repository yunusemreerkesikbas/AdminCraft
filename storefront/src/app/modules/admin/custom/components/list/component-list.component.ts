import { CommonModule } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    computed,
    EventEmitter,
    inject,
    Input,
    OnDestroy,
    OnInit,
    Output,
    signal,
    TemplateRef,
    ViewChild,
    ViewEncapsulation,
} from '@angular/core';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule } from '@angular/material/paginator';
import { BasePaginatedListComponent } from '@core/crud/base-paginated-list.component';
import { LanguageContextService } from '@core/services/language-context.service';
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
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        MatButtonModule,
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
    extends BasePaginatedListComponent<
        ComponentDto,
        CreateComponentRequest,
        UpdateComponentRequest
    >
    implements OnInit, OnDestroy
{
    @Input() mode: 'admin' | 'picker' = 'admin';
    @Output() componentSelected = new EventEmitter<ComponentDto>();

    @ViewChild('infoTemplate', { static: true })
    infoTemplate!: TemplateRef<any>;
    @ViewChild('visibleTemplate', { static: true })
    visibleTemplate!: TemplateRef<any>;
    @ViewChild('pickerActionsTemplate', { static: true })
    pickerActionsTemplate!: TemplateRef<any>;

    #matDialog = inject(MatDialog);
    #translocoService = inject(TranslocoService);
    #notificationService = inject(NotificationService);
    #languageContextService = inject(LanguageContextService);
    protected override service = inject(ComponentLibraryService);
    protected override store = inject(ComponentStore);
    protected override defaultSort = 'createdAt,desc';
    protected override defaultPageSize = 20;

    protected componentTypesSig = signal<ComponentTypeDto[]>([]);
    protected typesLoadingSig = signal<boolean>(false);
    protected supportedLanguagesSig = computed(() =>
        this.#languageContextService.supportedLanguages()
    );
    protected searchInputControl = new FormControl('');
    protected paginatedItemsSig = computed(() => this.store.items());

    protected columns: GridColumn<ComponentDto>[] = [];

    protected actions: GridAction<ComponentDto>[] = [
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

        this.#loadComponentTypes();
        this.#setupSearchDebounce();
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
                error: () => {
                    this.#notificationService.alert(
                        'admin.components.errors.loadTypesFailed'
                    );
                    this.typesLoadingSig.set(false);
                },
            });
    }

    protected override onLoadError(error: any): void {
        this.#notificationService.alert('admin.components.errors.loadFailed');
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
        this.store.setLoading(true);
        this.service
            .getComponentDetail(componentId)
            .pipe(take(1))
            .subscribe({
                next: (detail) => {
                    this.store.setLoading(false);
                    this.#openEditDialog(detail);
                },
                error: () => {
                    this.store.setLoading(false);
                    this.#notificationService.alert(
                        'admin.components.errors.loadDetailFailed'
                    );
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
        if (
            !confirm(
                this.#translocoService.translate(
                    'admin.components.confirmDelete'
                )
            )
        ) {
            return;
        }

        this.service
            .deleteComponent(componentId)
            .pipe(take(1))
            .subscribe({
                next: () => {
                    this.#notificationService.success(
                        'admin.components.success.deleted'
                    );
                    this.loadItems();
                },
                error: () =>
                    this.#notificationService.alert(
                        'admin.components.errors.deleteFailed'
                    ),
            });
    }

    selectComponent(component: ComponentDto): void {
        this.componentSelected.emit(component);
    }
}
