import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { TranslocoPipe } from '@jsverse/transloco';
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
  field = input.required<GeneralFieldConfig | LangFieldConfig>();
  form = input.required<FormGroup>();
  options = input<SpaSelectOption[]>([]);

  getErrorMessage(): string {
    const fieldKey = this.field().key;
    const control = this.form().get(fieldKey);
    
    if (!control || !control.errors || !control.touched) {
      return '';
    }

    if (control.errors['required']) {
      return 'admin.common.validation.required';
    }
    if (control.errors['maxlength']) {
      return 'admin.common.validation.maxLength';
    }
    if (control.errors['min']) {
      return 'admin.common.validation.min';
    }
    if (control.errors['max']) {
      return 'admin.common.validation.max';
    }

    return 'admin.common.validation.invalid';
  }

  get isInvalid(): boolean {
    const control = this.form().get(this.field().key);
    return !!(control?.invalid && control?.touched);
  }
}

