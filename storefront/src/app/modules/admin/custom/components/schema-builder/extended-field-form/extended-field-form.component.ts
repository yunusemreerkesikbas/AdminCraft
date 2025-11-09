import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { TranslocoModule } from '@jsverse/transloco';
import { NotificationService } from '@shared/notifications/notification.service';
import { SpaSelectComponent } from '@shared/components/custom-ui/spa-select/spa-select.component';
import { ExtendedFieldDefinition, ExtendedFieldType } from '../../models/component-library.types';
import { ExtendedFieldValidatorService } from '../../validators/extended-field-validator.service';

export interface ExtendedFieldFormData {
    mode: 'create' | 'edit';
    field?: ExtendedFieldDefinition;
    existingKeys?: string[];
}

@Component({
    selector: 'spa-extended-field-form',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatDialogModule,
        MatButtonModule,
        MatFormFieldModule,
        MatInputModule,
        MatCheckboxModule,
        TranslocoModule,
        SpaSelectComponent
    ],
    templateUrl: './extended-field-form.component.html',
    styleUrls: ['./extended-field-form.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ExtendedFieldFormComponent implements OnInit {
    #dialogRef = inject(MatDialogRef<ExtendedFieldFormComponent>);
    #data = inject<ExtendedFieldFormData>(MAT_DIALOG_DATA);
    #fb = inject(FormBuilder);
    #validator = inject(ExtendedFieldValidatorService);
    #notify = inject(NotificationService);

    form!: FormGroup;
    selectedType = signal<ExtendedFieldType>('text');
    isSubmitting = signal(false);

    readonly validationRules = this.#validator.getValidationRules();
    readonly mode = this.#data.mode;
    readonly originalKey = this.#data.field?.key;

    readonly typeOptions = [
        { value: 'text' as ExtendedFieldType, label: 'Text' },
        { value: 'textarea' as ExtendedFieldType, label: 'Textarea' },
        { value: 'number' as ExtendedFieldType, label: 'Number' },
        { value: 'boolean' as ExtendedFieldType, label: 'Boolean (Checkbox)' },
        { value: 'select' as ExtendedFieldType, label: 'Select (Dropdown)' }
    ];
    showTextConstraints = computed(() =>
        this.selectedType() === 'text' || this.selectedType() === 'textarea'
    );
    showNumberConstraints = computed(() => this.selectedType() === 'number');
    showSelectConstraints = computed(() => this.selectedType() === 'select');

    ngOnInit(): void {
        this.#buildForm();
        this.form.get('type')?.valueChanges.subscribe((type: ExtendedFieldType) => {
            this.selectedType.set(type);
            this.#resetConstraints();
        });
    }

    #buildForm(): void {
        const field = this.#data.field;

        this.form = this.#fb.group({
            key: [
                { value: field?.key || '', disabled: this.mode === 'edit' },
                [
                    Validators.required,
                    Validators.maxLength(this.validationRules.MAX_KEY_LENGTH),
                    Validators.pattern(this.validationRules.KEY_PATTERN),
                    (control) => {
                        const value = control.value;
                        if (!value) return null;
                        const existingKeys = this.#data.existingKeys || [];
                        if (this.mode === 'edit' && value === this.originalKey) {
                            return null;
                        }
                        if (existingKeys.includes(value)) {
                            return { duplicate: true };
                        }
                        return null;
                    }
                ]
            ],
            type: [field?.type || 'text', Validators.required],
            label: [
                field?.label || '',
                [Validators.required, Validators.maxLength(this.validationRules.MAX_LABEL_LENGTH)]
            ],
            required: [field?.required || false],
            minLength: [field?.minLength || null],
            maxLength: [field?.maxLength || null],
            pattern: [field?.pattern || ''],
            min: [field?.min || null],
            max: [field?.max || null],
            options: [field?.options?.join('\n') || '']
        });

        if (field?.type) {
            this.selectedType.set(field.type);
        }
    }

    #resetConstraints(): void {
        this.form.patchValue({
            minLength: null,
            maxLength: null,
            pattern: '',
            min: null,
            max: null,
            options: ''
        });
    }

    cancel(): void {
        this.#dialogRef.close(null);
    }

    save(): void {
        if (this.isSubmitting()) return;

        this.form.markAllAsTouched();

        if (!this.form.valid) {
            return;
        }

        this.isSubmitting.set(true);

        const formValue = this.form.getRawValue();
        const field: ExtendedFieldDefinition = {
            key: formValue.key,
            type: formValue.type,
            label: formValue.label,
            required: formValue.required || undefined
        };
        if (formValue.type === 'text' || formValue.type === 'textarea') {
            if (formValue.minLength != null && formValue.minLength !== '') {
                field.minLength = Number(formValue.minLength);
            }
            if (formValue.maxLength != null && formValue.maxLength !== '') {
                field.maxLength = Number(formValue.maxLength);
            }
            if (formValue.pattern && formValue.pattern.trim() !== '') {
                field.pattern = formValue.pattern.trim();
            }
        } else if (formValue.type === 'number') {
            if (formValue.min != null && formValue.min !== '') {
                field.min = Number(formValue.min);
            }
            if (formValue.max != null && formValue.max !== '') {
                field.max = Number(formValue.max);
            }
        } else if (formValue.type === 'select') {
            if (formValue.options && formValue.options.trim() !== '') {
                field.options = formValue.options
                    .split('\n')
                    .map((opt: string) => opt.trim())
                    .filter((opt: string) => opt !== '');
            }
        }
        const errors = this.#validator.validateFieldDefinition(field);
        if (errors.length > 0) {
            this.#notify.alert('admin.components.extendedFields.validationFailed');
            this.isSubmitting.set(false);
            return;
        }

        this.#dialogRef.close(field);
    }

    getErrorMessage(controlName: string): string {
        const control = this.form.get(controlName);
        if (!control || !control.errors) return '';

        if (control.errors['required']) return 'This field is required';
        if (control.errors['maxLength']) {
            const max = control.errors['maxLength'].requiredLength;
            return `Maximum ${max} characters allowed`;
        }
        if (control.errors['pattern']) return 'Invalid format. Must start with a letter and contain only letters, numbers, and underscores';
        if (control.errors['duplicate']) return 'This key already exists';

        return 'Invalid value';
    }
}
