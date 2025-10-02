import { Injectable } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import {
  GeneralFieldConfig,
  ItemDialogSchema,
  LangFieldConfig
} from '../types/item-dialog.types';

@Injectable({ providedIn: 'root' })
export class ItemFormBuilderService {
  constructor(private formBuilder: FormBuilder) {}

  buildForms(
    schema: ItemDialogSchema,
    languages: ReadonlyArray<string>,
    initialValues?: Record<string, any>
  ): { generalForm: FormGroup; i18nForms: Map<string, FormGroup> } {
    const generalForm = this.#buildGeneralForm(
      schema.general,
      initialValues
    );
    const i18nForms = this.#buildI18nForms(
      schema.i18n,
      languages,
      initialValues
    );

    return { generalForm, i18nForms };
  }

  #buildGeneralForm(
    fields: ReadonlyArray<GeneralFieldConfig>,
    initialValues?: Record<string, any>
  ): FormGroup {
    const controls: Record<string, any> = {};

    for (const field of fields) {
      const value = initialValues?.[field.key] ?? this.#getDefaultValue(field);
      const validators = this.#getValidators(field);
      controls[field.key] = [value, validators];
    }

    return this.formBuilder.group(controls);
  }

  #buildI18nForms(
    fields: ReadonlyArray<LangFieldConfig>,
    languages: ReadonlyArray<string>,
    initialValues?: Record<string, any>
  ): Map<string, FormGroup> {
    const i18nForms = new Map<string, FormGroup>();

    for (const lang of languages) {
      const controls: Record<string, any> = {};

      for (const field of fields) {
        const langValues = initialValues?.[lang];
        const value = langValues?.[field.key] ?? this.#getDefaultValue(field);
        const validators = this.#getValidators(field);
        controls[field.key] = [value, validators];
      }

      i18nForms.set(lang, this.formBuilder.group(controls));
    }

    return i18nForms;
  }

  #getValidators(field: GeneralFieldConfig | LangFieldConfig): any[] {
    const validators: any[] = [];

    if (field.required) {
      validators.push(Validators.required);
    }

    if (field.maxLength) {
      validators.push(Validators.maxLength(field.maxLength));
    }

    if (field.type === 'number') {
      if (field.minValue !== undefined) {
        validators.push(Validators.min(field.minValue));
      }
      if (field.maxValue !== undefined) {
        validators.push(Validators.max(field.maxValue));
      }
    }

    if (field.type === 'text' || field.type === 'textarea') {
      if (!field.maxLength) {
        validators.push(Validators.maxLength(500));
      }
    }

    return validators;
  }

  #getDefaultValue(field: GeneralFieldConfig | LangFieldConfig): any {
    switch (field.type) {
      case 'checkbox':
        return false;
      case 'number':
        return field.minValue ?? 0;
      case 'select':
        return field.options?.[0]?.value ?? '';
      case 'date':
        return null;
      default:
        return '';
    }
  }
}
