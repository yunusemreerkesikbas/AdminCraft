import { CommonModule, UpperCasePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { LanguageContextService } from '@core/services/language-context.service';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { SpaLocalizedFormDialog } from '@shared/components/spa-localized-form-dialog';
import { SpaTabContainerComponent, SpaTabContentDirective, TabDefinition } from '@shared/components/spa-tab-container';
import { SpaCheckboxComponent } from 'app/shared/components/custom-ui/spa-checkbox/spa-checkbox.component';
import { SpaInputComponent } from 'app/shared/components/custom-ui/spa-input/spa-input.component';
import { SpaSelectComponent } from 'app/shared/components/custom-ui/spa-select/spa-select.component';
import { SpaTextareaComponent } from 'app/shared/components/custom-ui/spa-textarea/spa-textarea.component';
import { SpaDialogContentComponent, SpaDialogFooterComponent, SpaDialogHeaderComponent } from 'app/shared/components/spa-dialog';
import { map, Observable, of, switchMap, take } from 'rxjs';
import { SpaMediaPickerComponent } from '../../../media/components/spa-media-picker/spa-media-picker.component';
import { SpaResponsiveMediaPickerComponent } from '../../../media/components/spa-responsive-media-picker/spa-responsive-media-picker.component';
import { MediaService } from '../../../media/media.service';
import { ComponentEntry, EntryFieldDefinition, EntryI18nDto } from '../../models/component-entry.types';
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
        MatIconModule,
        MatButtonModule,
        MatProgressSpinnerModule,
        TranslocoModule,
        UpperCasePipe,
        SpaInputComponent,
        SpaSelectComponent,
        SpaTextareaComponent,
        SpaCheckboxComponent,
        SpaDialogHeaderComponent,
        SpaDialogContentComponent,
        SpaDialogFooterComponent,
        SpaMediaPickerComponent,
        SpaResponsiveMediaPickerComponent,
        SpaTabContainerComponent,
        SpaTabContentDirective
    ]
})
export class ComponentEntryFormComponent extends SpaLocalizedFormDialog<boolean, ComponentEntryFormData> {
    override data = inject<ComponentEntryFormData>(MAT_DIALOG_DATA);

    #entryService = inject(ComponentEntryService);
    #fieldService = inject(EntryFieldService);
    #mediaService = inject(MediaService);
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

    get tabs(): TabDefinition[] {
        return [
            { id: 'general', label: 'admin.components.entries.tabs.general', icon: 'settings' },
            { id: 'media', label: 'admin.media.title', icon: 'image' },
            ...this.languages.map(lang => ({
                id: 'lang-' + lang,
                label: lang.toUpperCase(),
                icon: 'translate'
            }))
        ];
    }

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
            status: [this.data.entry?.status ?? 'DRAFT', Validators.required],
            responsiveMedia: [this.#buildResponsiveMediaValue()]
        });
    }

    #buildResponsiveMediaValue(): { desktop: any; mobile: any } | null {
        const responsive = this.data.entry?.responsiveMedia;
        if (!responsive) return null;
        return {
            desktop: responsive.desktopMedia || null,
            mobile: responsive.mobileMedia || null
        };
    }

    #resolveResponsiveMediaId(): Observable<number | undefined> {
        const mediaValue = this.generalForm.value.responsiveMedia;
        const currentSetId = this.data.entry?.responsiveMedia?.id;

        const desktopMediaId = typeof mediaValue?.desktop === 'number'
            ? mediaValue.desktop
            : mediaValue?.desktop?.id;
        const mobileMediaId = typeof mediaValue?.mobile === 'number'
            ? mediaValue.mobile
            : mediaValue?.mobile?.id;

        if (!desktopMediaId && !mobileMediaId) {
            return of(undefined);
        }

        const request = {
            desktopMediaId,
            mobileMediaId,
            code: `responsive_entry_${this.data.componentId}_${Date.now()}`
        };

        if (currentSetId) {
            return this.#mediaService.updateResponsiveMedia(currentSetId, request).pipe(
                map(() => currentSetId)
            );
        } else {
            return this.#mediaService.createResponsiveMedia(request).pipe(
                map(set => set.id)
            );
        }
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

        const initialValue = this.data.translations?.[language]?.customFields?.[field.fieldKey] ?? this.#getDefaultValue(field.fieldType);

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
            case 'media': return null;
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

        const translations = this.#buildCompositeTranslations();

        this.#resolveResponsiveMediaId().pipe(
            switchMap(responsiveMediaId => {
                if (this.data.mode === 'create') {
                    const payload: any = {
                        componentId: this.data.componentId,
                        sortOrder: this.data.sortOrder ?? 0,
                        isVisible: this.generalForm.value.isVisible,
                        styleClasses: this.generalForm.value.styleClasses || undefined,
                        status: this.generalForm.value.status,
                        responsiveMediaId,
                        translations
                    };
                    return this.#entryService.createComposite(payload);
                } else {
                    const payload: any = {
                        sortOrder: this.data.sortOrder,
                        isVisible: this.generalForm.value.isVisible,
                        styleClasses: this.generalForm.value.styleClasses || undefined,
                        status: this.generalForm.value.status,
                        responsiveMediaId,
                        translations
                    };
                    return this.#entryService.updateComposite(this.data.entry!.id, payload);
                }
            }),
            take(1)
        ).subscribe({
            next: () => {
                this.setSubmitting(false);
                const msgKey = this.data.mode === 'create'
                    ? 'admin.components.entries.createSuccess'
                    : 'admin.components.entries.updateSuccess';
                this.notify.success(msgKey);
                this.close(true);
            },
            error: (err) => {
                this.setSubmitting(false);
                const msgKey = this.data.mode === 'create'
                    ? 'admin.components.entries.createFailed'
                    : 'admin.components.entries.updateFailed';
                this.notify.alert(err?.error?.message || msgKey);
            }
        });
    }

    #buildCompositeTranslations(): Record<string, any> {
        const translations: Record<string, any> = {};
        const baseFields = ['title', 'description'];

        this.languages.forEach(lang => {
            const formData = this.i18nForms[lang].value;
            const dynamicFields: Record<string, any> = {};

            Object.keys(formData).forEach(key => {
                if (!baseFields.includes(key) && formData[key] !== null && formData[key] !== '') {
                    dynamicFields[key] = formData[key];
                }
            });

            translations[lang] = {
                title: formData.title || undefined,
                description: formData.description || undefined,
                status: this.generalForm.value.status || ComponentStatus.DRAFT,
                dynamicFields: Object.keys(dynamicFields).length > 0 ? dynamicFields : undefined
            };
        });

        return translations;
    }
}
