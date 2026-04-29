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
    MediaSummaryDto,
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

    protected readonly tabsSig = computed<TabDefinition[]>(() => [
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
    ]);

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

    #buildResponsiveMediaValue(): { desktopMedia: any; mobileMedia: any } | null {
        const responsive = this.data.entry?.responsiveMedia;
        if (responsive) {
            return {
                desktopMedia: responsive.desktopMedia || null,
                mobileMedia: responsive.mobileMedia || null,
            };
        }

        const hydratedMedia = Object.values(this.data.translations ?? {})
            .map((translation) => translation?.customFields?.media)
            .find(
                (media): media is MediaSummaryDto =>
                    typeof media?.id === 'number'
            );

        if (!hydratedMedia) {
            return null;
        }

        return {
            desktopMedia: hydratedMedia,
            mobileMedia: null,
        };
    }

    #resolveResponsiveMediaId(): Observable<number | null | undefined> {
        const mediaValue = this.generalForm.value.responsiveMedia;
        const currentSetId = this.data.entry?.responsiveMedia?.id;

        const desktopMediaId = this.#extractMediaId(mediaValue?.desktopMedia);
        const mobileMediaId = this.#extractMediaId(mediaValue?.mobileMedia);

        if (!desktopMediaId && !mobileMediaId) {
            return of(this.data.mode === 'edit' ? null : undefined);
        }

        const request = {
            desktopMediaId,
            mobileMediaId,
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

    #extractMediaId(media: unknown): number | undefined {
        if (media == null) return undefined;
        if (typeof media === 'number' && !Number.isNaN(media)) return media;
        if (typeof media === 'object' && 'id' in media) {
            const id = (media as { id: unknown }).id;
            return typeof id === 'number' && !Number.isNaN(id) ? id : undefined;
        }
        return undefined;
    }

    protected buildI18nForm(lang: string): FormGroup {
        const translation = this.#getExistingTranslation(lang);
        return this.fb.group({
            title: [translation?.title || ''],
            description: [translation?.description || ''],
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
                error: (error) => {
                    this.notify.alert(error?.error?.message ?? '');
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
            type: f.fieldType as DynamicFieldConfig['type'],
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
                        return this.#entryService.createCompositeWithResponse(
                            payload
                        );
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
                        return this.#entryService.updateCompositeWithResponse(
                            this.data.entry!.id,
                            payload
                        );
                    }
                }),
                take(1)
            )
            .subscribe({
                next: (response) => {
                    this.setSubmitting(false);
                    this.notify.success(response.message ?? '');
                    this.close(true);
                },
                error: (err) => {
                    this.setSubmitting(false);
                    this.notify.alert(err?.error?.message ?? '');
                },
            });
    }

    #buildCompositeTranslations(): Record<string, any> {
        const translations: Record<string, any> = {};
        const baseFields = ['title', 'description'];
        const isEditMode = this.data.mode === 'edit';
        let hasAnyTranslationContent = false;

        this.languages.forEach((lang) => {
            const formData = this.i18nForms[lang].value;
            const existingTranslation = this.#getExistingTranslation(lang);
            const existingDynamicFields =
                this.#extractPersistedDynamicFields(existingTranslation);
            const titleValue = this.#normalizeTranslationField(
                formData.title as string
            );
            const descriptionValue = this.#normalizeTranslationField(
                formData.description as string
            );
            const dynamicFields = this.#buildDynamicFieldsPayload(
                formData,
                baseFields
            );
            const hasCurrentTranslationContent =
                titleValue.length > 0 ||
                descriptionValue.length > 0 ||
                Object.keys(dynamicFields).length > 0;

            if (hasCurrentTranslationContent) {
                hasAnyTranslationContent = true;
            }

            const shouldIncludeLocale = isEditMode
                ? !!existingTranslation || hasCurrentTranslationContent
                : hasCurrentTranslationContent;

            if (!shouldIncludeLocale) {
                return;
            }

            translations[lang.toUpperCase()] = {
                title: titleValue,
                description: descriptionValue,
                dynamicFields: this.#resolveDynamicFieldsValue(
                    dynamicFields,
                    existingDynamicFields,
                    isEditMode
                ),
            };
        });

        return hasAnyTranslationContent ? translations : {};
    }

    #hasSelectedMedia(): boolean {
        const mediaValue = this.generalForm.value.responsiveMedia;
        return (
            !!this.#extractMediaId(mediaValue?.desktopMedia) ||
            !!this.#extractMediaId(mediaValue?.mobileMedia)
        );
    }

    #normalizeTranslationField(value: string | null | undefined): string {
        return value?.trim() ?? '';
    }

    #getExistingTranslation(lang: string): EntryI18nDto | undefined {
        return (
            this.data.translations?.[lang] ??
            this.data.translations?.[lang.toUpperCase()]
        );
    }

    #extractPersistedDynamicFields(
        translation: EntryI18nDto | undefined
    ): Record<string, any> {
        const customFields = translation?.customFields;
        if (!customFields) {
            return {};
        }

        return Object.fromEntries(
            Object.entries(customFields).filter(([key]) => key !== 'media')
        );
    }

    #buildDynamicFieldsPayload(
        formData: Record<string, any>,
        baseFields: string[]
    ): Record<string, any> {
        const dynamicFields: Record<string, any> = {};
        const shouldClearLegacyMediaUid = !this.#hasSelectedMedia();

        Object.keys(formData).forEach((key) => {
            const value = formData[key];
            const shouldSkipMediaUid = shouldClearLegacyMediaUid && key === 'mediaUid';

            if (
                baseFields.includes(key) ||
                shouldSkipMediaUid ||
                value === null ||
                value === ''
            ) {
                return;
            }

            dynamicFields[key] = value;
        });

        return dynamicFields;
    }

    #resolveDynamicFieldsValue(
        dynamicFields: Record<string, any>,
        existingDynamicFields: Record<string, any>,
        isEditMode: boolean
    ): Record<string, any> | undefined {
        if (Object.keys(dynamicFields).length > 0) {
            return dynamicFields;
        }

        if (isEditMode && Object.keys(existingDynamicFields).length > 0) {
            return {};
        }

        return undefined;
    }
}
