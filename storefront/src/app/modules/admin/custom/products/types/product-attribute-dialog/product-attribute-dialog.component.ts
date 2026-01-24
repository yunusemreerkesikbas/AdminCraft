import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { TranslocoModule } from '@jsverse/transloco';
import { VALIDATION_LIMITS } from '@shared/constants/validation.constants';
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
        SpaSelectComponent
    ]
})
export class ProductAttributeDialogComponent extends SpaLocalizedFormDialog<CreateAttributeDefinitionRequest, ProductAttributeDialogData> {
    override data = inject<ProductAttributeDialogData>(MAT_DIALOG_DATA);

    fieldTypes = PRODUCT_FIELD_TYPES;

    protected buildGeneralForm(): FormGroup {
        const form = this.fb.group({
            name: [this.data.attribute?.name || '', [
                Validators.required,
                Validators.maxLength(VALIDATION_LIMITS.ATTRIBUTE_NAME_MAX)
            ]],
            fieldType: [this.data.attribute?.fieldType || 'TEXT', Validators.required]
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

        const value: CreateAttributeDefinitionRequest = {
            name: formValue.name,
            fieldType: formValue.fieldType
        };

        this.close(value);
    }
}
