import { ChangeDetectionStrategy, Component, computed, inject, OnDestroy, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule } from '@angular/material/paginator';
import { BasePaginatedListComponent } from '@core/crud/base-paginated-list.component';
import { TranslocoModule } from '@jsverse/transloco';
import { GridAction, GridColumn, SpaAdminGridComponent } from '@shared/components/spa-admin-grid';
import { SpaAdminPaginatorComponent } from '@shared/components/spa-admin-paginator/spa-admin-paginator.component';
import { SpaAdminSortDropdownComponent } from '@shared/components/spa-admin-sort-dropdown/spa-admin-sort-dropdown.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { ConfirmationService } from '@shared/services/confirmation.service';
import { ItemDialogService } from '@shared/services/item-dialog.service';
import { type ItemDialogOptions } from '@shared/types/item-dialog.types';
import { AdminPageHeaderComponent } from 'app/shared/components/admin-page-header/admin-page-header.component';
import { take, takeUntil } from 'rxjs';
import { ComponentTypeFormData } from '../models/component-form.types';
import {
    ComponentTypeDto,
    CreateComponentTypeRequest,
    UpdateComponentTypeRequest
} from '../models/component-library.types';
import { ComponentSchemaBuilderService } from '../services/component-schema-builder.service';
import { ComponentTypeService } from '../services/component-type.service';
import { ComponentTypeStore } from '../services/component-type.store';
import { ComponentTypeEditDialogComponent } from './component-type-edit-dialog/component-type-edit-dialog.component';

@Component({
    selector: 'spa-component-types-list',
    templateUrl: './component-types-list.component.html',
    styleUrls: ['./component-types-list.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: true,
    imports: [
        FormsModule,
        ReactiveFormsModule,
        MatButtonModule,
        MatIconModule,
        MatDialogModule,
        MatPaginatorModule,
        TranslocoModule,
        AdminPageHeaderComponent,
        SpaAdminGridComponent,
        SpaAdminPaginatorComponent,
        SpaAdminSortDropdownComponent
    ]
})
export class ComponentTypesListComponent extends BasePaginatedListComponent<ComponentTypeDto, CreateComponentTypeRequest, UpdateComponentTypeRequest> implements OnInit, OnDestroy {
    @ViewChild('infoTemplate', { static: true }) infoTemplate!: TemplateRef<any>;

    #notificationService = inject(NotificationService);
    #confirmationService = inject(ConfirmationService);
    #itemDialogService = inject(ItemDialogService);
    #matDialog = inject(MatDialog);
    #componentSchemaBuilderService = inject(ComponentSchemaBuilderService);

    protected override service = inject(ComponentTypeService);
    protected override store = inject(ComponentTypeStore);
    protected override defaultSort = 'createdAt,desc';
    protected override defaultPageSize = 20;

    protected searchInputControl = new FormControl('');
    protected paginatedItemsSig = computed(() => this.store.items());

    protected columns: GridColumn<ComponentTypeDto>[] = [];

    protected actions: GridAction<ComponentTypeDto>[] = [
        { icon: 'heroicons_outline:pencil-square', label: 'admin.common.edit', action: 'edit' },
        { icon: 'heroicons_outline:trash', label: 'admin.common.delete', action: 'delete', color: 'warn' }
    ];

    protected override onInit(): void {
        this.columns = [
            {
                key: 'info',
                label: 'admin.common.grid.uid',
                type: 'custom',
                template: this.infoTemplate,
                width: '1fr'
            },
            {
                key: 'category',
                label: 'admin.components.fields.category',
                type: 'badge',
                hideOn: 'sm',
                width: '150px'
            }
        ];

        this.#setupSearchDebounce();
    }

    #setupSearchDebounce(): void {
        this.searchInputControl.valueChanges.pipe(
            takeUntil(this.destroy$)
        ).subscribe(query => {
            this.onSearchInput(query || '');
        });
    }

    protected override onLoadError(error: any): void {
        this.#notificationService.alert('admin.components.errors.loadTypesFailed');
    }

    protected onGridAction(event: { action: string; item: ComponentTypeDto }): void {
        switch (event.action) {
            case 'edit':
                this.editType(event.item);
                break;
            case 'delete':
                this.deleteType(event.item);
                break;
        }
    }

    protected createType(): void {
        const schema = this.#componentSchemaBuilderService.buildComponentTypeSchema();
        const initial: ComponentTypeFormData = {
            name: null,
            category: null,
            navigationAware: false
        };

        const options: ItemDialogOptions<ComponentTypeFormData> = {
            titleKey: 'admin.components.types.create',
            mode: 'create',
            schema,
            languages: [],
            initial,
            modalData: { disableClose: true, width: '600px' }
        };

        this.#itemDialogService.open(options)
            .pipe(take(1))
            .subscribe((result) => {
                if (!result) return;

                const payload: CreateComponentTypeRequest = {
                    name: result.name!,
                    category: result.category || undefined,
                    navigationAware: !!result.navigationAware
                };

                this.service.create(payload)
                    .pipe(take(1))
                    .subscribe({
                        next: () => {
                            this.#notificationService.success('admin.components.types.success.created');
                            this.loadItems();
                        },
                        error: () => this.#notificationService.alert('admin.components.types.errors.createFailed')
                    });
            });
    }

    protected editType(type: ComponentTypeDto): void {
        if (type.isSystem) {
            this.#notificationService.warning('admin.components.types.errors.cannotEditSystem');
            return;
        }

        const dialogRef = this.#matDialog.open(ComponentTypeEditDialogComponent, {
            width: '560px',
            maxWidth: '95vw',
            maxHeight: '90vh',
            disableClose: true,
            data: { type }
        });

        dialogRef.afterClosed()
            .pipe(take(1))
            .subscribe((success) => {
                if (success) {
                    this.loadItems();
                }
            });
    }

    protected deleteType(type: ComponentTypeDto): void {
        if (type.isSystem) {
            this.#notificationService.warning('admin.components.types.errors.cannotDeleteSystem');
            return;
        }

        this.#confirmationService.confirm(
            'admin.components.types.confirmDeleteTitle',
            'admin.components.types.confirmDelete',
            'admin.common.actions.delete'
        ).pipe(take(1)).subscribe(confirmed => {
            if (!confirmed) return;

            this.service.delete(type.id)
                .pipe(take(1))
                .subscribe({
                    next: () => {
                        this.#notificationService.success('admin.components.types.success.deleted');
                        this.loadItems();
                    },
                    error: () => this.#notificationService.alert('admin.components.types.errors.deleteFailed')
                });
        });
    }

}
