import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, input } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { SpaMediaPickerComponent } from 'app/modules/admin/custom/media/components/spa-media-picker/spa-media-picker.component';
import { SpaCheckboxComponent } from '../spa-checkbox/spa-checkbox.component';
import { SpaInputComponent } from '../spa-input/spa-input.component';
import { SpaSelectComponent } from '../spa-select/spa-select.component';
import { SpaTextareaComponent } from '../spa-textarea/spa-textarea.component';
import { DynamicFieldConfig } from './spa-dynamic-form.types';

@Component({
    selector: 'spa-dynamic-form',
    templateUrl: './spa-dynamic-form.component.html',
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        TranslocoModule,
        SpaInputComponent,
        SpaSelectComponent,
        SpaTextareaComponent,
        SpaCheckboxComponent,
        SpaMediaPickerComponent
    ]
})
export class SpaDynamicFormComponent {
    form = input.required<FormGroup>();
    fields = input.required<DynamicFieldConfig[]>();
    
    translationPrefix = input<string>('admin.common.fields.');

    #transloco = inject(TranslocoService);

    protected getLabel(field: DynamicFieldConfig): string {
        if (field.label) return field.label;
        if (field.labelKey) return this.#transloco.translate(field.labelKey);
        
        const key = this.translationPrefix() + field.key;
        const translation = this.#transloco.translate(key);
        return translation !== key ? translation : this.#humanize(field.key);
    }

    #humanize(str: string): string {
        return str
            .replace(/([A-Z])/g, ' $1')
            .replace(/^./, s => s.toUpperCase())
            .trim();
    }
}
