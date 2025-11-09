import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { TranslocoModule } from '@jsverse/transloco';
import { NotificationService } from '@shared/notifications/notification.service';
import { ItemDialogService } from '@shared/services/item-dialog.service';
import type { ItemDialogOptions } from '@shared/types/item-dialog.types';
import { take } from 'rxjs';
import { ComponentLibraryService } from '../services/component-library.service';
import { ComponentSchemaBuilderService } from '../services/component-schema-builder.service';
import {
    ComponentTypeDto,
    CreateComponentTypeRequest,
    UpdateComponentTypeRequest
} from '../models/component-library.types';
import { ComponentTypeFormData } from '../models/component-form.types';

@Component({
    selector: 'spa-component-types-manager',
    templateUrl: './component-types-manager.component.html',
    styleUrls: ['./component-types-manager.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: true,
    imports: [
        CommonModule,
        MatButtonModule,
        MatIconModule,
        MatDialogModule,
        TranslocoModule
    ]
})
export class ComponentTypesManagerComponent {
    #notify = inject(NotificationService);
    #componentService = inject(ComponentLibraryService);
    #dialog = inject(ItemDialogService);
    #schema = inject(ComponentSchemaBuilderService);
    #dialogRef = inject(MatDialogRef<ComponentTypesManagerComponent>);

    types = signal<ComponentTypeDto[]>([]);
    isLoading = signal<boolean>(false);

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
                    this.#notify.alert('Failed to load component types');
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
            icon: null
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
                    icon: result.icon || undefined
                };

                this.#componentService.createComponentType(payload)
                    .pipe(take(1))
                    .subscribe({
                        next: () => {
                            this.#notify.success('Component type created');
                            this.loadTypes();
                        },
                        error: () => this.#notify.alert('Failed to create component type')
                    });
            });
    }

    editType(type: ComponentTypeDto): void {
        if (type.isSystem) {
            this.#notify.warning('Cannot edit system component type');
            return;
        }

        const schema = this.#schema.buildComponentTypeSchema();
        const initial: ComponentTypeFormData = {
            code: type.code,
            name: type.name,
            category: type.category || null,
            icon: type.icon || null
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
                    icon: result.icon || undefined
                };

                this.#componentService.updateComponentType(type.id, payload)
                    .pipe(take(1))
                    .subscribe({
                        next: () => {
                            this.#notify.success('Component type updated');
                            this.loadTypes();
                        },
                        error: () => this.#notify.alert('Failed to update component type')
                    });
            });
    }

    deleteType(type: ComponentTypeDto): void {
        if (type.isSystem) {
            this.#notify.warning('Cannot delete system component type');
            return;
        }

        if (!confirm(`Are you sure you want to delete "${type.name}"?`)) {
            return;
        }

        this.#componentService.deleteComponentType(type.id)
            .pipe(take(1))
            .subscribe({
                next: () => {
                    this.#notify.success('Component type deleted');
                    this.loadTypes();
                },
                error: () => this.#notify.alert('Failed to delete component type')
            });
    }

    close(): void {
        this.#dialogRef.close();
    }
}
