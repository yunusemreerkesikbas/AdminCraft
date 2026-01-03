import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TenantContextService } from '@core/tenant/tenant-context.service';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { LanguageResponse } from '@modules/admin/custom/tenants/tenants.types';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaTextareaComponent } from '@shared/components/custom-ui/spa-textarea/spa-textarea.component';
import { SpaToggleComponent } from '@shared/components/custom-ui/spa-toggle/spa-toggle.component';
import { SpaDialogContentComponent } from '@shared/components/spa-dialog/spa-dialog-content/spa-dialog-content.component';
import { SpaDialogFooterComponent } from '@shared/components/spa-dialog/spa-dialog-footer/spa-dialog-footer.component';
import { SpaDialogHeaderComponent } from '@shared/components/spa-dialog/spa-dialog-header/spa-dialog-header.component';
import { SpaLocalizedFormDialog } from '@shared/components/spa-localized-form-dialog/spa-localized-form-dialog.directive';
import { take } from 'rxjs';
import { MediaService } from '../../media.service';
import { MediaDetailDialogData, MediaDetailResponse, MediaStatus } from '../../media.types';

@Component({
    selector: 'app-media-detail-dialog',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatButtonModule,
        MatIconModule,
        MatTabsModule,
        MatProgressSpinnerModule,
        TranslocoModule,
        SpaDialogHeaderComponent,
        SpaDialogContentComponent,
        SpaDialogFooterComponent,
        SpaInputComponent,
        SpaTextareaComponent,
        SpaToggleComponent
    ],
    templateUrl: './media-detail-dialog.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class MediaDetailDialogComponent extends SpaLocalizedFormDialog<boolean, MediaDetailDialogData> {

    protected readonly mediaService = inject(MediaService);
    readonly #tenantContext = inject(TenantContextService);
    protected readonly _transloco = inject(TranslocoService);

    readonly supportedLanguages = signal<LanguageResponse[]>([]);
    readonly #mediaDetails = signal<MediaDetailResponse | null>(null);
    readonly #isLoadingDetails = signal(true);

    /** Media data - prefer loaded details, fallback to dialog data */
    readonly media = computed(() => this.#mediaDetails() ?? this.data?.media);

    /** Container variants from API response */
    readonly containerVariants = computed(() => this.#mediaDetails()?.container?.variants ?? []);

    /** Loading state for format tab */
    readonly isLoadingDetails = computed(() => this.#isLoadingDetails());

    /** Check if media is still processing - only from loaded details */
    readonly isProcessing = computed(() => {
        const details = this.#mediaDetails();
        // Only check processing status if we have loaded details
        if (!details) return false;
        return details.status === MediaStatus.PROCESSING;
    });

    /** Check if has any variants */
    readonly hasVariants = computed(() => this.containerVariants().length > 0);

    override ngOnInit(): void {
        const tenant = this.#tenantContext.tenant();
        if (tenant?.supportedLanguages) {
            this.supportedLanguages.set(tenant.supportedLanguages);
            this.languages = tenant.supportedLanguages.map(l => l.code);
        } else {
            this.languages = ['EN'];
        }
        super.ngOnInit();
    }

    protected buildGeneralForm(): FormGroup {
        return this.fb.group({
            originalName: [this.data?.media?.originalName || '', Validators.required],
            isPublic: [this.data?.media?.isPublic ?? true]
        });
    }

    protected buildI18nForm(lang: string): FormGroup {
        return this.fb.group({
            altText: ['', Validators.maxLength(500)],
            title: ['', Validators.maxLength(255)],
            description: ['']
        });
    }

    protected override initializeForm(): void {
        super.initializeForm();
        this.#loadMediaDetails();
    }

    #loadMediaDetails(): void {
        const mediaId = this.data?.media?.id;
        if (!mediaId) {
            this.#isLoadingDetails.set(false);
            return;
        }

        this.#isLoadingDetails.set(true);
        this.mediaService.getDetails(mediaId).pipe(take(1)).subscribe({
            next: (details) => {
                // Store full details for container/variants access
                this.#mediaDetails.set(details);
                this.#isLoadingDetails.set(false);

                // Patch General Form
                this.generalForm.patchValue({
                    originalName: details.originalName,
                    isPublic: details.isPublic
                });

                // Patch I18n Forms
                if (details.translations) {
                    Object.entries(details.translations).forEach(([lang, i18n]) => {
                        if (this.i18nForms[lang] && i18n) {
                            this.i18nForms[lang].patchValue({
                                altText: i18n.altText || '',
                                title: i18n.title || '',
                                description: i18n.description || ''
                            });
                        }
                    });
                }
            },
            error: (err) => {
                this.#isLoadingDetails.set(false);
                this.onError(err);
            }
        });
    }

    save(): void {
        if (!this.isAllFormsValid()) return;

        const mediaId = this.data?.media?.id;
        if (!mediaId) return;

        const generalData = this.generalForm.value;
        const translations: Record<string, any> = {};

        this.languages.forEach(lang => {
            if (this.formHasContent(this.i18nForms[lang])) {
                translations[lang] = this.i18nForms[lang].value;
            }
        });

        this.mediaService.updateComposite(mediaId, {
            originalName: generalData.originalName,
            isPublic: generalData.isPublic,
            translations: translations
        }).pipe(take(1)).subscribe({
            next: () => {
                this._transloco.selectTranslate('admin.media.messages.updateSuccess').pipe(take(1)).subscribe(msg => {
                   // success message handled by caller usually or here?
                   // The caller (list component) shows success message usually.
                   // But let's check recent changes. 
                   // List component shows success message.
                });
                this.close(true);
            },
            error: (err) => this.onError(err)
        });
    }
}
