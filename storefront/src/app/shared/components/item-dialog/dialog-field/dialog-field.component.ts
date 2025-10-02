import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, input } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { TranslocoService, TranslocoPipe } from '@jsverse/transloco';
import { GeneralFieldConfig, LangFieldConfig } from '@shared/types/item-dialog.types';
import { SpaInputComponent } from '../../custom-ui/spa-input/spa-input.component';
import { SpaSelectComponent, SpaSelectOption } from '../../custom-ui/spa-select/spa-select.component';
import { SpaTextareaComponent } from '../../custom-ui/spa-textarea/spa-textarea.component';

@Component({
  selector: 'spa-dialog-field',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCheckboxModule,
    TranslocoPipe,
    SpaInputComponent,
    SpaTextareaComponent,
    SpaSelectComponent
  ],
  templateUrl: './dialog-field.component.html',
  styleUrls: ['./dialog-field.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DialogFieldComponent {
  #transloco = inject(TranslocoService);

  field = input.required<GeneralFieldConfig | LangFieldConfig>();
  form = input.required<FormGroup>();
  options = input<SpaSelectOption[]>([]);

  getErrorMessage(): string {
    const fieldKey = this.field().key;
    const control = this.form().get(fieldKey);
    const fieldLabel = this.field().labelKey;

    if (!control || !control.errors || !control.touched) {
      return '';
    }

    const translatedField = this.#transloco.translate(fieldLabel);

    if (control.errors['required']) {
      return this.#transloco.translate('admin.common.validation.required', {
        field: translatedField
      });
    }
    if (control.errors['maxlength']) {
      return this.#transloco.translate('admin.common.validation.maxLength', {
        count: control.errors['maxlength'].requiredLength
      });
    }
    if (control.errors['min']) {
      return this.#transloco.translate('admin.common.validation.min', {
        min: control.errors['min'].min
      });
    }
    if (control.errors['max']) {
      return this.#transloco.translate('admin.common.validation.max', {
        max: control.errors['max'].max
      });
    }

    return this.#transloco.translate('admin.common.validation.invalid');
  }

  get isInvalid(): boolean {
    const control = this.form().get(this.field().key);
    return !!(control?.invalid && control?.touched);
  }
}

