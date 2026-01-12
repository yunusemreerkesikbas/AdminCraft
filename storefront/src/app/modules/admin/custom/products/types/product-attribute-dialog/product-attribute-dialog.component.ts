import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaCheckboxComponent } from '@shared/components/custom-ui/spa-checkbox/spa-checkbox.component';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaSelectComponent } from '@shared/components/custom-ui/spa-select/spa-select.component';
import { SpaDialogContentComponent, SpaDialogFooterComponent, SpaDialogHeaderComponent } from '@shared/components/spa-dialog';
import { SpaLocalizedFormDialog } from '@shared/components/spa-localized-form-dialog';
import { AttributeDefinition, CreateAttributeDefinitionRequest, PRODUCT_FIELD_TYPES } from '../../models/product-type.types';

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

    protected buildGeneralForm(): FormGroup {
        return this.fb.group({
            code: [this.data.attribute?.code || '', [Validators.required, Validators.pattern(/^[a-z0-9_]+$/)]],
            name: [this.data.attribute?.name || '', Validators.required],
            fieldType: [this.data.attribute?.fieldType || 'TEXT', Validators.required],
            isRequired: [this.data.attribute?.isRequired || false],
            isSearchable: [this.data.attribute?.isSearchable || false],
            sortOrder: [this.data.attribute?.sortOrder || 0]
        });
    }

    protected buildI18nForm(lang: string): FormGroup {
        return this.fb.group({}); 
    }

    save(): void {
        if (this.generalForm.invalid) {
            this.generalForm.markAllAsTouched();
            return;
        }

        const value: CreateAttributeDefinitionRequest = this.generalForm.value;
        this.close(value);
    }
}
