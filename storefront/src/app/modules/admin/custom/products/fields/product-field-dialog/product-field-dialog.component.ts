import { CommonModule } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    computed,
    inject,
    signal,
} from '@angular/core';
import { FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaCheckboxComponent } from '@shared/components/custom-ui/spa-checkbox/spa-checkbox.component';
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
        SpaCheckboxComponent,
    ],
})
export class ProductFieldDialogComponent extends SpaLocalizedFormDialog<
    CreateProductFieldRequest,
    ProductFieldDialogData
> {
    override data = inject<ProductFieldDialogData>(MAT_DIALOG_DATA);

    fieldTypes = GLOBAL_FIELD_TYPES;
    selectedFieldType = signal<ProductFieldType>(
        this.data.field?.fieldType || 'TEXT'
    );

    showTextValidation = computed(() =>
        ['TEXT', 'RICHTEXT'].includes(this.selectedFieldType())
    );
    showNumberValidation = computed(
        () => this.selectedFieldType() === 'NUMBER'
    );
    showDateValidation = computed(() => this.selectedFieldType() === 'DATE');

    protected buildGeneralForm(): FormGroup {
        const validationConfig = this.data.field?.validationConfig || {};

        const form = this.fb.group({
            uid: [
                this.data.field?.uid,
                [
                    Validators.required,
                    Validators.maxLength(VALIDATION_LIMITS.FIELD_UID_MAX),
                    Validators.pattern(VALIDATION_PATTERNS.UID),
                ],
            ],
            code: [
                this.data.field?.code,
                [
                    Validators.required,
                    Validators.maxLength(VALIDATION_LIMITS.FIELD_CODE_MAX),
                    Validators.pattern(VALIDATION_PATTERNS.CODE),
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
            isRequired: [this.data.field?.isRequired],
            isVisibleInList: [this.data.field?.isVisibleInList],
            sortOrder: [this.data.field?.sortOrder],
            defaultValue: [this.data.field?.defaultValue],
            // Validation config fields
            minLength: [validationConfig['minLength']],
            maxLength: [validationConfig['maxLength']],
            pattern: [validationConfig['pattern']],
            minValue: [validationConfig['minValue']],
            maxValue: [validationConfig['maxValue']],
            minDate: [validationConfig['minDate']],
            maxDate: [validationConfig['maxDate']],
        });

        // Listen to fieldType changes
        form
            .get('fieldType')
            ?.valueChanges.subscribe((fieldType: ProductFieldType) => {
                this.selectedFieldType.set(fieldType);
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

        // Build validationConfig based on field type
        const validationConfig: Record<string, unknown> = {};
        const fieldType = formValue.fieldType as ProductFieldType;

        if (['TEXT', 'RICHTEXT'].includes(fieldType)) {
            if (formValue.minLength)
                validationConfig['minLength'] = formValue.minLength;
            if (formValue.maxLength)
                validationConfig['maxLength'] = formValue.maxLength;
            if (formValue.pattern)
                validationConfig['pattern'] = formValue.pattern;
        } else if (fieldType === 'NUMBER') {
            if (formValue.minValue !== null)
                validationConfig['minValue'] = formValue.minValue;
            if (formValue.maxValue !== null)
                validationConfig['maxValue'] = formValue.maxValue;
        } else if (fieldType === 'DATE') {
            if (formValue.minDate)
                validationConfig['minDate'] = formValue.minDate;
            if (formValue.maxDate)
                validationConfig['maxDate'] = formValue.maxDate;
        }

        const value: CreateProductFieldRequest = {
            uid: formValue.uid,
            code: formValue.code,
            name: formValue.name,
            fieldType: formValue.fieldType,
            isRequired: formValue.isRequired,
            isVisibleInList: formValue.isVisibleInList,
            sortOrder: formValue.sortOrder,
            defaultValue: formValue.defaultValue || undefined,
            validationConfig:
                Object.keys(validationConfig).length > 0
                    ? validationConfig
                    : undefined,
        };

        this.close(value);
    }
}
