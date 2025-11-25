import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { TranslocoModule } from '@jsverse/transloco';
import { BaseDialogComponent } from '@shared/components/base-dialog';
import { FuseCardComponent } from '@fuse/components/card';
import { fuseAnimations } from '@fuse/animations';

@Component({
    selector: 'spa-entry-field-dialog',
    templateUrl: './entry-field-dialog.component.html',
    styleUrls: ['./entry-field-dialog.component.scss'],
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush,
    animations: fuseAnimations,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatDialogModule,
        MatFormFieldModule,
        MatIconModule,
        MatInputModule,
        MatSelectModule,
        MatCheckboxModule,
        MatButtonModule,
        TranslocoModule,
        FuseCardComponent
    ]
})
export class EntryFieldDialogComponent extends BaseDialogComponent {
    #fb = inject(FormBuilder);

    fieldForm = this.#fb.group({
        fieldKey: ['', [Validators.required, Validators.pattern(/^[a-z][a-zA-Z0-9]{0,49}$/)]],
        fieldType: ['text', Validators.required],
        isRequired: [false],
        maxLength: [null as number | null],
        minValue: [null as number | null],
        maxValue: [null as number | null]
    });

    fieldTypeOptions = [
        { value: 'text', labelKey: 'admin.components.entryFields.types.text' },
        { value: 'textarea', labelKey: 'admin.components.entryFields.types.textarea' },
        { value: 'number', labelKey: 'admin.components.entryFields.types.number' },
        { value: 'boolean', labelKey: 'admin.components.entryFields.types.boolean' }
    ];

    selectedFieldType = signal<string>('text');

    showMaxLength = computed(() => this.selectedFieldType() === 'text');
    showMinMax = computed(() => this.selectedFieldType() === 'number');

    ngOnInit(): void {
        this.fieldForm.get('fieldType')?.valueChanges.subscribe(value => {
            if (value) {
                this.selectedFieldType.set(value);
                this.#clearConditionalFields();
            }
        });
    }

    #clearConditionalFields(): void {
        this.fieldForm.patchValue({
            maxLength: null,
            minValue: null,
            maxValue: null
        });
    }

    save(): void {
        this.onSave();
    }

    protected onSave(): void {
        if (this.fieldForm.invalid) return;

        const formValue = this.fieldForm.value;
        const result = {
            fieldKey: formValue.fieldKey!,
            fieldType: formValue.fieldType!,
            isRequired: formValue.isRequired || false,
            ...(formValue.maxLength && { maxLength: formValue.maxLength }),
            ...(formValue.minValue !== null && { minValue: formValue.minValue }),
            ...(formValue.maxValue !== null && { maxValue: formValue.maxValue })
        };

        this.close(result);
    }
}

