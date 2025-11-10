import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, Input, OnInit } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ExtendedFieldsSchema } from '../models/component-library.types';
import { ExtendedFieldValidatorService } from '../validators/extended-field-validator.service';
import { DynamicFieldComponent } from './dynamic-field/dynamic-field.component';

@Component({
    selector: 'spa-dynamic-form',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, DynamicFieldComponent],
    templateUrl: './dynamic-form.component.html',
    styleUrls: ['./dynamic-form.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class DynamicFormComponent implements OnInit {
    @Input({ required: true }) schema!: ExtendedFieldsSchema | null;
    @Input({ required: true }) formGroup!: FormGroup;

    #validator = inject(ExtendedFieldValidatorService);

    ngOnInit(): void {
        if (!this.schema || !this.schema.i18n) return;

        this.schema.i18n.forEach(field => {
            const validators = this.#validator.buildValidators(field);
            const control = new FormControl('', validators);
            this.formGroup.addControl(field.key, control);
        });
    }

    getControl(key: string): FormControl {
        return this.formGroup.get(key) as FormControl;
    }
}
