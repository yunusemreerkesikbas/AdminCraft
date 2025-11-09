import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, HostListener, inject, OnInit, signal } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatTabsModule } from '@angular/material/tabs';
import { TranslocoPipe } from '@jsverse/transloco';
import { DynamicFormComponent } from '@modules/admin/custom/components/dynamic-form/dynamic-form.component';
import { ItemFormBuilderService } from '../../services/item-form-builder.service';
import { GeneralFieldConfig, ItemDialogOptions, LangFieldConfig } from '../../types/item-dialog.types';
import { SpaSelectOption } from '../custom-ui/spa-select/spa-select.component';
import { DialogFieldComponent } from './dialog-field/dialog-field.component';

@Component({
  selector: 'spa-item-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatTabsModule,
    TranslocoPipe,
    DialogFieldComponent,
    DynamicFormComponent
  ],
  templateUrl: './item-dialog.component.html',
  styleUrls: ['./item-dialog.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ItemDialogComponent<TDto = any, TId = string> implements OnInit {
  #dialogRef = inject(MatDialogRef<ItemDialogComponent<TDto, TId>>);
  #data = inject<ItemDialogOptions<TDto, TId>>(MAT_DIALOG_DATA);
  #formBuilder = inject(ItemFormBuilderService);

  readonly options = this.#data;
  readonly isSubmitting = signal(false);

  generalForm!: FormGroup;
  i18nForms = new Map<string, FormGroup>();

  generalFieldsWithOptions: ReadonlyArray<GeneralFieldConfig & { transformedOptions?: SpaSelectOption[] }> = [];
  i18nFieldsWithOptions: ReadonlyArray<LangFieldConfig & { transformedOptions?: SpaSelectOption[] }> = [];

  ngOnInit(): void {
    const mergedInitial = {
      ...this.options.initial as Record<string, any>,
      ...this.options.i18nInitial
    };

    const forms = this.#formBuilder.buildForms(
      this.options.schema,
      this.options.languages,
      mergedInitial
    );

    this.generalForm = forms.generalForm;
    this.i18nForms = forms.i18nForms;

    this.generalFieldsWithOptions = this.options.schema.general.map(field => ({
      ...field,
      transformedOptions: field.type === 'select' ? this.#transformOptions(field) : undefined
    }));

    this.i18nFieldsWithOptions = this.options.schema.i18n.map(field => ({
      ...field,
      transformedOptions: field.type === 'select' ? this.#transformOptions(field) : undefined
    }));
  }

  @HostListener('document:keydown.escape', ['$event'])
  handleEscKey(event: KeyboardEvent): void {
    const disableClose = this.options.modalData?.disableClose ?? true;
    if (disableClose) {
      event.preventDefault();
      event.stopPropagation();
    }
  }

  get generalFields(): ReadonlyArray<GeneralFieldConfig & { transformedOptions?: SpaSelectOption[] }> {
    return this.generalFieldsWithOptions;
  }

  get i18nFields(): ReadonlyArray<LangFieldConfig & { transformedOptions?: SpaSelectOption[] }> {
    return this.i18nFieldsWithOptions;
  }

  #transformOptions(field: GeneralFieldConfig | LangFieldConfig): SpaSelectOption[] {
    if (!field.options) {
      return [];
    }

    return field.options.map(opt => ({
      value: opt.value,
      label: opt.label || opt.labelKey || ''
    }));
  }

  get languages(): ReadonlyArray<string> {
    return this.options.languages;
  }

  getLanguageForm(lang: string): FormGroup | undefined {
    return this.i18nForms.get(lang);
  }

  cancel(): void {
    this.#dialogRef.close(null);
  }

  save(): void {
    if (this.isSubmitting()) {
      return;
    }

    this.generalForm.markAllAsTouched();
    this.i18nForms.forEach(form => form.markAllAsTouched());

    if (!this.#isValid()) {
      return;
    }

    this.isSubmitting.set(true);

    const result = this.#buildDto();
    this.#dialogRef.close(result);
  }

  #isValid(): boolean {
    let isValid = this.generalForm.valid;

    for (const form of this.i18nForms.values()) {
      // Only validate language forms that have content
      // This allows users to edit a single language without filling all tabs
      const hasContent = this.#formHasContent(form);

      if (hasContent && !form.valid) {
        isValid = false;
        break;
      }
    }

    return isValid;
  }

  #formHasContent(form: FormGroup): boolean {
    const values = form.value;

    // Check if any field has a non-empty value
    return Object.values(values).some(value => {
      if (value === null || value === undefined) {
        return false;
      }

      if (typeof value === 'string') {
        return value.trim().length > 0;
      }

      if (typeof value === 'boolean') {
        return true; // Checkboxes always have a value
      }

      if (typeof value === 'number') {
        return true; // Numbers always have a value
      }

      if (Array.isArray(value)) {
        return value.length > 0;
      }

      return true;
    });
  }

  #buildDto(): Partial<TDto> {
    const dto: Record<string, any> = {
      ...this.generalForm.value
    };

    for (const [lang, form] of this.i18nForms.entries()) {
      dto[lang] = form.value;
    }

    if (this.options.id) {
      dto['id'] = this.options.id;
    }

    return dto as Partial<TDto>;
  }

}
