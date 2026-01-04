import { SpaLocalizedFormDialogData } from '@/app/shared/components/spa-dialog-base';
import { CommonModule, UpperCasePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, ViewEncapsulation } from '@angular/core';
import { FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { TenantContextService } from '@core/tenant/tenant-context.service';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { SpaCheckboxComponent } from '@shared/components/custom-ui/spa-checkbox/spa-checkbox.component';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaSelectComponent } from '@shared/components/custom-ui/spa-select/spa-select.component';
import { SpaTextareaComponent } from '@shared/components/custom-ui/spa-textarea/spa-textarea.component';
import { SpaDialogContentComponent, SpaDialogFooterComponent, SpaDialogHeaderComponent } from '@shared/components/spa-dialog';
import { SpaLocalizedFormDialog } from '@shared/components/spa-localized-form-dialog';
import { NotificationService } from '@shared/notifications/notification.service';
import { map, Observable, of, switchMap, take } from 'rxjs';
import { SpaResponsiveMediaPickerComponent } from '../../media/components/spa-responsive-media-picker/spa-responsive-media-picker.component';
import { MediaService } from '../../media/media.service';
import { ComponentEntryListComponent } from '../entries/component-entry-list/component-entry-list.component';
import { ComponentDetailDto, ComponentStatus, ComponentTypeDto, CreateComponentCompositeRequest, UpdateComponentCompositeRequest } from '../models/component-library.types';
import { ComponentLibraryService } from '../services/component-library.service';

export interface ComponentEditDialogData extends SpaLocalizedFormDialogData<ComponentDetailDto> {
    mode: 'create' | 'edit';
    component?: ComponentDetailDto;
    componentTypes?: ComponentTypeDto[];
    languages: string[];
}

@Component({
    selector: 'spa-component-edit-dialog',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatButtonModule,
        MatIconModule,
        MatTabsModule,
        TranslocoModule,
        UpperCasePipe,
        SpaInputComponent,
        SpaSelectComponent,
        SpaCheckboxComponent,
        SpaTextareaComponent,
        ComponentEntryListComponent,
        SpaDialogHeaderComponent,
        SpaDialogContentComponent,
        SpaDialogFooterComponent,
        SpaResponsiveMediaPickerComponent
    ],
    templateUrl: './component-edit-dialog.component.html',
    styleUrls: ['./component-edit-dialog.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ComponentEditDialogComponent extends SpaLocalizedFormDialog<any, ComponentEditDialogData> {
    readonly #translocoService = inject(TranslocoService);
    readonly #componentService = inject(ComponentLibraryService);
    readonly #mediaService = inject(MediaService);
    readonly #notify = inject(NotificationService);
    readonly #tenantContext = inject(TenantContextService);

    statusOptions = Object.values(ComponentStatus).map(s => ({ value: s, label: s }));
    componentTypeOptions: { value: any; label: string }[] = [];
    override languages: string[] = [];

    readonly dialogTitle = this.data?.component
        ? this.#translocoService.translate('admin.dialog.title.edit')
        : this.#translocoService.translate('admin.dialog.title.create');

    constructor() {
        super();
        this.componentTypeOptions = (this.data?.componentTypes || []).map(type => ({
            value: type.id,
            label: type.name
        }));
    }

    override ngOnInit(): void {
        const tenant = this.#tenantContext.tenant();
        if (this.data?.languages?.length) {
             this.languages = this.data.languages;
        } else if (tenant?.supportedLanguages) {
             this.languages = tenant.supportedLanguages.map(l => l.code);
        } else {
             this.languages = ['en'];
        }
        super.ngOnInit();
    }

    protected buildGeneralForm(): FormGroup {
        const component = this.data?.component;
        const responsiveMedia = component?.responsiveMedia;
        const responsiveValue = responsiveMedia ? {
            desktop: responsiveMedia.desktopMedia,
            mobile: responsiveMedia.mobileMedia
        } : null;

        return this.fb.group({
            name: [component?.name || '', Validators.required],
            componentTypeId: [component?.componentTypeId || '', Validators.required],
            status: [component?.status, Validators.required],
            isVisible: [component?.isVisible ?? true],
            styleClasses: [component?.styleClasses || ''],
            displayOrder: [component?.displayOrder || 0],
            responsiveMedia: [responsiveValue]
        });
    }

    protected buildI18nForm(lang: string): FormGroup {
        const translation = this.data?.component?.translations?.[lang];
        return this.fb.group({
            title: [translation?.title || ''],
            subtitle: [translation?.subtitle || ''],
            description: [translation?.description || '']
        });
    }

    save(): void {
        if (this.generalForm.invalid) return;

        const translations = this.#buildTranslations();
        
        if (Object.keys(translations).length === 0) {
            // Logic to warn or validate could go here
        }

        this.setSubmitting(true);

        if (this.data.mode === 'create') {
            this.#createComponentComposite(translations);
        } else {
            this.#updateComponentComposite(translations);
        }
    }

    #createComponentComposite(translations: Record<string, any>): void {
        const generalData = this.generalForm.value;

        const request: CreateComponentCompositeRequest = {
            componentTypeId: generalData.componentTypeId,
            name: generalData.name,
            displayOrder: generalData.displayOrder,
            isVisible: generalData.isVisible,
            styleClasses: generalData.styleClasses,
            status: generalData.status,
            translations: translations
        };

        this.#componentService.createComposite(request)
            .pipe(
                switchMap(response => this.#handleResponsiveMedia(response.id).pipe(map(() => response))),
                take(1)
            )
            .subscribe({
                next: (response) => {
                    this.#notify.success('admin.components.success.created');
                    this.close(true);
                },
                error: (err) => {
                    this.setSubmitting(false);
                    this.#notify.alert('admin.components.errors.createFailed');
                    console.error(err);
                }
            });
    }

    #updateComponentComposite(translations: Record<string, any>): void {
        if (!this.data.component?.id) return;
        const generalData = this.generalForm.value;

        const request: UpdateComponentCompositeRequest = {
            name: generalData.name,
            displayOrder: generalData.displayOrder,
            isVisible: generalData.isVisible,
            styleClasses: generalData.styleClasses,
            status: generalData.status,
            translations: translations
        };

        this.#componentService.updateComposite(this.data.component.id, request)
            .pipe(
                switchMap(response => this.#handleResponsiveMedia(response.id).pipe(map(() => response))),
                take(1)
            )
            .subscribe({
                next: (response) => {
                    this.#notify.success('admin.components.success.updated');
                    this.close(true);
                },
                error: (err) => {
                    this.setSubmitting(false);
                    this.#notify.alert('admin.components.errors.updateFailed');
                    console.error(err);
                }
            });
    }

    #buildTranslations(): Record<string, any> {
        const translations: Record<string, any> = {};

        this.languages.forEach(lang => {
            const form = this.i18nForms[lang];
            if (!form) return;

            const title = form.value.title as string;
            const subtitle = form.value.subtitle as string;
            const description = form.value.description as string;
            const status = form.value.status || ComponentStatus.DRAFT;

            if (title && title.trim().length > 0) {
                translations[lang.toUpperCase()] = {
                    title: title.trim(),
                    subtitle: subtitle?.trim() || undefined,
                    description: description?.trim() || undefined,
                    status: status
                };
            }
        });

        return translations;
    }

    #handleResponsiveMedia(componentId: number): Observable<any> {
        const responsiveValue = this.generalForm.get('responsiveMedia')?.value;
        const currentSetId = this.data.component?.responsiveMedia?.id;

        const desktopMediaId = responsiveValue?.desktop?.id;
        const mobileMediaId = responsiveValue?.mobile?.id;

        if (!desktopMediaId && !mobileMediaId) {
            if (currentSetId) {
                return this.#componentService.assignResponsiveMedia(componentId, null);
            }
            return of(null);
        }

        const responsiveMediaRequest = {
            desktopMediaId: desktopMediaId,
            mobileMediaId: mobileMediaId,
            code: `responsive_${componentId}_${Date.now()}`
        };

        if (currentSetId) {
            return this.#mediaService.updateResponsiveMedia(currentSetId, responsiveMediaRequest).pipe(
                map(() => null)
            );
        } else {
            return this.#mediaService.createResponsiveMedia(responsiveMediaRequest).pipe(
                switchMap(set => this.#componentService.assignResponsiveMedia(componentId, set.id))
            );
        }
    }
}
