import { Directive, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { SpaLocalizedFormDialogData } from '../spa-dialog-base/spa-dialog-base.types';
import { SpaFormDialog } from '../spa-form-dialog';

@Directive()
export abstract class SpaLocalizedFormDialog<
    TResult = any,
    TData extends SpaLocalizedFormDialogData = SpaLocalizedFormDialogData
> extends SpaFormDialog<TResult, TData> implements OnInit {

    generalForm!: FormGroup;
    i18nForms: Record<string, FormGroup> = {};

    protected override get form(): FormGroup {
        return this.generalForm;
    }

    protected override set form(value: FormGroup) {
        this.generalForm = value;
    }

    languages: string[] = ['TR', 'EN'];

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

    protected isAllFormsValid(): boolean {
        if (this.generalForm.invalid) return false;
        for (const form of Object.values(this.i18nForms)) {
            if (this.formHasContent(form) && form.invalid) return false;
        }
        return true;
    }

    protected formHasContent(form: FormGroup): boolean {
        return Object.values(form.value).some(value => {
            if (value === null || value === undefined) return false;
            if (typeof value === 'string') return value.trim().length > 0;
            return true;
        });
    }

    protected buildI18nPayload(): Record<string, any> {
        const payload: Record<string, any> = {};
        for (const [lang, form] of Object.entries(this.i18nForms)) {
            if (this.formHasContent(form)) {
                payload[lang] = form.value;
            }
        }
        return payload;
    }

    getLanguageForm(lang: string): FormGroup | undefined {
        return this.i18nForms[lang];
    }

    protected abstract buildGeneralForm(): FormGroup;

    protected abstract buildI18nForm(lang: string): FormGroup;
}
