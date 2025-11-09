import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, forwardRef, inject, signal, ViewEncapsulation } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { NotificationService } from '@shared/notifications/notification.service';
import { ExtendedFieldDefinition, ExtendedFieldsSchema } from '../../models/component-library.types';
import { ExtendedFieldValidatorService } from '../../validators/extended-field-validator.service';
import { ExtendedFieldFormComponent, ExtendedFieldFormData } from '../extended-field-form/extended-field-form.component';

@Component({
    selector: 'spa-extended-fields-builder',
    standalone: true,
    imports: [
        CommonModule,
        MatButtonModule,
        MatIconModule,
        MatTooltipModule,
        TranslocoModule
    ],
    templateUrl: './extended-fields-builder.component.html',
    styleUrls: ['./extended-fields-builder.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => ExtendedFieldsBuilderComponent),
            multi: true
        }
    ]
})
export class ExtendedFieldsBuilderComponent implements ControlValueAccessor {
    #dialog = inject(MatDialog);
    #validator = inject(ExtendedFieldValidatorService);
    #notify = inject(NotificationService);
    #transloco = inject(TranslocoService);

    fields = signal<ExtendedFieldDefinition[]>([]);
    disabled = false;

    readonly validationRules = this.#validator.getValidationRules();

    #onChange: (val: ExtendedFieldsSchema | null) => void = () => {};
    #onTouched: () => void = () => {};

    writeValue(value: ExtendedFieldsSchema | null | undefined): void {
        if (value?.i18n && Array.isArray(value.i18n)) {
            this.fields.set([...value.i18n]);
        } else {
            this.fields.set([]);
        }
    }

    registerOnChange(fn: any): void {
        this.#onChange = fn;
    }

    registerOnTouched(fn: any): void {
        this.#onTouched = fn;
    }

    setDisabledState(isDisabled: boolean): void {
        this.disabled = isDisabled;
    }

    addField(): void {
        if (this.disabled) return;

        if (this.fields().length >= this.validationRules.MAX_FIELDS) {
            this.#notify.warning(
                this.#transloco.translate('admin.components.extendedFields.maxFieldsReached', {
                    max: this.validationRules.MAX_FIELDS
                })
            );
            return;
        }

        const data: ExtendedFieldFormData = {
            mode: 'create',
            existingKeys: this.fields().map(f => f.key)
        };

        const dialogRef = this.#dialog.open(ExtendedFieldFormComponent, {
            width: '600px',
            maxHeight: '90vh',
            data,
            disableClose: true
        });

        dialogRef.afterClosed().subscribe((result: ExtendedFieldDefinition | null) => {
            if (!result) return;

            const newFields = [...this.fields(), result];
            this.#updateFields(newFields);
        });
    }

    editField(field: ExtendedFieldDefinition, index: number): void {
        if (this.disabled) return;

        const data: ExtendedFieldFormData = {
            mode: 'edit',
            field: { ...field },
            existingKeys: this.fields().map(f => f.key)
        };

        const dialogRef = this.#dialog.open(ExtendedFieldFormComponent, {
            width: '600px',
            maxHeight: '90vh',
            data,
            disableClose: true
        });

        dialogRef.afterClosed().subscribe((result: ExtendedFieldDefinition | null) => {
            if (!result) return;

            const newFields = [...this.fields()];
            newFields[index] = result;
            this.#updateFields(newFields);
        });
    }

    deleteField(index: number): void {
        if (this.disabled) return;

        const field = this.fields()[index];
        const confirmMsg = this.#transloco.translate('admin.components.extendedFields.confirmDelete', {
            name: field.label || field.key
        });

        if (!confirm(confirmMsg)) {
            return;
        }

        const newFields = this.fields().filter((_, i) => i !== index);
        this.#updateFields(newFields);
    }

    moveUp(index: number): void {
        if (this.disabled || index === 0) return;

        const newFields = [...this.fields()];
        [newFields[index - 1], newFields[index]] = [newFields[index], newFields[index - 1]];
        this.#updateFields(newFields);
    }

    moveDown(index: number): void {
        if (this.disabled || index === this.fields().length - 1) return;

        const newFields = [...this.fields()];
        [newFields[index], newFields[index + 1]] = [newFields[index + 1], newFields[index]];
        this.#updateFields(newFields);
    }

    #updateFields(newFields: ExtendedFieldDefinition[]): void {
        this.fields.set(newFields);
        const schema: ExtendedFieldsSchema = { i18n: newFields };

        const validation = this.#validator.validateSchema(schema);
        if (!validation.valid) {
            console.warn('Schema validation warnings:', validation.errors);
        }

        this.#onChange(newFields.length > 0 ? schema : null);
        this.#onTouched();
    }

    getJsonPreview(): string {
        if (this.fields().length === 0) {
            return '{\n  "i18n": []\n}';
        }

        const schema: ExtendedFieldsSchema = { i18n: this.fields() };
        return JSON.stringify(schema, null, 2);
    }

    getFieldTypeLabel(type: string): string {
        const typeMap: Record<string, string> = {
            text: 'Text',
            textarea: 'Textarea',
            number: 'Number',
            boolean: 'Boolean',
            select: 'Select'
        };
        return typeMap[type] || type;
    }

    getFieldConstraintsLabel(field: ExtendedFieldDefinition): string {
        const parts: string[] = [];

        if (field.required) {
            parts.push('required');
        }

        if (field.type === 'text' || field.type === 'textarea') {
            if (field.minLength !== undefined) {
                parts.push(`min: ${field.minLength}`);
            }
            if (field.maxLength !== undefined) {
                parts.push(`max: ${field.maxLength}`);
            }
            if (field.pattern) {
                parts.push('pattern');
            }
        } else if (field.type === 'number') {
            if (field.min !== undefined) {
                parts.push(`min: ${field.min}`);
            }
            if (field.max !== undefined) {
                parts.push(`max: ${field.max}`);
            }
        } else if (field.type === 'select') {
            if (field.options) {
                parts.push(`${field.options.length} options`);
            }
        }

        return parts.length > 0 ? `(${parts.join(', ')})` : '';
    }
}
