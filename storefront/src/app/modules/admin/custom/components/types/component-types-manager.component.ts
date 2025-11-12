import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { NotificationService } from '@shared/notifications/notification.service';
import { SpaSearchInputComponent } from '@shared/components/custom-ui/spa-search-input/spa-search-input.component';
import { ItemDialogService } from '@shared/services/item-dialog.service';
import { type ItemDialogOptions } from '@shared/types/item-dialog.types';
import { take } from 'rxjs';
import { ComponentTypeFormData } from '../models/component-form.types';
import { ComponentTypeDto, CreateComponentTypeRequest, ExtendedFieldsSchema, UpdateComponentTypeRequest } from '../models/component-library.types';
import { ExtendedFieldsBuilderDialogComponent } from '../schema-builder/extended-fields-builder-dialog/extended-fields-builder-dialog.component';
import { ComponentLibraryService } from '../services/component-library.service';
import { ComponentSchemaBuilderService } from '../services/component-schema-builder.service';

@Component({
    selector: 'spa-component-types-manager',
    templateUrl: './component-types-manager.component.html',
    styleUrls: ['./component-types-manager.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        MatButtonModule,
        MatIconModule,
        MatDialogModule,
        MatTooltipModule,
        TranslocoModule,
        SpaSearchInputComponent
    ]
})
export class ComponentTypesManagerComponent implements OnInit {
    #notify = inject(NotificationService);
    #componentService = inject(ComponentLibraryService);
    #dialog = inject(ItemDialogService);
    #matDialog = inject(MatDialog);
    #schema = inject(ComponentSchemaBuilderService);
    #dialogRef = inject(MatDialogRef<ComponentTypesManagerComponent>);
    #transloco = inject(TranslocoService);

    types = signal<ComponentTypeDto[]>([]);
    isLoading = signal<boolean>(false);
    searchTerm = signal<string>('');

    filteredTypes = computed(() => {
        const term = this.searchTerm().toLowerCase();
        if (!term) return this.types();

        return this.types().filter(t =>
            t.name.toLowerCase().includes(term) ||
            t.code.toLowerCase().includes(term) ||
            (t.category && t.category.toLowerCase().includes(term))
        );
    });

    ngOnInit(): void {
        this.loadTypes();
    }

    loadTypes(): void {
        this.isLoading.set(true);
        this.#componentService.listComponentTypes()
            .pipe(take(1))
            .subscribe({
                next: (types) => {
                    this.types.set(types);
                    this.isLoading.set(false);
                },
                error: () => {
                    this.#notify.alert('admin.components.errors.loadTypesFailed');
                    this.isLoading.set(false);
                }
            });
    }

    createType(): void {
        const schema = this.#schema.buildComponentTypeSchema();
        const initial: ComponentTypeFormData = {
            code: null,
            name: null,
            category: null,
            icon: null,
            extendedFieldsSchema: null
        };

        const options: ItemDialogOptions<ComponentTypeFormData> = {
            titleKey: 'admin.components.types.create',
            mode: 'create',
            schema,
            languages: [],
            initial,
            modalData: { disableClose: true, width: '600px' }
        };

        this.#dialog.open(options)
            .pipe(take(1))
            .subscribe((result) => {
                if (!result) return;

                const payload: CreateComponentTypeRequest = {
                    code: result.code!,
                    name: result.name!,
                    category: result.category || undefined,
                    icon: result.icon || undefined,
                    extendedFieldsSchema: result.extendedFieldsSchema || undefined
                };

                this.#componentService.createComponentType(payload)
                    .pipe(take(1))
                    .subscribe({
                        next: () => {
                            this.#notify.success('admin.components.types.success.created');
                            this.loadTypes();
                        },
                        error: () => this.#notify.alert('admin.components.types.errors.createFailed')
                    });
            });
    }

    editType(type: ComponentTypeDto): void {
        if (type.isSystem) {
            this.#notify.warning('admin.components.types.errors.cannotEditSystem');
            return;
        }

        const schema = this.#schema.buildComponentTypeSchema();
        const initial: ComponentTypeFormData = {
            code: type.code,
            name: type.name,
            category: type.category || null,
            icon: type.icon || null,
            extendedFieldsSchema: type.extendedFieldsSchema || null
        };

        const options: ItemDialogOptions<ComponentTypeFormData, number> = {
            titleKey: 'admin.components.types.edit',
            mode: 'edit',
            schema,
            languages: [],
            initial,
            id: type.id,
            modalData: { disableClose: true, width: '600px' }
        };

        this.#dialog.open(options)
            .pipe(take(1))
            .subscribe((result) => {
                if (!result) return;

                const payload: UpdateComponentTypeRequest = {
                    name: result.name!,
                    category: result.category || undefined,
                    icon: result.icon || undefined,
                    extendedFieldsSchema: result.extendedFieldsSchema || undefined
                };

                this.#componentService.updateComponentType(type.id, payload)
                    .pipe(take(1))
                    .subscribe({
                        next: () => {
                            this.#notify.success('admin.components.types.success.updated');
                            this.loadTypes();
                        },
                        error: () => this.#notify.alert('admin.components.types.errors.updateFailed')
                    });
            });
    }

    manageExtendedFields(type: ComponentTypeDto): void {
        const dialogRef = this.#matDialog.open(ExtendedFieldsBuilderDialogComponent, {
            width: '800px',
            maxHeight: '90vh',
            data: type.extendedFieldsSchema,
            disableClose: true
        });

        dialogRef.afterClosed().pipe(take(1)).subscribe((schema: ExtendedFieldsSchema | null) => {
            if (schema === undefined) return;

            const payload: UpdateComponentTypeRequest = {
                name: type.name,
                category: type.category || undefined,
                icon: type.icon || undefined,
                extendedFieldsSchema: schema || undefined
            };

            this.#componentService.updateComponentType(type.id, payload)
                .pipe(take(1))
                .subscribe({
                    next: () => {
                        this.#notify.success('admin.components.types.success.updated');
                        this.loadTypes();
                    },
                    error: () => this.#notify.alert('admin.components.types.errors.updateFailed')
                });
        });
    }

    deleteType(type: ComponentTypeDto): void {
        if (type.isSystem) {
            this.#notify.warning('admin.components.types.errors.cannotDeleteSystem');
            return;
        }

        if (!confirm(this.#transloco.translate('admin.components.types.confirmDelete', { name: type.name }))) {
            return;
        }

        this.#componentService.deleteComponentType(type.id)
            .pipe(take(1))
            .subscribe({
                next: () => {
                    this.#notify.success('admin.components.types.success.deleted');
                    this.loadTypes();
                },
                error: () => this.#notify.alert('admin.components.types.errors.deleteFailed')
            });
    }

    onSearchChange(term: string): void {
        this.searchTerm.set(term);
    }

    close(): void {
        this.#dialogRef.close();
    }
}
