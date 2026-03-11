import { CommonModule, DatePipe, NgClass } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    OnDestroy,
    OnInit,
    computed,
    inject,
    signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { AdminPageHeaderComponent } from '@shared/components/admin-page-header/admin-page-header.component';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaTextareaComponent } from '@shared/components/custom-ui/spa-textarea/spa-textarea.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { Subject, forkJoin, takeUntil } from 'rxjs';
import {
    MailCampaignStatus,
    MailCampaignVm,
    MailSubscriberStatus,
    MailSubscriberVm,
    MailTemplateTypeDetailVm,
} from './mail-marketing.types';
import { PlatformMailMarketingService } from './platform-mail-marketing.service';

type SupportedLanguage = 'TR' | 'EN';

@Component({
    selector: 'spa-platform-mail-marketing',
    standalone: true,
    templateUrl: './platform-mail-marketing.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        CommonModule,
        DatePipe,
        NgClass,
        ReactiveFormsModule,
        MatButtonModule,
        MatIconModule,
        MatSlideToggleModule,
        MatTabsModule,
        TranslocoModule,
        AdminPageHeaderComponent,
        SpaInputComponent,
        SpaTextareaComponent,
    ],
})
export class SpaPlatformMailMarketingComponent implements OnInit, OnDestroy {
    readonly #fb = inject(FormBuilder);
    readonly #platformMailMarketingService = inject(
        PlatformMailMarketingService
    );
    readonly #notificationService = inject(NotificationService);
    readonly #activatedRoute = inject(ActivatedRoute);
    readonly #router = inject(Router);
    readonly #destroy$ = new Subject<void>();

    protected readonly templateTypeSig = signal<string>('');
    protected readonly loadingSig = signal(true);
    protected readonly savingLanguageSig = signal<SupportedLanguage | null>(
        null
    );
    protected readonly sendingCampaignSig = signal(false);
    protected readonly selectedLanguageSig = signal<SupportedLanguage>('TR');
    protected readonly detailSig = signal<MailTemplateTypeDetailVm | null>(
        null
    );
    protected readonly subscribersSig = signal<MailSubscriberVm[]>([]);

    protected readonly trForm = this.#fb.group({
        subject: this.#fb.control('', [Validators.required]),
        content: this.#fb.control('', [Validators.required]),
        active: this.#fb.control(true),
    });

    protected readonly enForm = this.#fb.group({
        subject: this.#fb.control('', [Validators.required]),
        content: this.#fb.control('', [Validators.required]),
        active: this.#fb.control(true),
    });

    protected readonly lastCampaignSig = computed<MailCampaignVm | null>(
        () => this.detailSig()?.lastCampaign ?? null
    );

    ngOnInit(): void {
        this.#activatedRoute.paramMap
            .pipe(takeUntil(this.#destroy$))
            .subscribe((params) => {
                const templateType = params.get('templateType');
                if (!templateType) {
                    this.#router.navigate(['../'], {
                        relativeTo: this.#activatedRoute,
                    });
                    return;
                }
                this.templateTypeSig.set(templateType);
                this.#loadData(templateType);
            });
    }

    ngOnDestroy(): void {
        this.#destroy$.next();
        this.#destroy$.complete();
    }

    protected onBack(): void {
        this.#router.navigate(['../'], { relativeTo: this.#activatedRoute });
    }

    protected onLanguageTabChange(index: number): void {
        this.selectedLanguageSig.set(index === 0 ? 'TR' : 'EN');
    }

    protected onSaveTranslation(language: SupportedLanguage): void {
        const form = language === 'TR' ? this.trForm : this.enForm;
        if (form.invalid) {
            form.markAllAsTouched();
            return;
        }

        this.savingLanguageSig.set(language);
        this.#platformMailMarketingService
            .updateTemplateTranslation(this.templateTypeSig(), language, {
                subject: form.controls.subject.value ?? '',
                content: form.controls.content.value ?? '',
                active: !!form.controls.active.value,
            })
            .pipe(takeUntil(this.#destroy$))
            .subscribe({
                next: (response) => {
                    this.savingLanguageSig.set(null);
                    if (response.message) {
                        this.#notificationService.success(response.message);
                    }
                    this.#loadDetailOnly(this.templateTypeSig());
                },
                error: (error) => {
                    this.savingLanguageSig.set(null);
                    this.#notificationService.alert(error.error.message);
                },
            });
    }

    protected onSendCampaign(language: SupportedLanguage): void {
        const translation = this.#translationByLanguage(language);
        if (!translation?.id) {
            return;
        }

        this.sendingCampaignSig.set(true);
        this.#platformMailMarketingService
            .sendCampaign(translation.id)
            .pipe(takeUntil(this.#destroy$))
            .subscribe({
                next: (response) => {
                    this.sendingCampaignSig.set(false);
                    this.detailSig.update((detail) =>
                        detail
                            ? {
                                  ...detail,
                                  lastCampaign: response.data,
                              }
                            : detail
                    );
                    if (response.message) {
                        this.#notificationService.success(response.message);
                    }
                },
                error: (error) => {
                    this.sendingCampaignSig.set(false);
                    this.#notificationService.alert(error.error.message);
                },
            });
    }

    protected onRefreshCampaign(): void {
        const campaign = this.lastCampaignSig();
        if (!campaign) {
            return;
        }
        this.#platformMailMarketingService
            .getCampaign(campaign.id)
            .pipe(takeUntil(this.#destroy$))
            .subscribe({
                next: (latestCampaign) => {
                    this.detailSig.update((detail) =>
                        detail
                            ? {
                                  ...detail,
                                  lastCampaign: latestCampaign,
                              }
                            : detail
                    );
                },
                error: (error) =>
                    this.#notificationService.alert(error.error.message),
            });
    }

    protected onRefreshSubscribers(): void {
        this.#platformMailMarketingService
            .getSubscribers(this.templateTypeSig())
            .pipe(takeUntil(this.#destroy$))
            .subscribe({
                next: (subscribers) => this.subscribersSig.set(subscribers),
                error: (error) =>
                    this.#notificationService.alert(error.error.message),
            });
    }

    protected onManageSubscribers(): void {
        this.#router.navigate(['../subscribers'], {
            relativeTo: this.#activatedRoute,
        });
    }

    protected getCampaignStatusClass(status: MailCampaignStatus): string {
        if (status === 'COMPLETED') {
            return 'bg-green-100 text-green-800 dark:bg-green-900/40 dark:text-green-200';
        }
        if (status === 'COMPLETED_WITH_ERRORS') {
            return 'bg-amber-100 text-amber-800 dark:bg-amber-900/40 dark:text-amber-200';
        }
        if (status === 'FAILED') {
            return 'bg-red-100 text-red-800 dark:bg-red-900/40 dark:text-red-200';
        }
        return 'bg-blue-100 text-blue-800 dark:bg-blue-900/40 dark:text-blue-200';
    }

    protected getSubscriberStatusClass(status: MailSubscriberStatus): string {
        if (status === 'ACTIVE') {
            return 'bg-green-100 text-green-800 dark:bg-green-900/40 dark:text-green-200';
        }
        if (status === 'UNSUBSCRIBED') {
            return 'bg-red-100 text-red-800 dark:bg-red-900/40 dark:text-red-200';
        }
        return 'bg-blue-100 text-blue-800 dark:bg-blue-900/40 dark:text-blue-200';
    }

    #loadData(templateType: string): void {
        this.loadingSig.set(true);
        forkJoin({
            detail: this.#platformMailMarketingService.getTemplateTypeDetail(
                templateType
            ),
            subscribers:
                this.#platformMailMarketingService.getSubscribers(templateType),
        })
            .pipe(takeUntil(this.#destroy$))
            .subscribe({
                next: ({ detail, subscribers }) => {
                    this.detailSig.set(detail);
                    this.subscribersSig.set(subscribers);
                    this.#patchTranslationForms(detail);
                    this.loadingSig.set(false);
                },
                error: (error) => {
                    this.loadingSig.set(false);
                    this.#notificationService.alert(error.error.message);
                },
            });
    }

    #loadDetailOnly(templateType: string): void {
        this.#platformMailMarketingService
            .getTemplateTypeDetail(templateType)
            .pipe(takeUntil(this.#destroy$))
            .subscribe({
                next: (detail) => {
                    this.detailSig.set(detail);
                    this.#patchTranslationForms(detail);
                },
                error: (error) =>
                    this.#notificationService.alert(error.error.message),
            });
    }

    #patchTranslationForms(detail: MailTemplateTypeDetailVm): void {
        const trTemplate = detail.templates.find(
            (template) => template.language?.toUpperCase() === 'TR'
        );
        const enTemplate = detail.templates.find(
            (template) => template.language?.toUpperCase() === 'EN'
        );

        this.trForm.patchValue({
            subject: trTemplate?.subject ?? '',
            content: trTemplate?.content ?? '',
            active: trTemplate?.active ?? true,
        });
        this.trForm.markAsPristine();

        this.enForm.patchValue({
            subject: enTemplate?.subject ?? '',
            content: enTemplate?.content ?? '',
            active: enTemplate?.active ?? true,
        });
        this.enForm.markAsPristine();
    }

    #translationByLanguage(language: SupportedLanguage) {
        return this.detailSig()?.templates.find(
            (template) => template.language?.toUpperCase() === language
        );
    }
}
