import { ChangeDetectionStrategy, Component, inject, OnInit, ViewEncapsulation } from '@angular/core';
import { FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { fuseAnimations } from '@fuse/animations';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaCheckboxComponent } from '@shared/components/custom-ui/spa-checkbox/spa-checkbox.component';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaSelectComponent } from '@shared/components/custom-ui/spa-select/spa-select.component';
import { SpaDialogComponent } from '@shared/components/spa-dialog';
import { SpaDialogData } from '@shared/components/spa-dialog-base/spa-dialog-base.types';
import { SpaFormDialog } from '@shared/components/spa-form-dialog';
import {
    SpaTabContainerComponent,
    SpaTabContentDirective,
    TabDefinition
} from '@shared/components/spa-tab-container';
import { VALIDATION_LIMITS } from '@shared/constants/validation.constants';
import { take, takeUntil } from 'rxjs';
import { EntryFieldsBuilderComponent } from '../../entries/entry-fields-builder/entry-fields-builder.component';
import { COMPONENT_CATEGORIES } from '../../models/component-categories.constants';
import {
    ComponentTypeDto,
    UpdateComponentTypeRequest
} from '../../models/component-library.types';
import { ComponentTypeService } from '../../services/component-type.service';

interface DialogData extends SpaDialogData {
    type: ComponentTypeDto;
}

@Component({
    selector: 'spa-component-type-edit-dialog',
    templateUrl: './component-type-edit-dialog.component.html',
    styleUrls: ['./component-type-edit-dialog.component.scss'],
    standalone: true,
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    animations: fuseAnimations,
    imports: [
        ReactiveFormsModule,
        MatIconModule,
        MatButtonModule,
        TranslocoModule,
        EntryFieldsBuilderComponent,
        SpaInputComponent,
        SpaSelectComponent,
        SpaCheckboxComponent,
        SpaDialogComponent,
        SpaTabContainerComponent,
        SpaTabContentDirective
    ]
})
export class ComponentTypeEditDialogComponent extends SpaFormDialog<ComponentTypeDto, DialogData> implements OnInit {
    readonly #service = inject(ComponentTypeService);

    protected form!: FormGroup;
    protected categoryOptions = COMPONENT_CATEGORIES;
    protected readonly tabs: TabDefinition[] = [
        { id: 'general', label: 'admin.components.types.tabs.general', icon: 'settings' },
        { id: 'entryFields', label: 'admin.components.types.tabs.entryFields', icon: 'extension' }
    ];

    override ngOnInit(): void {
        const typeData = this.data!.type;
        this.form = this.fb.group({
            name: [typeData.name, [
                Validators.required,
                Validators.maxLength(VALIDATION_LIMITS.COMPONENT_TYPE_NAME_MAX)
            ]],
            category: [typeData.category || null, [
                Validators.maxLength(VALIDATION_LIMITS.COMPONENT_TYPE_CATEGORY_MAX)
            ]],
            navigationAware: [typeData.navigationAware ?? false]
        });
    }

    save(): void {
        if (this.form.invalid) return;
        if (this.isSubmitting()) return;

        const payload: UpdateComponentTypeRequest = {
            name: this.form.value.name!,
            category: this.form.value.category || undefined,
            navigationAware: !!this.form.value.navigationAware
        };

        this.setSubmitting(true);
        this.#service.update(this.data!.type.id, payload)
            .pipe(take(1), takeUntil(this.destroy$))
            .subscribe({
                next: (result) => {
                    this.setSubmitting(false);
                    this.notify.success('admin.components.types.success.updated');
                    this.close(result);
                },
                error: () => {
                    this.setSubmitting(false);
                    this.notify.alert('admin.components.types.errors.updateFailed');
                }
            });
    }
}
