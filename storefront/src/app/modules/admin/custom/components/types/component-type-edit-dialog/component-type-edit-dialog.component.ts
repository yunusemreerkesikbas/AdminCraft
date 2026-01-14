import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit } from '@angular/core';
import { FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { fuseAnimations } from '@fuse/animations';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaSelectComponent } from '@shared/components/custom-ui/spa-select/spa-select.component';
import { SpaDialogContentComponent, SpaDialogFooterComponent, SpaDialogHeaderComponent } from '@shared/components/spa-dialog';
import { SpaDialogData } from '@shared/components/spa-dialog-base/spa-dialog-base.types';
import { SpaFormDialog } from '@shared/components/spa-form-dialog';
import { VALIDATION_LIMITS } from '@shared/constants/validation.constants';
import { take, takeUntil } from 'rxjs';
import { EntryFieldsBuilderComponent } from '../../entries/entry-fields-builder/entry-fields-builder.component';
import { COMPONENT_CATEGORIES } from '../../models/component-categories.constants';
import { ComponentTypeDto, UpdateComponentTypeRequest } from '../../models/component-library.types';
import { ComponentTypeService } from '../../services/component-type.service';

interface DialogData extends SpaDialogData {
    type: ComponentTypeDto;
}

@Component({
    selector: 'spa-component-type-edit-dialog',
    templateUrl: './component-type-edit-dialog.component.html',
    styleUrls: ['./component-type-edit-dialog.component.scss'],
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush,
    animations: fuseAnimations,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatTabsModule,
        MatIconModule,
        MatButtonModule,
        TranslocoModule,
        EntryFieldsBuilderComponent,
        SpaInputComponent,
        SpaSelectComponent,
        SpaDialogHeaderComponent,
        SpaDialogContentComponent,
        SpaDialogFooterComponent
    ]
})
export class ComponentTypeEditDialogComponent extends SpaFormDialog<ComponentTypeDto, DialogData> implements OnInit {
    readonly #service = inject(ComponentTypeService);

    protected form!: FormGroup;
    categoryOptions = COMPONENT_CATEGORIES;

    override ngOnInit(): void {
        const typeData = this.data!.type;
        this.form = this.fb.group({
            name: [typeData.name, [
                Validators.required,
                Validators.maxLength(VALIDATION_LIMITS.COMPONENT_TYPE_NAME_MAX)
            ]],
            category: [typeData.category || null, [
                Validators.maxLength(VALIDATION_LIMITS.COMPONENT_TYPE_CATEGORY_MAX)
            ]]
        });
    }

    save(): void {
        if (this.form.invalid) return;
        if (this.isSubmitting()) return;

        const payload: UpdateComponentTypeRequest = {
            name: this.form.value.name!,
            category: this.form.value.category || undefined
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
