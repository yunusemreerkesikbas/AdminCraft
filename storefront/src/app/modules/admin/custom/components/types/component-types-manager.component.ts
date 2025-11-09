import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { NotificationService } from '@shared/notifications/notification.service';
import { ItemDialogService } from '@shared/services/item-dialog.service';
import type { ItemDialogOptions } from '@shared/types/item-dialog.types';
import { take } from 'rxjs';
import { ComponentTypeFormData } from '../models/component-form.types';
import {
    ComponentTypeDto,
    CreateComponentTypeRequest,
    UpdateComponentTypeRequest
} from '../models/component-library.types';
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
    #transloco = inject(TranslocoService);

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

    close(): void {
        this.#dialogRef.close();
    }
}
