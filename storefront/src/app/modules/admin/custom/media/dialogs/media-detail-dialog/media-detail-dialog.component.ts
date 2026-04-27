import { CommonModule } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    computed,
    inject,
    signal,
} from '@angular/core';
import { FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TenantContextService } from '@core/tenant/tenant-context.service';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { MediaService } from '@media/media.service';
import {
    MediaDetailDialogData,
    MediaDetailResponse,
    MediaLinkedUsage,
    MediaStatus,
} from '@media/media.types';
import { LanguageResponse } from '@modules/admin/custom/tenants/tenants.types';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaTextareaComponent } from '@shared/components/custom-ui/spa-textarea/spa-textarea.component';
import { SpaToggleComponent } from '@shared/components/custom-ui/spa-toggle/spa-toggle.component';
import { SpaDialogContentComponent } from '@shared/components/spa-dialog/spa-dialog-content/spa-dialog-content.component';
import { SpaDialogFooterComponent } from '@shared/components/spa-dialog/spa-dialog-footer/spa-dialog-footer.component';
import { SpaDialogHeaderComponent } from '@shared/components/spa-dialog/spa-dialog-header/spa-dialog-header.component';
import { SpaGenericModalComponent } from '@shared/components/spa-generic-modal';
import {
    ModalConfig,
    SPA_GENERIC_MODAL_DIALOG_OPTIONS,
} from '@shared/components/spa-generic-modal/spa-generic-modal.types';
import { SpaLocalizedFormDialog } from '@shared/components/spa-localized-form-dialog/spa-localized-form-dialog.directive';
import { VALIDATION_LIMITS } from '@shared/constants/validation.constants';
import { NotificationService } from '@shared/notifications/notification.service';
import { take } from 'rxjs';
import { ComponentEditDialogComponent } from '../../../components/component-edit-dialog/component-edit-dialog.component';
import { ComponentEntryFormComponent } from '../../../components/entries/component-entry-form/component-entry-form.component';
import { ComponentLibraryService } from '../../../components/services/component-library.service';
import { ComponentEntryService } from '../../../components/services/component-entry.service';
import { FocalPointPickerComponent } from '../../components/focal-point-picker/focal-point-picker.component';
import { FormatGeneratorComponent } from '../../components/format-generator/format-generator.component';
import { MediaBindDialogComponent } from '../media-bind-dialog/media-bind-dialog.component';

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
        MatTooltipModule,
        TranslocoModule,
        SpaDialogHeaderComponent,
        SpaDialogContentComponent,
        SpaDialogFooterComponent,
        SpaInputComponent,
        SpaTextareaComponent,
        SpaToggleComponent,
        FormatGeneratorComponent,
        FocalPointPickerComponent,
    ],
    templateUrl: './media-detail-dialog.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MediaDetailDialogComponent extends SpaLocalizedFormDialog<
    boolean,
    MediaDetailDialogData
> {
    protected mediaService = inject(MediaService);
    #tenantContext = inject(TenantContextService);
    #transloco = inject(TranslocoService);
    #notificationService = inject(NotificationService);

    protected supportedLanguages = signal<LanguageResponse[]>([]);
    #mediaDetails = signal<MediaDetailResponse | null>(null);
    #isLoadingDetails = signal(true);
    protected media = computed(() => this.#mediaDetails() ?? this.data?.media);
    protected containerVariants = computed(
        () => this.#mediaDetails()?.container?.variants ?? []
    );
    protected isLoadingDetails = computed(() => this.#isLoadingDetails());
    protected isProcessing = computed(() => {
        const details = this.#mediaDetails();
        if (!details) return false;
        return details.status === MediaStatus.PROCESSING;
    });
    protected hasVariants = computed(() => this.containerVariants().length > 0);
    #componentService = inject(ComponentLibraryService);
    #componentEntryService = inject(ComponentEntryService);
    #matDialog = inject(MatDialog);
    protected linkedComponentsSig = signal<MediaLinkedUsage[]>([]);
    protected isLoadingLinkedComponentsSig = signal(false);
    protected deletingLinkedUsageSig = signal<string | null>(null);

    override ngOnInit(): void {
        const tenant = this.#tenantContext.tenant();
        if (tenant?.supportedLanguages) {
            this.supportedLanguages.set(tenant.supportedLanguages);
            this.languages = tenant.supportedLanguages.map((l) => l.code);
        } else {
            this.languages = ['en'];
        }
        super.ngOnInit();
    }

    protected buildGeneralForm(): FormGroup {
        return this.fb.group({
            originalName: [
                this.data?.media?.originalName || '',
                Validators.required,
            ],
            isPublic: [this.data?.media?.isPublic ?? true],
        });
    }

    protected buildI18nForm(lang: string): FormGroup {
        return this.fb.group({
            altText: [
                '',
                Validators.maxLength(VALIDATION_LIMITS.MEDIA_ALT_TEXT_MAX),
            ],
            title: [
                '',
                Validators.maxLength(VALIDATION_LIMITS.MEDIA_TITLE_MAX),
            ],
            description: [
                '',
                Validators.maxLength(VALIDATION_LIMITS.MEDIA_DESCRIPTION_MAX),
            ],
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
        this.mediaService
            .getDetails(mediaId)
            .pipe(take(1))
            .subscribe({
                next: (details) => {
                    this.#mediaDetails.set(details);
                    this.#isLoadingDetails.set(false);
                    this.generalForm.patchValue({
                        originalName: details.originalName,
                        isPublic: details.isPublic,
                    });

                    if (details.translations) {
                        Object.entries(details.translations).forEach(
                            ([lang, i18n]) => {
                                if (this.i18nForms[lang] && i18n) {
                                    this.i18nForms[lang].patchValue({
                                        altText: i18n.altText || '',
                                        title: i18n.title || '',
                                        description: i18n.description || '',
                                    });
                                }
                            }
                        );
                    }
                },
                error: (err) => {
                    this.#isLoadingDetails.set(false);
                    this.onError(err);
                },
            });
    }

    #loadLinkedComponents(): void {
        const mediaId = this.data?.media?.id;
        if (!mediaId) return;

        this.isLoadingLinkedComponentsSig.set(true);
        this.mediaService
            .getLinkedComponents(mediaId)
            .pipe(take(1))
            .subscribe({
                next: (usages) => {
                    this.linkedComponentsSig.set(usages);
                    this.isLoadingLinkedComponentsSig.set(false);
                },
                error: () => this.isLoadingLinkedComponentsSig.set(false),
            });
    }

    reloadDetails(): void {
        this.#loadMediaDetails();
        this.#loadLinkedComponents();
    }

    openBindDialog(): void {
        const media = this.media();
        if (!media) return;

        this.#matDialog
            .open(MediaBindDialogComponent, {
                width: '640px',
                maxHeight: '90vh',
                disableClose: true,
                data: {
                    media,
                },
            })
            .afterClosed()
            .pipe(take(1))
            .subscribe((success) => {
                if (success) {
                    this.reloadDetails();
                }
            });
    }

    openLinkedUsage(usage: MediaLinkedUsage): void {
        if (usage.entryId) {
            this.#openEntry(usage);
            return;
        }

        this.#openComponent(usage.componentId);
    }

    protected usageTrackBy(index: number, usage: MediaLinkedUsage): string {
        return this.usageGroupKey(usage);
    }

    protected usageGroupKey(usage: MediaLinkedUsage): string {
        return `${usage.componentId}-${usage.entryId ?? 'component'}`;
    }

    protected isDeletingUsage(
        usage: MediaLinkedUsage,
        linkTypeCode: string
    ): boolean {
        return (
            this.deletingLinkedUsageSig() ===
            this.usageLinkKey(usage, linkTypeCode)
        );
    }

    protected isDeletingAnyUsage(usage: MediaLinkedUsage): boolean {
        return usage.linkTypes.some((linkType) =>
            this.isDeletingUsage(usage, linkType.code)
        );
    }

    protected usageLinkKey(
        usage: MediaLinkedUsage,
        linkTypeCode: string
    ): string {
        return `${this.usageGroupKey(usage)}-${linkTypeCode}`;
    }

    deleteLinkedUsage(usage: MediaLinkedUsage, linkTypeCode: string): void {
        const mediaId = this.data?.media?.id;
        if (!mediaId) return;

        this.#openDeleteModal(
            'admin.media.dialogs.unlink.title',
            usage.entryId
                ? 'admin.media.dialogs.unlink.messageEntry'
                : 'admin.media.dialogs.unlink.messageComponent'
        )
            .pipe(take(1))
            .subscribe((confirmed) => {
                if (!confirmed) {
                    return;
                }

                this.deletingLinkedUsageSig.set(
                    this.usageLinkKey(usage, linkTypeCode)
                );
                this.mediaService
                    .unlinkMedia(mediaId, usage, linkTypeCode)
                    .pipe(take(1))
                    .subscribe({
                        next: (response) => {
                            this.deletingLinkedUsageSig.set(null);
                            this.#notificationService.success(
                                response.message ?? 'admin.common.success.deleted'
                            );
                            this.reloadDetails();
                        },
                        error: (err) => {
                            this.deletingLinkedUsageSig.set(null);
                            this.onError(err);
                        },
                    });
            });
    }

    #openDeleteModal(titleKey: string, messageKey: string) {
        const dialogConfig: ModalConfig = {
            type: 'warning',
            variant: 'confirmation',
            title: this.#transloco.translate(titleKey),
            icon: 'warning',
            data: null,
            sections: [
                {
                    type: 'alert-box',
                    alertType: 'warning',
                    content: this.#transloco.translate(messageKey),
                },
            ],
            actions: [
                {
                    label: this.#transloco.translate(
                        'admin.common.actions.cancel'
                    ),
                    value: false,
                },
                {
                    label: this.#transloco.translate(
                        'admin.common.actions.delete'
                    ),
                    color: 'warn',
                    value: true,
                },
            ],
        };

        return this.#matDialog
            .open(SpaGenericModalComponent, {
                ...SPA_GENERIC_MODAL_DIALOG_OPTIONS,
                width: '500px',
                maxHeight: '50vh',
                data: dialogConfig,
            })
            .afterClosed();
    }

    #openComponent(componentId: number): void {
        this.#componentService
            .getComponentById(componentId)
            .pipe(take(1))
            .subscribe({
                next: (component) => {
                    this.#matDialog.open(ComponentEditDialogComponent, {
                        width: '900px',
                        height: '90vh',
                        maxHeight: '90vh',
                        panelClass: 'spa-compact-dialog',
                        data: {
                            component: component,
                            mode: 'edit',
                            languages: this.supportedLanguages().map((l) => l.code),
                        },
                        disableClose: true,
                        autoFocus: false,
                    });
                },
                error: () =>
                    this.#notificationService.alert(
                        'admin.components.errors.loadDetailFailed'
                    ),
            });
    }

    #openEntry(usage: MediaLinkedUsage): void {
        if (!usage.entryId) return;

        this.#componentService
            .getComponentDetail(usage.componentId)
            .pipe(take(1))
            .subscribe({
                next: (component) => {
                    this.#componentEntryService
                        .getEntryDetail(usage.entryId!)
                        .pipe(take(1))
                        .subscribe({
                            next: (entry) => {
                                this.#matDialog.open(ComponentEntryFormComponent, {
                                    width: '800px',
                                    maxHeight: '90vh',
                                    disableClose: true,
                                    data: {
                                        mode: 'edit',
                                        componentId: usage.componentId,
                                        componentTypeId: component.componentTypeId,
                                        languages: this.supportedLanguages().map((l) => l.code),
                                        entryId: usage.entryId,
                                        entry,
                                        translations: entry.translations,
                                    },
                                });
                            },
                            error: () =>
                                this.#notificationService.alert(
                                    'admin.messages.loadError'
                                ),
                        });
                },
                error: () =>
                    this.#notificationService.alert(
                        'admin.components.errors.loadDetailFailed'
                    ),
            });
    }

    deleteVariant(variantId: number): void {
        const mediaId = this.data?.media?.id;
        if (!mediaId) return;

        this.#openDeleteModal(
            'admin.media.deleteVariant.title',
            'admin.media.deleteVariant.message'
        )
            .pipe(take(1))
            .subscribe((confirmed) => {
                if (!confirmed) {
                    return;
                }

                this.mediaService
                    .deleteVariant(mediaId, variantId)
                    .pipe(take(1))
                    .subscribe({
                        next: (response) => {
                            this.#notificationService.success(response.message!);
                            this.reloadDetails();
                        },
                        error: (err) => this.onError(err),
                    });
            });
    }

    save(): void {
        if (!this.isAllFormsValid()) return;

        const mediaId = this.data?.media?.id;
        if (!mediaId) return;

        const generalData = this.generalForm.value;
        const translations: Record<string, any> = {};

        this.languages.forEach((lang) => {
            if (this.formHasContent(this.i18nForms[lang])) {
                translations[lang] = this.i18nForms[lang].value;
            }
        });

        this.mediaService
            .updateComposite(mediaId, {
                originalName: generalData.originalName,
                isPublic: generalData.isPublic,
                translations: translations,
            })
            .pipe(take(1))
            .subscribe({
                next: (response) => {
                    this.#notificationService.success(response.message!);
                    this.close(true);
                },
                error: (err) => this.onError(err),
            });
    }
}
