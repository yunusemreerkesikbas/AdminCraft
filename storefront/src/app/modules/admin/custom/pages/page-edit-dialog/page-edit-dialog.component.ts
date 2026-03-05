import { SpaLocalizedFormDialogData } from '@/app/shared/components/spa-dialog-base';
import { UpperCasePipe } from '@angular/common';
import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    inject,
    signal,
    ViewEncapsulation,
} from '@angular/core';
import { FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { TenantContextService } from '@core/tenant/tenant-context.service';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaSelectComponent } from '@shared/components/custom-ui/spa-select/spa-select.component';
import { SpaTextareaComponent } from '@shared/components/custom-ui/spa-textarea/spa-textarea.component';
import { SpaDialogComponent } from '@shared/components/spa-dialog';
import { SpaLocalizedFormDialog } from '@shared/components/spa-localized-form-dialog';
import {
    SpaTabContainerComponent,
    SpaTabContentDirective,
    TabDefinition,
} from '@shared/components/spa-tab-container';
import {
    VALIDATION_LIMITS,
    VALIDATION_PATTERNS,
} from '@shared/constants/validation.constants';
import { NotificationService } from '@shared/notifications/notification.service';
import { take } from 'rxjs';
import { PageTemplateService } from '../../templates/page-template.service';
import { PageTemplate } from '../../templates/page-template.types';
import { PageBuilderService } from '../page-builder.service';
import {
    CreatePageCompositeRequest,
    PageDetailDto,
    PageStatus,
    RobotTag,
    UpdatePageCompositeRequest,
} from '../page-builder.types';

export interface PageEditDialogData
    extends SpaLocalizedFormDialogData<PageDetailDto> {
    mode: 'create' | 'edit';
    page?: PageDetailDto;
    languages: string[];
}

@Component({
    selector: 'spa-page-edit-dialog',
    standalone: true,
    imports: [
        ReactiveFormsModule,
        MatIconModule,
        TranslocoModule,
        UpperCasePipe,
        SpaInputComponent,
        SpaSelectComponent,
        SpaTextareaComponent,
        SpaDialogComponent,
        SpaTabContainerComponent,
        SpaTabContentDirective,
    ],
    templateUrl: './page-edit-dialog.component.html',
    styleUrl: './page-edit-dialog.component.scss',
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PageEditDialogComponent extends SpaLocalizedFormDialog<
    any,
    PageEditDialogData
> {
    readonly #translocoService = inject(TranslocoService);
    readonly #pageService = inject(PageBuilderService);
    readonly #templateService = inject(PageTemplateService);
    readonly #notify = inject(NotificationService);
    readonly #tenantContext = inject(TenantContextService);
    readonly #cdr = inject(ChangeDetectorRef);

    // Template data signals
    protected pageTemplates = signal<PageTemplate[]>([]);
    protected templatesLoading = signal<boolean>(false);

    // Template options for spa-select
    templateOptions: { value: number | null; label: string }[] = [];

    statusOptions: { value: PageStatus; label: string }[] = [
        { value: 'DRAFT', label: 'DRAFT' },
        { value: 'PUBLISHED', label: 'PUBLISHED' },
        { value: 'ARCHIVED', label: 'ARCHIVED' },
    ];

    robotTagOptions: { value: RobotTag; label: string }[] = [
        { value: 'INDEX_FOLLOW', label: 'INDEX_FOLLOW' },
        { value: 'NOINDEX_FOLLOW', label: 'NOINDEX_FOLLOW' },
        { value: 'INDEX_NOFOLLOW', label: 'INDEX_NOFOLLOW' },
        { value: 'NOINDEX_NOFOLLOW', label: 'NOINDEX_NOFOLLOW' },
    ];

    override languages: string[] = [];

    readonly dialogTitle = this.data?.page
        ? this.#translocoService.translate('admin.dialog.title.edit')
        : this.#translocoService.translate('admin.dialog.title.create');

    get tabs(): TabDefinition[] {
        return [
            {
                id: 'general',
                label: 'admin.pages.tabs.general',
                icon: 'settings',
            },
            ...this.languages.map((lang) => ({
                id: 'lang-' + lang,
                label: lang.toUpperCase(),
                icon: 'translate',
            })),
        ];
    }

    override ngOnInit(): void {
        const tenant = this.#tenantContext.tenant();
        if (this.data?.languages?.length) {
            this.languages = this.data.languages;
        } else if (tenant?.supportedLanguages) {
            this.languages = tenant.supportedLanguages.map((l) => l.code);
        } else {
            this.languages = ['tr', 'en'];
        }
        if (this.data?.mode === 'create') {
            this.#loadPageTemplates();
        } else if (this.data?.page?.templateId != null) {
            const label = this.data.page.templateUid;
            this.templateOptions = [
                { value: this.data.page.templateId, label },
            ];
        }
        super.ngOnInit();
    }

    #loadPageTemplates(): void {
        this.templatesLoading.set(true);
        this.#templateService
            .getActive()
            .pipe(take(1))
            .subscribe({
                next: (templates) => {
                    this.pageTemplates.set(templates);
                    this.templateOptions = [
                        ...templates.map((t) => ({
                            value: t.id,
                            label: t.uid,
                        })),
                    ];
                    this.templatesLoading.set(false);
                    this.#cdr.markForCheck();
                },
                error: () => {
                    this.#notify.alert(
                        'admin.pages.errors.loadTemplatesFailed'
                    );
                    this.templatesLoading.set(false);
                },
            });
    }

    protected buildGeneralForm(): FormGroup {
        const page = this.data?.page;
        const createdAt = page?.createdAt;
        const updatedAt = page?.updatedAt;
        const isEdit = this.data?.mode === 'edit' || !!page;

        return this.fb.group({
            templateId: [{ value: page?.templateId ?? null, disabled: isEdit }],
            status: [page?.status || 'DRAFT', Validators.required],
            styleClasses: [
                page?.styleClasses || '',
                [
                    Validators.maxLength(
                        VALIDATION_LIMITS.PAGE_STYLE_CLASSES_MAX
                    ),
                ],
            ],
            robotTag: [page?.robotTag || 'INDEX_FOLLOW', Validators.required],
            uid: [
                { value: page?.uid || '', disabled: false },
                [
                    Validators.maxLength(VALIDATION_LIMITS.UID_PAGE_MAX),
                    Validators.pattern(VALIDATION_PATTERNS.UID),
                ],
            ],
            createdAt: [{ value: createdAt || '-', disabled: true }],
            updatedAt: [{ value: updatedAt || '-', disabled: true }],
        });
    }

    protected buildI18nForm(lang: string): FormGroup {
        const translation =
            this.data?.page?.translations?.[
                lang as keyof typeof this.data.page.translations
            ];
        return this.fb.group({
            canonicalUrl: [
                translation?.canonicalUrl || '',
                [
                    Validators.maxLength(
                        VALIDATION_LIMITS.PAGE_CANONICAL_URL_MAX
                    ),
                ],
            ],
            title: [
                translation?.title || translation?.name || '',
                [
                    Validators.required,
                    Validators.maxLength(VALIDATION_LIMITS.PAGE_TITLE_MAX),
                ],
            ],
            description: [
                translation?.description || '',
                [Validators.maxLength(VALIDATION_LIMITS.PAGE_DESCRIPTION_MAX)],
            ],
        });
    }

    save(): void {
        if (this.generalForm.invalid) return;

        const translations = this.#buildTranslations();

        this.setSubmitting(true);

        if (this.data.mode === 'create') {
            this.#createPageComposite(translations);
        } else {
            this.#updatePageComposite(translations);
        }
    }

    #createPageComposite(translations: Record<string, any>): void {
        const generalData = this.generalForm.getRawValue();

        const request: CreatePageCompositeRequest = {
            templateId: generalData.templateId,
            status: generalData.status,
            styleClasses: generalData.styleClasses || null,
            robotTag: generalData.robotTag,
            uid: generalData.uid || null,
            translations: translations,
        };

        this.#pageService
            .createComposite(request)
            .pipe(take(1))
            .subscribe({
                next: () => {
                    this.#notify.success('admin.pages.success.created');
                    this.close(true);
                },
                error: () => {
                    this.setSubmitting(false);
                    this.#notify.alert('admin.pages.errors.createFailed');
                },
            });
    }

    #updatePageComposite(translations: Record<string, any>): void {
        if (!this.data.page?.id) return;
        const generalData = this.generalForm.getRawValue();

        const request: UpdatePageCompositeRequest = {
            uid: generalData.uid ?? null,
            templateId: generalData.templateId,
            status: generalData.status,
            styleClasses: generalData.styleClasses || null,
            translations: translations,
        };

        this.#pageService
            .updateComposite(this.data.page.id, request)
            .pipe(take(1))
            .subscribe({
                next: () => {
                    this.#notify.success('admin.pages.success.updated');
                    this.close(true);
                },
                error: () => {
                    this.setSubmitting(false);
                    this.#notify.alert('admin.pages.errors.updateFailed');
                },
            });
    }

    #buildTranslations(): Record<string, any> {
        const translations: Record<string, any> = {};

        this.languages.forEach((lang) => {
            const form = this.i18nForms[lang];
            if (!form) return;

            const title = form.value.title as string;
            const canonicalUrl = form.value.canonicalUrl as string;

            if (
                (title && title.trim().length > 0) ||
                (canonicalUrl && canonicalUrl.trim().length > 0)
            ) {
                translations[lang.toUpperCase()] = {
                    canonicalUrl: form.value.canonicalUrl?.trim() || undefined,
                    title: title?.trim() || undefined,
                    description: form.value.description?.trim() || undefined,
                    status: this.generalForm.get('status')?.value,
                } as any;
            }
        });

        return translations;
    }
}
