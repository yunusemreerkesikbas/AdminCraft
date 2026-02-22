import { UpperCasePipe } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    computed,
    inject,
    signal,
} from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { LanguageContextService } from '@core/services/language-context.service';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { SpaLocalizedFormDialog } from '@shared/components/spa-localized-form-dialog';
import {
    SpaTabContainerComponent,
    SpaTabContentDirective,
    TabDefinition,
} from '@shared/components/spa-tab-container';
import { SpaCheckboxComponent } from 'app/shared/components/custom-ui/spa-checkbox/spa-checkbox.component';
import { SpaDynamicFormService } from 'app/shared/components/custom-ui/spa-dynamic-form/spa-dynamic-form.service';
import { DynamicFieldConfig } from 'app/shared/components/custom-ui/spa-dynamic-form/spa-dynamic-form.types';
import { SpaInputComponent } from 'app/shared/components/custom-ui/spa-input/spa-input.component';
import { SpaTextareaComponent } from 'app/shared/components/custom-ui/spa-textarea/spa-textarea.component';
import { SpaDialogComponent } from 'app/shared/components/spa-dialog';
import { map, Observable, of, switchMap, take } from 'rxjs';
import { SpaMediaPickerComponent } from '../../../media/components/spa-media-picker/spa-media-picker.component';
import { MediaService } from '../../../media/media.service';
import {
    ComponentEntry,
    CreateComponentEntryCompositeRequest,
    EntryFieldDefinition,
    EntryI18nDto,
    UpdateComponentEntryCompositeRequest,
} from '../../models/component-entry.types';
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
        ReactiveFormsModule,
        MatIconModule,
        MatButtonModule,
        MatProgressSpinnerModule,
        TranslocoModule,
        UpperCasePipe,
        SpaInputComponent,
        SpaTextareaComponent,
        SpaCheckboxComponent,
        SpaDialogComponent,
        SpaMediaPickerComponent,
        SpaTabContainerComponent,
        SpaTabContentDirective,
    ],
})
export class ComponentEntryFormComponent extends SpaLocalizedFormDialog<
    boolean,
    ComponentEntryFormData
> {
    override data = inject<ComponentEntryFormData>(MAT_DIALOG_DATA);

    #entryService = inject(ComponentEntryService);
    #fieldService = inject(EntryFieldService);
    #mediaService = inject(MediaService);
    #languageContext = inject(LanguageContextService);
    #dynamicFormService = inject(SpaDynamicFormService);
    #transloco = inject(TranslocoService);

    override languages = this.#languageContext.supportedLanguages();

    protected fieldDefinitionsSig = signal<EntryFieldDefinition[]>([]);
    protected dynamicFieldsConfigSig = computed(() =>
        this.#mapToDynamicConfig(this.fieldDefinitionsSig())
    );
    protected isLoadingSig = signal<boolean>(false);

    protected canSaveSig = computed(
        () =>
            this.generalForm?.valid && !this.isSubmitting() && !this.isLoadingSig()
    );

    protected get tabs(): TabDefinition[] {
        return [
            {
                id: 'general',
                label: 'admin.components.entries.tabs.general',
                icon: 'settings',
            },
            { id: 'media', label: 'admin.media.title', icon: 'image' },
            ...this.languages.map((lang) => ({
                id: 'lang-' + lang,
                label: lang.toUpperCase(),
                icon: 'translate',
            })),
        ];
    }

    override ngOnInit(): void {
        super.ngOnInit();

        if (this.data.componentTypeId) {
            this.#loadFieldDefinitions();
        }
    }

    protected buildGeneralForm(): FormGroup {
        return this.fb.group({
            isVisible: [this.data.entry?.isVisible ?? true],
            styleClasses: [this.data.entry?.styleClasses || ''],
            responsiveMedia: [this.#buildResponsiveMediaValue()],
        });
    }

    #buildResponsiveMediaValue(): { desktop: any; mobile: any } | null {
        const responsive = this.data.entry?.responsiveMedia;
        if (!responsive) return null;
        return {
            desktop: responsive.desktopMedia || null,
            mobile: responsive.mobileMedia || null,
        };
    }

    #resolveResponsiveMediaId(): Observable<number | undefined> {
        const mediaValue = this.generalForm.value.responsiveMedia;
        const currentSetId = this.data.entry?.responsiveMedia?.id;

        const desktopMediaId =
            typeof mediaValue?.desktop === 'number'
                ? mediaValue.desktop
                : mediaValue?.desktop?.id;
        const mobileMediaId =
            typeof mediaValue?.mobile === 'number'
                ? mediaValue.mobile
                : mediaValue?.mobile?.id;

        if (!desktopMediaId && !mobileMediaId) {
            return of(undefined);
        }

        const responsiveMedia = this.data.entry?.responsiveMedia;
        const request = {
            desktopMediaId,
            mobileMediaId,
            code: responsiveMedia ? String(responsiveMedia.id) : undefined,
        };

        if (currentSetId) {
            return this.#mediaService
                .updateResponsiveMedia(currentSetId, request)
                .pipe(map(() => currentSetId));
        } else {
            return this.#mediaService
                .createResponsiveMedia(request)
                .pipe(map((set) => set.id));
        }
    }

    protected buildI18nForm(lang: string): FormGroup {
        return this.fb.group({
            title: [this.data.translations?.[lang]?.title || ''],
            description: [this.data.translations?.[lang]?.description || ''],
        });
    }

    #loadFieldDefinitions(): void {
        this.isLoadingSig.set(true);

        this.#fieldService
            .getFields(this.data.componentTypeId!)
            .pipe(take(1))
            .subscribe({
                next: (fields) => {
                    this.fieldDefinitionsSig.set(fields);
                    this.#addDynamicFieldsToForms();
                    this.isLoadingSig.set(false);
                },
                error: () => {
                    this.notify.alert(
                        'admin.components.entries.loadFieldsFailed'
                    );
                    this.isLoadingSig.set(false);
                },
            });
    }

    #addDynamicFieldsToForms(): void {
        const configs = this.dynamicFieldsConfigSig();

        this.languages.forEach((lang) => {
            const formGroup = this.i18nForms[lang];
            if (!formGroup) return;

            const initialValues =
                this.data.translations?.[lang]?.customFields || {};
            this.#dynamicFormService.addControlsToFormGroup(
                formGroup,
                configs,
                initialValues
            );
        });
    }

    #mapToDynamicConfig(fields: EntryFieldDefinition[]): DynamicFieldConfig[] {
        return fields.map((f) => ({
            key: f.fieldKey,
            type: f.fieldType as any,
            labelKey: `admin.components.entryFields.custom.${f.fieldKey}`,
        }));
    }

    protected getFieldLabelWithFallback(fieldKey: string): string {
        const key = `admin.components.entryFields.custom.${fieldKey}`;
        const translated = this.#transloco.translate(key);
        return translated === key ? fieldKey : translated;
    }

    save(): void {
        if (this.generalForm.invalid) {
            this.notify.warning('admin.common.validation.generalFormInvalid');
            return;
        }

        const hasInvalidI18n = Object.values(this.i18nForms).some(
            (form) => form.invalid
        );
        if (hasInvalidI18n) {
            this.notify.warning('admin.common.validation.i18nFormsInvalid');
            return;
        }

        this.setSubmitting(true);

        const translations = this.#buildCompositeTranslations();

        this.#resolveResponsiveMediaId()
            .pipe(
                switchMap((responsiveMediaId) => {
                    if (this.data.mode === 'create') {
                        const payload: CreateComponentEntryCompositeRequest = {
                            componentId: this.data.componentId,
                            sortOrder: this.data.sortOrder ?? 0,
                            isVisible: this.generalForm.value.isVisible,
                            styleClasses:
                                this.generalForm.value.styleClasses ||
                                undefined,
                            responsiveMediaId,
                            translations,
                        };
                        return this.#entryService.createComposite(payload);
                    } else {
                        const payload: UpdateComponentEntryCompositeRequest = {
                            sortOrder: this.data.sortOrder,
                            isVisible: this.generalForm.value.isVisible,
                            styleClasses:
                                this.generalForm.value.styleClasses ||
                                undefined,
                            responsiveMediaId,
                            translations,
                        };
                        return this.#entryService.updateComposite(
                            this.data.entry!.id,
                            payload
                        );
                    }
                }),
                take(1)
            )
            .subscribe({
                next: () => {
                    this.setSubmitting(false);
                    const msgKey =
                        this.data.mode === 'create'
                            ? 'admin.components.entries.createSuccess'
                            : 'admin.components.entries.updateSuccess';
                    this.notify.success(msgKey);
                    this.close(true);
                },
                error: (err) => {
                    this.setSubmitting(false);
                    const msgKey =
                        this.data.mode === 'create'
                            ? 'admin.components.entries.createFailed'
                            : 'admin.components.entries.updateFailed';
                    this.notify.alert(err?.error?.message || msgKey);
                },
            });
    }

    #buildCompositeTranslations(): Record<string, any> {
        const translations: Record<string, any> = {};
        const baseFields = ['title', 'description'];

        this.languages.forEach((lang) => {
            const formData = this.i18nForms[lang].value;
            const dynamicFields: Record<string, any> = {};

            Object.keys(formData).forEach((key) => {
                if (
                    !baseFields.includes(key) &&
                    formData[key] !== null &&
                    formData[key] !== ''
                ) {
                    dynamicFields[key] = formData[key];
                }
            });

            translations[lang] = {
                title: formData.title || undefined,
                description: formData.description || undefined,
                dynamicFields:
                    Object.keys(dynamicFields).length > 0
                        ? dynamicFields
                        : undefined,
            };
        });

        return translations;
    }
}
