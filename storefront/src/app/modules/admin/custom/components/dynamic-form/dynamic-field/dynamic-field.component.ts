import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, Input } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { SpaSelectComponent } from '@shared/components/custom-ui/spa-select/spa-select.component';
import { SpaTextareaComponent } from '@shared/components/custom-ui/spa-textarea/spa-textarea.component';
import { ExtendedFieldDefinition } from '../../models/component-library.types';

@Component({
    selector: 'spa-dynamic-field',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatCheckboxModule, SpaSelectComponent, SpaTextareaComponent],
    templateUrl: './dynamic-field.component.html',
    styleUrls: ['./dynamic-field.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class DynamicFieldComponent {
    @Input({ required: true }) field!: ExtendedFieldDefinition;
    @Input({ required: true }) control!: FormControl;

    selectOptions = computed(() => {
        if (this.field.type !== 'select' || !this.field.options) return [];
        return this.field.options.map(opt => ({ value: opt, label: opt }));
    });

    getErrorMessage(): string {
        if (!this.control.errors) return '';
        if (this.control.errors['required']) return 'This field is required';
        if (this.control.errors['minlength']) {
            const min = this.control.errors['minlength'].requiredLength;
            return `Minimum ${min} characters required`;
        }
        if (this.control.errors['maxlength']) {
            const max = this.control.errors['maxlength'].requiredLength;
            return `Maximum ${max} characters allowed`;
        }
        if (this.control.errors['min']) return `Minimum value is ${this.field.min}`;
        if (this.control.errors['max']) return `Maximum value is ${this.field.max}`;
        if (this.control.errors['pattern']) return 'Invalid format';
        return 'Invalid value';
    }
}
