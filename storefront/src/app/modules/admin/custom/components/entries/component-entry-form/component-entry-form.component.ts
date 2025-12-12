import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTabsModule } from '@angular/material/tabs';
import { LanguageContextService } from '@core/services/language-context.service';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { SpaLocalizedFormDialog } from '@shared/components/spa-localized-form-dialog';
import { SpaCheckboxComponent } from 'app/shared/components/custom-ui/spa-checkbox/spa-checkbox.component';
import { SpaInputComponent } from 'app/shared/components/custom-ui/spa-input/spa-input.component';
import { SpaSelectComponent } from 'app/shared/components/custom-ui/spa-select/spa-select.component';
import { SpaTextareaComponent } from 'app/shared/components/custom-ui/spa-textarea/spa-textarea.component';
import { SpaDialogComponent } from 'app/shared/components/spa-dialog';
import { forkJoin, take } from 'rxjs';
import { ComponentEntry, CreateEntryRequest, EntryFieldDefinition, EntryI18nDto, EntryI18nRequest, UpdateEntryRequest } from '../../models/component-entry.types';
import { ComponentStatus } from '../../models/component-library.types';
import { ComponentEntryService } from '../../services/component-entry.service';
import { EntryFieldService } from '../../services/entry-field.service';

interface ComponentEntryFormData {
    mode: 'create' | 'edit';
    componentId: number;
    componentTypeId?: number;
    languages?: string[];
    entryId?: number;
    entry?: ComponentEntry;
    translations?: Record<string, EntryI18nDto>;
    sortOrder?: number;
}

@Component({
    selector: 'spa-component-entry-form',
    templateUrl: './component-entry-form.component.html',
    styleUrls: ['./component-entry-form.component.scss'],
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatTabsModule,
        MatIconModule,
        MatButtonModule,
        MatProgressSpinnerModule,
        TranslocoModule,
        SpaInputComponent,
        SpaSelectComponent,
        SpaTextareaComponent,
        SpaCheckboxComponent,
        SpaDialogComponent
    ]
})
export class ComponentEntryFormComponent extends SpaLocalizedFormDialog<boolean, ComponentEntryFormData> {
    override data = inject<ComponentEntryFormData>(MAT_DIALOG_DATA);
    
    #entryService = inject(ComponentEntryService);
    #fieldService = inject(EntryFieldService);
    #transloco = inject(TranslocoService);
    #langCtx = inject(LanguageContextService);

    override languages = this.#langCtx.supportedLanguages();
    
    fieldDefinitions = signal<EntryFieldDefinition[]>([]);
    isLoading = signal<boolean>(false);
    
    canSave = computed(() => 
        this.generalForm?.valid && 
        !this.isSubmitting() && 
        !this.isLoading()
    );

    statusOptions = Object.values(ComponentStatus).map(s => ({ value: s, label: s }));

    override ngOnInit(): void {
        super.ngOnInit();
        
        if (this.data.componentTypeId) {
            setTimeout(() => {
                this.#loadFieldDefinitions();
            });
        }
    }

    protected buildGeneralForm(): FormGroup {
        return this.fb.group({
            isVisible: [this.data.entry?.isVisible ?? true],
            styleClasses: [this.data.entry?.styleClasses || ''],
            status: [this.data.entry?.status ?? 'DRAFT', Validators.required]
        });
    }

    protected buildI18nForm(lang: string): FormGroup {
        return this.fb.group({
            title: [this.data.translations?.[lang]?.title || ''],
            description: [this.data.translations?.[lang]?.description || '']
        });
    }

    #loadFieldDefinitions(): void {
        this.isLoading.set(true);
        
        this.#fieldService.getFields(this.data.componentTypeId!)
            .pipe(take(1))
            .subscribe({
                next: (fields) => {
                    this.fieldDefinitions.set(fields);
                    this.#addDynamicFieldsToForms();
                    this.isLoading.set(false);
                },
                error: () => {
                    this.notify.alert('admin.components.entries.loadFieldsFailed');
                    this.isLoading.set(false);
                }
            });
    }

    #addDynamicFieldsToForms(): void {
        this.languages.forEach(lang => {
            const formGroup = this.i18nForms[lang];
            if (!formGroup) return;

            this.fieldDefinitions().forEach(field => {
                const control = this.#createControlForFieldType(field, lang);
                (formGroup as any).addControl(field.fieldKey, control);
            });
        });
    }

    #createControlForFieldType(field: EntryFieldDefinition, language: string): FormControl {
        const validators = [];
        if (field.isRequired) validators.push(Validators.required);

        const initialValue = this.data.translations?.[language]?.[field.fieldKey] ?? this.#getDefaultValue(field.fieldType);

        switch (field.fieldType) {
            case 'text':
                if (field.maxLength) validators.push(Validators.maxLength(field.maxLength));
                break;
            case 'number':
                if (field.minValue !== undefined) validators.push(Validators.min(field.minValue));
                if (field.maxValue !== undefined) validators.push(Validators.max(field.maxValue));
                break;
        }

        return new FormControl(initialValue, validators);
    }

    #getDefaultValue(fieldType: string): any {
        switch (fieldType) {
            case 'boolean': return false;
            case 'number': return null;
            default: return '';
        }
    }

    protected getFieldLabelWithFallback(fieldKey: string): string {
        const i18nKey = `admin.components.entryFields.custom.${fieldKey}`;
        const translation = this.#transloco.translate(i18nKey);
        
        if (translation !== i18nKey) {
            return translation;
        }
        
        return this.#humanizeFieldKey(fieldKey);
    }

    #humanizeFieldKey(fieldKey: string): string {
        return fieldKey
            .replace(/([A-Z])/g, ' $1')
            .replace(/^./, str => str.toUpperCase())
            .trim();
    }

    save(): void {
        if (this.generalForm.invalid) {
            this.notify.warning('admin.validation.generalFormInvalid');
            return;
        }

        const hasInvalidI18n = Object.values(this.i18nForms).some(form => form.invalid);
        if (hasInvalidI18n) {
            this.notify.warning('admin.validation.i18nFormsInvalid');
            return;
        }

        this.setSubmitting(true);

        if (this.data.mode === 'create') {
            this.#createEntry();
        } else {
            this.#updateEntry();
        }
    }

    #createEntry(): void {
        const payload: CreateEntryRequest = {
            componentId: this.data.componentId,
            sortOrder: this.data.sortOrder ?? 0,
            isVisible: this.generalForm.value.isVisible,
            styleClasses: this.generalForm.value.styleClasses || undefined
        };

        this.#entryService.create(payload)
            .pipe(take(1))
            .subscribe({
                next: (entry) => {
                    this.#saveI18nData(entry.id);
                },
                error: () => {
                    this.setSubmitting(false);
                    this.notify.alert('admin.components.entries.createFailed');
                }
            });
    }

    #updateEntry(): void {
        const payload: UpdateEntryRequest = {
            isVisible: this.generalForm.value.isVisible,
            styleClasses: this.generalForm.value.styleClasses || undefined,
            status: this.generalForm.value.status
        };

        this.#entryService.update(this.data.entry!.id, payload)
            .pipe(take(1))
            .subscribe({
                next: () => {
                    this.#saveI18nData(this.data.entry!.id);
                },
                error: () => {
                    this.setSubmitting(false);
                    this.notify.alert('admin.components.entries.updateFailed');
                }
            });
    }

    #saveI18nData(entryId: number): void {
        const i18nRequests = this.languages.map(lang => {
            const formData = this.i18nForms[lang].value;
            const baseFields = ['title', 'description'];

            const dynamicFields: Record<string, any> = {};
            Object.keys(formData).forEach(key => {
                if (!baseFields.includes(key)) {
                    dynamicFields[key] = formData[key];
                }
            });

            const request: EntryI18nRequest = {
                title: formData.title || undefined,
                description: formData.description || undefined,
                ...dynamicFields
            };

            return this.#entryService.upsertI18n(entryId, lang, request);
        });

        forkJoin(i18nRequests)
            .pipe(take(1))
            .subscribe({
                next: () => {
                    this.setSubmitting(false);
                    this.notify.success(
                        this.data.mode === 'create' 
                            ? 'admin.components.entries.createSuccess' 
                            : 'admin.components.entries.updateSuccess'
                    );
                    this.close(true);
                },
                error: () => {
                    this.setSubmitting(false);
                    this.notify.alert('admin.components.entries.saveI18nFailed');
                }
            });
    }
}
