import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTabsModule } from '@angular/material/tabs';
import { TenantContextService } from '@core/tenant/tenant-context.service';
import { FuseConfirmationService } from '@fuse/services/confirmation/confirmation.service';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { MediaService } from '@media/media.service';
import { MediaDetailDialogData, MediaDetailResponse, MediaStatus } from '@media/media.types';
import { LanguageResponse } from '@modules/admin/custom/tenants/tenants.types';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaTextareaComponent } from '@shared/components/custom-ui/spa-textarea/spa-textarea.component';
import { SpaToggleComponent } from '@shared/components/custom-ui/spa-toggle/spa-toggle.component';
import { SpaDialogContentComponent } from '@shared/components/spa-dialog/spa-dialog-content/spa-dialog-content.component';
import { SpaDialogFooterComponent } from '@shared/components/spa-dialog/spa-dialog-footer/spa-dialog-footer.component';
import { SpaDialogHeaderComponent } from '@shared/components/spa-dialog/spa-dialog-header/spa-dialog-header.component';
import { SpaLocalizedFormDialog } from '@shared/components/spa-localized-form-dialog/spa-localized-form-dialog.directive';
import { VALIDATION_LIMITS } from '@shared/constants/validation.constants';
import { NotificationService } from '@shared/notifications/notification.service';
import { forkJoin, take } from 'rxjs';
import { ComponentEditDialogComponent } from '../../../components/component-edit-dialog/component-edit-dialog.component';
import { ComponentDto } from '../../../components/models/component-library.types';
import { ComponentLibraryService } from '../../../components/services/component-library.service';
import { FocalPointPickerComponent } from '../../components/focal-point-picker/focal-point-picker.component';
import { FormatGeneratorComponent } from '../../components/format-generator/format-generator.component';

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
        SpaToggleComponent,
        FormatGeneratorComponent,
        FocalPointPickerComponent
    ],
    templateUrl: './media-detail-dialog.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class MediaDetailDialogComponent extends SpaLocalizedFormDialog<boolean, MediaDetailDialogData> {

    protected mediaService = inject(MediaService);
    #tenantContext = inject(TenantContextService);
    #transloco = inject(TranslocoService);
    #notificationService = inject(NotificationService);
    #confirmationService = inject(FuseConfirmationService);

    protected supportedLanguages = signal<LanguageResponse[]>([]);
    #mediaDetails = signal<MediaDetailResponse | null>(null);
    #isLoadingDetails = signal(true);
    protected media = computed(() => this.#mediaDetails() ?? this.data?.media);
    protected containerVariants = computed(() => this.#mediaDetails()?.container?.variants ?? []);
    protected isLoadingDetails = computed(() => this.#isLoadingDetails());
    protected isProcessing = computed(() => {
        const details = this.#mediaDetails();
        if (!details) return false;
        return details.status === MediaStatus.PROCESSING;
    });
    protected hasVariants = computed(() => this.containerVariants().length > 0);
    #componentService = inject(ComponentLibraryService);
    #matDialog = inject(MatDialog);
    protected linkedComponentsSig = signal<ComponentDto[]>([]);
    protected isLoadingLinkedComponentsSig = signal(false);

    override ngOnInit(): void {
        const tenant = this.#tenantContext.tenant();
        if (tenant?.supportedLanguages) {
            this.supportedLanguages.set(tenant.supportedLanguages);
            this.languages = tenant.supportedLanguages.map(l => l.code);
        } else {
            this.languages = ['en'];
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
            altText: ['', Validators.maxLength(VALIDATION_LIMITS.MEDIA_ALT_TEXT_MAX)],
            title: ['', Validators.maxLength(VALIDATION_LIMITS.MEDIA_TITLE_MAX)],
            description: ['', Validators.maxLength(VALIDATION_LIMITS.MEDIA_DESCRIPTION_MAX)]
        });
    }

    protected override initializeForm(): void {
        super.initializeForm();
        this.#loadMediaDetails();
        this.#loadLinkedComponents();
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
                this.#mediaDetails.set(details);
                this.#isLoadingDetails.set(false);
                this.generalForm.patchValue({
                    originalName: details.originalName,
                    isPublic: details.isPublic
                });

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

    #loadLinkedComponents(): void {
        const mediaId = this.data?.media?.id;
        if (!mediaId) return;

        this.isLoadingLinkedComponentsSig.set(true);
        this.mediaService.getLinkedComponents(mediaId).pipe(take(1)).subscribe({
            next: (ids) => {
                if (ids.length === 0) {
                    this.linkedComponentsSig.set([]);
                    this.isLoadingLinkedComponentsSig.set(false);
                    return;
                }
                
                forkJoin(ids.map(id => this.#componentService.getComponentById(id))).pipe(take(1)).subscribe({
                    next: (components) => {
                        this.linkedComponentsSig.set(components);
                        this.isLoadingLinkedComponentsSig.set(false);
                    },
                    error: () => this.isLoadingLinkedComponentsSig.set(false)
                });
            },
            error: () => this.isLoadingLinkedComponentsSig.set(false)
        });
    }

    reloadDetails(): void {
        this.#loadMediaDetails();
        this.#loadLinkedComponents();
    }

    openComponent(component: ComponentDto): void {
        this.#matDialog.open(ComponentEditDialogComponent, {
            data: {
                component: component,
                mode: 'edit',
                languages: this.supportedLanguages().map(l => l.code)
            },
            disableClose: true,
            autoFocus: false,
            maxHeight: '90vh'
        });
    }

    deleteVariant(variantId: number): void {
        const mediaId = this.data?.media?.id;
        if (!mediaId) return;

        const confirmation = this.#confirmationService.open({
            title: 'admin.media.deleteVariant.title',
            message: 'admin.media.deleteVariant.message',
            icon: {
                show: true,
                name: 'heroicons_outline:exclamation-triangle',
                color: 'warn'
            },
            actions: {
                confirm: {
                    show: true,
                    label: 'admin.common.actions.delete',
                    color: 'warn'
                },
                cancel: {
                    show: true,
                    label: 'admin.common.actions.cancel'
                }
            },
            dismissible: true
        });

        confirmation.afterClosed().subscribe((result) => {
            if (result === 'confirmed') {
                this.mediaService.deleteVariant(mediaId, variantId)
                    .pipe(take(1))
                    .subscribe({
                        next: () => {
                            this.#notificationService.success('media.variant.delete.success');
                            this.reloadDetails();
                        },
                        error: (err) => this.onError(err)
                    });
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
                this.#transloco.selectTranslate('admin.media.messages.updateSuccess').pipe(take(1)).subscribe(msg => {
                   this.#notificationService.success(msg);
                });
                this.close(true);
            },
            error: (err) => this.onError(err)
        });
    }
}
