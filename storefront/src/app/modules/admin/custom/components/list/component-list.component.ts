import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, EventEmitter, inject, Input, OnInit, Output, signal, TemplateRef, ViewChild, ViewEncapsulation } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { BaseCrudListComponent } from '@core/crud';
import { LanguageContextService } from '@core/services/language-context.service';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { GridAction, GridColumn, SpaAdminGridComponent } from '@shared/components/spa-admin-grid';
import { NotificationService } from '@shared/notifications/notification.service';
import { AdminPageHeaderComponent } from 'app/shared/components/admin-page-header/admin-page-header.component';
import { take } from 'rxjs';
import { ComponentEditDialogComponent } from '../component-edit-dialog/component-edit-dialog.component';
import {
    ComponentDetailDto,
    ComponentDto,
    ComponentTypeDto,
    CreateComponentRequest,
    UpdateComponentRequest
} from '../models/component-library.types';
import { ComponentLibraryService } from '../services/component-library.service';
import { ComponentStore } from '../services/component.store';
import { ComponentTypesManagerComponent } from '../types/component-types-manager.component';

const DIALOG_CONFIG = {
    COMPONENT_FORM: { width: '800px', height: 'auto' },
    TYPES_MANAGER: { width: '900px', height: '80vh' }
};

@Component({
    selector: 'spa-component-list',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        MatButtonModule,
        MatIconModule,
        MatTooltipModule,
        TranslocoModule,
        AdminPageHeaderComponent,
        SpaAdminGridComponent
    ],
    templateUrl: './component-list.component.html',
    styleUrls: ['./component-list.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ComponentListComponent extends BaseCrudListComponent<ComponentDto, CreateComponentRequest, UpdateComponentRequest> implements OnInit {
    @Input() mode: 'admin' | 'picker' = 'admin';
    @Output() componentSelected = new EventEmitter<ComponentDto>();

    @ViewChild('infoTemplate', { static: true }) infoTemplate!: TemplateRef<any>;
    @ViewChild('pickerActionsTemplate', { static: true }) pickerActionsTemplate!: TemplateRef<any>;

    #dialog = inject(MatDialog);
    #transloco = inject(TranslocoService);
    #notify = inject(NotificationService);
    #languageContext = inject(LanguageContextService);
    protected service = inject(ComponentLibraryService);
    protected store = inject(ComponentStore);
    protected componentTypes = signal<ComponentTypeDto[]>([]);
    protected typesLoading = signal<boolean>(false);
    protected supportedLanguages = computed(() => this.#languageContext.supportedLanguages());
    protected searchTerm = signal<string>('');

    protected columns: GridColumn<ComponentDto>[] = [];

    protected actions: GridAction<ComponentDto>[] = [
        { icon: 'heroicons_outline:pencil-square', label: 'admin.common.edit', action: 'edit' },
        { icon: 'heroicons_outline:trash', label: 'admin.common.delete', action: 'delete', color: 'warn' }
    ];

    protected filteredComponents = computed(() => {
        const items = this.store.items();
        const term = this.searchTerm().toLowerCase();

        return items.filter(item => {
            const matchesSearch = !term || item.name.toLowerCase().includes(term) || item.uid.toLowerCase().includes(term);
            return matchesSearch;
        });
    });

    override ngOnInit(): void {
        this.columns = [
            {
                key: 'info',
                label: 'admin.common.grid.name',
                type: 'custom',
                template: this.infoTemplate,
                width: '1fr'
            },
            {
                key: 'componentTypeName',
                label: 'admin.components.filters.type',
                type: 'badge',
                hideOn: 'sm',
                width: '150px'
            },
            {
                key: 'status',
                label: 'admin.common.grid.status',
                type: 'status',
                hideOn: 'sm',
                width: '120px'
            }
        ];

        super.ngOnInit();
        this.#loadComponentTypes();
    }

    #loadComponentTypes(): void {
        this.typesLoading.set(true);
        this.service.listComponentTypes()
            .pipe(take(1))
            .subscribe({
                next: (types) => {
                    this.componentTypes.set(types);
                    this.typesLoading.set(false);
                },
                error: () => {
                    this.#notify.alert('admin.components.errors.loadTypesFailed');
                    this.typesLoading.set(false);
                }
            });
    }

    protected override fetchItems() {
        return this.service.listComponents();
    }

    protected override onLoadError(error: any): void {
        this.#notify.alert('admin.components.errors.loadFailed');
    }

    protected onGridAction(event: { action: string; item: ComponentDto }): void {
        switch (event.action) {
            case 'edit':
                this.editComponent(event.item.id);
                break;
            case 'delete':
                this.deleteComponent(event.item.id);
                break;
        }
    }




    protected override onSearchChange(term: string): void {
        this.searchTerm.set(term);
        super.onSearchChange(term);
    }

    canCreateComponent(): boolean {
        return this.componentTypes().length > 0;
    }

    createComponent(): void {
        if (!this.canCreateComponent()) {
            if (this.typesLoading()) {
                this.#notify.info('admin.components.info.loadingTypes');
            } else {
                this.#notify.warning('admin.components.errors.noTypes');
            }
            return;
        }

        const dialogRef = this.#dialog.open(ComponentEditDialogComponent, {
            width: DIALOG_CONFIG.COMPONENT_FORM.width,
            maxHeight: '90vh',
            disableClose: true,
            data: {
                mode: 'create',
                componentTypes: this.componentTypes(),
                languages: this.supportedLanguages()
            }
        });

        dialogRef.afterClosed().pipe(take(1)).subscribe((result) => {
            if (result) {
                this.loadItems();
            }
        });
    }

    protected editComponent(componentId: number): void {
        this.store.setLoading(true);
        this.service.getComponentDetail(componentId)
            .pipe(take(1))
            .subscribe({
                next: (detail) => {
                    this.store.setLoading(false);
                    this.#openEditDialog(detail);
                },
                error: () => {
                    this.store.setLoading(false);
                    this.#notify.alert('admin.components.errors.loadDetailFailed');
                }
            });
    }

    #openEditDialog(detail: ComponentDetailDto): void {
        const dialogRef = this.#dialog.open(ComponentEditDialogComponent, {
            width: '900px',
            maxHeight: '90vh',
            disableClose: true,
            data: {
                mode: 'edit',
                component: detail,
                languages: this.supportedLanguages(),
                componentTypes: this.componentTypes()
            }
        });

        dialogRef.afterClosed()
            .pipe(take(1))
            .subscribe((result) => {
                if (result) {
                    this.loadItems();
                }
            });
    }

    protected deleteComponent(componentId: number): void {
        if (!confirm(this.#transloco.translate('admin.components.confirmDelete'))) {
            return;
        }

        this.service.deleteComponent(componentId)
            .pipe(take(1))
            .subscribe({
                next: () => {
                    this.#notify.success('admin.components.success.deleted');
                    this.loadItems();
                },
                error: () => this.#notify.alert('admin.components.errors.deleteFailed')
            });
    }

    openTypesManager(): void {
        const dialogRef = this.#dialog.open(ComponentTypesManagerComponent, {
            width: DIALOG_CONFIG.TYPES_MANAGER.width,
            height: DIALOG_CONFIG.TYPES_MANAGER.height,
            disableClose: false
        });

        dialogRef.afterClosed().pipe(take(1)).subscribe(() => {
            this.#loadComponentTypes();
        });
    }

    selectComponent(component: ComponentDto): void {
        this.componentSelected.emit(component);
    }
}
