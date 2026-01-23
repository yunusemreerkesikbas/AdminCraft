import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal, computed } from '@angular/core';
import { FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { TranslocoModule } from '@jsverse/transloco';
import { VALIDATION_PATTERNS, VALIDATION_LIMITS } from '@shared/constants/validation.constants';
import { SpaCheckboxComponent } from '@shared/components/custom-ui/spa-checkbox/spa-checkbox.component';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaSelectComponent } from '@shared/components/custom-ui/spa-select/spa-select.component';
import { SpaDialogContentComponent, SpaDialogFooterComponent, SpaDialogHeaderComponent } from '@shared/components/spa-dialog';
import { SpaLocalizedFormDialog } from '@shared/components/spa-localized-form-dialog';
import { AttributeDefinition, CreateAttributeDefinitionRequest, PRODUCT_FIELD_TYPES, ProductFieldType } from '../../models/product-type.types';

export interface ProductAttributeDialogData {
    mode: 'create' | 'edit';
    attribute?: AttributeDefinition;
}

@Component({
    selector: 'spa-product-attribute-dialog',
    templateUrl: './product-attribute-dialog.component.html',
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
        SpaCheckboxComponent
    ]
})
export class ProductAttributeDialogComponent extends SpaLocalizedFormDialog<CreateAttributeDefinitionRequest, ProductAttributeDialogData> {
    override data = inject<ProductAttributeDialogData>(MAT_DIALOG_DATA);

    fieldTypes = PRODUCT_FIELD_TYPES;
    selectedFieldType = signal<ProductFieldType>(this.data.attribute?.fieldType || 'TEXT');

    showTextValidation = computed(() => ['TEXT', 'RICHTEXT'].includes(this.selectedFieldType()));
    showNumberValidation = computed(() => this.selectedFieldType() === 'NUMBER');
    showDateValidation = computed(() => this.selectedFieldType() === 'DATE');

    protected buildGeneralForm(): FormGroup {
        const validationConfig = this.data.attribute?.validationConfig || {};

        const form = this.fb.group({
            code: [this.data.attribute?.code || '', [
                Validators.required,
                Validators.maxLength(VALIDATION_LIMITS.ATTRIBUTE_CODE_MAX),
                Validators.pattern(VALIDATION_PATTERNS.CODE)
            ]],
            name: [this.data.attribute?.name || '', [
                Validators.required,
                Validators.maxLength(VALIDATION_LIMITS.ATTRIBUTE_NAME_MAX)
            ]],
            fieldType: [this.data.attribute?.fieldType || 'TEXT', Validators.required],
            isRequired: [this.data.attribute?.isRequired || false],
            isSearchable: [this.data.attribute?.isSearchable || false],
            sortOrder: [this.data.attribute?.sortOrder || 0],
            // Validation config fields
            minLength: [validationConfig['minLength'] || null],
            maxLength: [validationConfig['maxLength'] || null],
            pattern: [validationConfig['pattern'] || ''],
            minValue: [validationConfig['minValue'] || null],
            maxValue: [validationConfig['maxValue'] || null],
            minDate: [validationConfig['minDate'] || ''],
            maxDate: [validationConfig['maxDate'] || '']
        });

        // Listen to fieldType changes
        form.get('fieldType')?.valueChanges.subscribe((fieldType: ProductFieldType) => {
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
        const validationConfig: Record<string, any> = {};
        const fieldType = formValue.fieldType as ProductFieldType;

        if (['TEXT', 'RICHTEXT'].includes(fieldType)) {
            if (formValue.minLength) validationConfig['minLength'] = formValue.minLength;
            if (formValue.maxLength) validationConfig['maxLength'] = formValue.maxLength;
            if (formValue.pattern) validationConfig['pattern'] = formValue.pattern;
        } else if (fieldType === 'NUMBER') {
            if (formValue.minValue !== null) validationConfig['minValue'] = formValue.minValue;
            if (formValue.maxValue !== null) validationConfig['maxValue'] = formValue.maxValue;
        } else if (fieldType === 'DATE') {
            if (formValue.minDate) validationConfig['minDate'] = formValue.minDate;
            if (formValue.maxDate) validationConfig['maxDate'] = formValue.maxDate;
        }

        const value: CreateAttributeDefinitionRequest = {
            code: formValue.code,
            name: formValue.name,
            fieldType: formValue.fieldType,
            isRequired: formValue.isRequired,
            isSearchable: formValue.isSearchable,
            sortOrder: formValue.sortOrder,
            validationConfig: Object.keys(validationConfig).length > 0 ? validationConfig : undefined
        };

        this.close(value);
    }
}
