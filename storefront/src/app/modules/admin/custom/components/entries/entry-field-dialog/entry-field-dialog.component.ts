import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, OnInit, signal } from '@angular/core';
import { ReactiveFormsModule, Validators } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { fuseAnimations } from '@fuse/animations';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaCheckboxComponent } from '@shared/components/custom-ui/spa-checkbox/spa-checkbox.component';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaSelectComponent } from '@shared/components/custom-ui/spa-select/spa-select.component';
import { SpaDialogContentComponent, SpaDialogFooterComponent, SpaDialogHeaderComponent } from '@shared/components/spa-dialog';
import { SpaFormDialog } from '@shared/components/spa-form-dialog';

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
        MatIconModule,
        TranslocoModule,
        SpaInputComponent,
        SpaSelectComponent,
        SpaCheckboxComponent,
        SpaDialogHeaderComponent,
        SpaDialogContentComponent,
        SpaDialogFooterComponent
    ]
})
export class EntryFieldDialogComponent extends SpaFormDialog implements OnInit {
    protected form = this.fb.group({
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
        { value: 'boolean', labelKey: 'admin.components.entryFields.types.boolean' },
        { value: 'media', labelKey: 'admin.components.entryFields.types.media' }
    ];

    readonly #selectedFieldType = signal<string>('text');

    showMaxLength = computed(() => this.#selectedFieldType() === 'text');
    showMinMax = computed(() => this.#selectedFieldType() === 'number');

    override ngOnInit(): void {
        super.ngOnInit();
        this.form.get('fieldType')?.valueChanges.subscribe(value => {
            if (value) {
                this.#selectedFieldType.set(value);
                this.#clearConditionalFields();
            }
        });
    }

    #clearConditionalFields(): void {
        this.form.patchValue({
            maxLength: null,
            minValue: null,
            maxValue: null
        });
    }

    save(): void {
        if (this.form.invalid) return;

        const formValue = this.form.value;
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
