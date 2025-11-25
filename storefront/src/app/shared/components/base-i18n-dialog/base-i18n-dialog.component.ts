import { Directive, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { CrudEntity } from '@core/crud/api.types';
import { BaseCrudDialogComponent } from '../base-crud-dialog/base-crud-dialog.component';
import { I18nDialogData } from '../base-dialog/base-dialog.types';

@Directive()
export abstract class BaseI18nDialogComponent<
  TDto extends CrudEntity,
  TCreate = Partial<TDto>,
  TUpdate = Partial<TDto>
> extends BaseCrudDialogComponent<TDto, TCreate, TUpdate> implements OnInit {

  protected override readonly data!: I18nDialogData<TDto>;

  generalForm!: FormGroup;
  i18nForms: Record<string, FormGroup> = {};

  protected get form(): FormGroup {
    return this.generalForm;
  }

  protected set form(value: FormGroup) {
    this.generalForm = value;
  }

  get languages(): ReadonlyArray<string> {
    return this.data?.languages || ['TR', 'EN'];
  }

  override ngOnInit(): void {
    this.buildForms();
    this.initializeForm();
  }

  protected buildForms(): void {
    this.generalForm = this.buildGeneralForm();
    this.languages.forEach(lang => {
      this.i18nForms[lang] = this.buildI18nForm(lang);
    });
  }

  protected override initializeForm(): void {
    if (this.data?.initial && this.generalForm) {
      this.generalForm.patchValue(this.data.initial);
    }
    if (this.data?.i18nInitial) {
      for (const [lang, data] of Object.entries(this.data.i18nInitial)) {
        const form = this.i18nForms[lang];
        if (form && data) {
          form.patchValue(data);
        }
      }
    }
  }

  protected override onSave(): void {
    this.generalForm.markAllAsTouched();
    Object.values(this.i18nForms).forEach(form => form.markAllAsTouched());
    if (!this.isValid()) {
      this.notify.warning('admin.validation.invalidForm');
      return;
    }
    super.onSave();
  }

  protected saveTab(tab: 'general' | string): void {
    const form = tab === 'general' ? this.generalForm : this.i18nForms[tab];

    if (!form) {
      console.warn(`Form not found for tab: ${tab}`);
      return;
    }
    form.markAllAsTouched();
    if (form.invalid) {
      this.notify.warning('admin.validation.invalidForm');
      return;
    }
    this.onTabSave(tab, form);
  }

  getLanguageForm(lang: string): FormGroup | undefined {
    return this.i18nForms[lang];
  }

  protected isValid(): boolean {
    if (this.generalForm.invalid) {
      return false;
    }
    for (const form of Object.values(this.i18nForms)) {
      const hasContent = this.formHasContent(form);
      if (hasContent && form.invalid) {
        return false;
      }
    }

    return true;
  }

  protected formHasContent(form: FormGroup): boolean {
    const values = form.value;

    return Object.values(values).some(value => {
      if (value === null || value === undefined) {
        return false;
      }

      if (typeof value === 'string') {
        return value.trim().length > 0;
      }

      if (typeof value === 'boolean' || typeof value === 'number') {
        return true;
      }

      if (Array.isArray(value)) {
        return value.length > 0;
      }

      return true;
    });
  }

  protected abstract buildGeneralForm(): FormGroup;

  protected abstract buildI18nForm(lang: string): FormGroup;

  protected onTabSave(tab: 'general' | string, form: FormGroup): void {
  }

  protected override buildPayload(): TCreate | TUpdate {
    const payload: any = {
      ...this.generalForm.value
    };

    for (const [lang, form] of Object.entries(this.i18nForms)) {
      const hasContent = this.formHasContent(form);
      if (hasContent) {
        payload[lang] = form.value;
      }
    }
    if (this.isEditMode && this.data.id) {
      payload.id = this.data.id;
    }
    return payload;
  }
}
