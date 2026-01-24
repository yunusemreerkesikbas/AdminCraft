import { CommonModule } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    inject,
} from '@angular/core';
import { FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaSelectComponent } from '@shared/components/custom-ui/spa-select/spa-select.component';
import {
    SpaDialogContentComponent,
    SpaDialogFooterComponent,
    SpaDialogHeaderComponent,
} from '@shared/components/spa-dialog';
import { SpaLocalizedFormDialog } from '@shared/components/spa-localized-form-dialog';
import {
    VALIDATION_LIMITS,
    VALIDATION_PATTERNS,
} from '@shared/constants/validation.constants';
import {
    CreateProductFieldRequest,
    ProductFieldDefinition,
    ProductFieldType,
} from '../../models/product-field.types';

export const GLOBAL_FIELD_TYPES: { value: ProductFieldType; label: string }[] =
    [
        { value: 'TEXT', label: 'Text' },
        { value: 'RICHTEXT', label: 'Rich Text' },
        { value: 'NUMBER', label: 'Number' },
        { value: 'BOOLEAN', label: 'Boolean' },
        { value: 'DATE', label: 'Date' },
        { value: 'MEDIA', label: 'Media' },
    ];

export interface ProductFieldDialogData {
    mode: 'create' | 'edit';
    field?: ProductFieldDefinition;
}

@Component({
    selector: 'spa-product-field-dialog',
    templateUrl: './product-field-dialog.component.html',
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        TranslocoModule,
        MatDialogModule,
        SpaDialogHeaderComponent,
        SpaDialogContentComponent,
        SpaDialogFooterComponent,
        SpaInputComponent,
        SpaSelectComponent,
    ],
})
export class ProductFieldDialogComponent extends SpaLocalizedFormDialog<
    CreateProductFieldRequest,
    ProductFieldDialogData
> {
    override data = inject<ProductFieldDialogData>(MAT_DIALOG_DATA);

    fieldTypes = GLOBAL_FIELD_TYPES;

    protected buildGeneralForm(): FormGroup {
        const form = this.fb.group({
            uid: [
                this.data.field?.uid,
                [
                    Validators.required,
                    Validators.maxLength(VALIDATION_LIMITS.FIELD_UID_MAX),
                    Validators.pattern(VALIDATION_PATTERNS.UID),
                ],
            ],
            name: [
                this.data.field?.name,
                [
                    Validators.required,
                    Validators.maxLength(VALIDATION_LIMITS.FIELD_NAME_MAX),
                ],
            ],
            fieldType: [this.data.field?.fieldType, Validators.required],
        });

        return form;
    }

    protected buildI18nForm(lang: string): FormGroup {
        return this.fb.group({});
    }

    save(): void {
        if (this.generalForm.invalid) {
            this.generalForm.markAllAsTouched();
            return;
        }

        const formValue = this.generalForm.value;

        const value: CreateProductFieldRequest = {
            uid: formValue.uid,
            name: formValue.name,
            fieldType: formValue.fieldType,
        };

        this.close(value);
    }
}
