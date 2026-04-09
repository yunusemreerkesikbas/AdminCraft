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
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { AdminPageHeaderComponent } from '@shared/components/admin-page-header/admin-page-header.component';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaTextareaComponent } from '@shared/components/custom-ui/spa-textarea/spa-textarea.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { Subject, takeUntil } from 'rxjs';
import {
    MailCampaignStatus,
    MailCampaignVm,
    MailSubscriberStatus,
    MailTemplateTypeDetailVm,
} from './mail-marketing.types';
import { MailCampaignOutboxDialogComponent } from './mail-campaign-outbox-dialog.component';
import { TenantMailMarketingService } from './tenant-mail-marketing.service';

type SupportedLanguage = 'TR' | 'EN';

@Component({
    selector: 'spa-tenant-mail-marketing',
    standalone: true,
    templateUrl: './tenant-mail-marketing.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        CommonModule,
        DatePipe,
        NgClass,
        ReactiveFormsModule,
        MatButtonModule,
        MatIconModule,
        MatSlideToggleModule,
        TranslocoModule,
        AdminPageHeaderComponent,
        SpaInputComponent,
        SpaTextareaComponent,
    ],
})
export class SpaTenantMailMarketingComponent implements OnInit, OnDestroy {
    readonly #fb = inject(FormBuilder);
    readonly #tenantMailMarketingService = inject(TenantMailMarketingService);
    readonly #notificationService = inject(NotificationService);
    readonly #activatedRoute = inject(ActivatedRoute);
    readonly #router = inject(Router);
    readonly #dialog = inject(MatDialog);
    readonly #destroy$ = new Subject<void>();

    protected readonly templateTypeSig = signal<string>('');
    protected readonly loadingSig = signal(true);
    protected readonly savingLanguageSig = signal<SupportedLanguage | null>(null);
    protected readonly sendingCampaignSig = signal(false);
    protected readonly selectedLanguageSig = signal<SupportedLanguage>('TR');
    protected readonly detailSig = signal<MailTemplateTypeDetailVm | null>(null);
    protected readonly campaignHistorySig = signal<MailCampaignVm[]>([]);

    protected readonly lastCampaignSig = computed<MailCampaignVm | null>(
        () => this.detailSig()?.lastCampaign ?? null
    );

    protected readonly sentPercentSig = computed<number>(() => {
        const campaign = this.lastCampaignSig();
        if (!campaign || campaign.totalCount === 0) return 0;
        return Math.round((campaign.sentCount / campaign.totalCount) * 100);
    });

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

    ngOnInit(): void {
        this.#activatedRoute.paramMap
            .pipe(takeUntil(this.#destroy$))
            .subscribe((params) => {
                const templateType = params.get('templateType');
                if (!templateType) {
                    this.#router.navigate(['../'], { relativeTo: this.#activatedRoute });
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

    protected onLanguageToggle(language: SupportedLanguage): void {
        this.selectedLanguageSig.set(language);
    }

    protected onSaveTranslation(language: SupportedLanguage): void {
        const form = language === 'TR' ? this.trForm : this.enForm;
        if (form.invalid) {
            form.markAllAsTouched();
            return;
        }

        this.savingLanguageSig.set(language);
        this.#tenantMailMarketingService
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

    protected onSendCampaign(): void {
        this.sendingCampaignSig.set(true);
        this.#tenantMailMarketingService
            .sendCampaign(this.templateTypeSig())
            .pipe(takeUntil(this.#destroy$))
            .subscribe({
                next: (response) => {
                    this.sendingCampaignSig.set(false);
                    this.detailSig.update((detail) =>
                        detail ? { ...detail, lastCampaign: response.data } : detail
                    );
                    this.#loadCampaignHistory(this.templateTypeSig());
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
        if (!campaign) return;
        this.#tenantMailMarketingService
            .getCampaign(campaign.id)
            .pipe(takeUntil(this.#destroy$))
            .subscribe({
                next: (latestCampaign) => {
                    this.detailSig.update((detail) =>
                        detail ? { ...detail, lastCampaign: latestCampaign } : detail
                    );
                },
                error: (error) =>
                    this.#notificationService.alert(error.error.message),
            });
    }

    protected onOpenOutboxDialog(campaign: MailCampaignVm): void {
        this.#dialog.open(MailCampaignOutboxDialogComponent, {
            width: '560px',
            data: {
                campaign,
                getCampaignOutbox: (id: number) =>
                    this.#tenantMailMarketingService.getCampaignOutbox(id),
            },
        });
    }

    protected onManageSubscribers(): void {
        this.#router.navigate(['../subscribers'], { relativeTo: this.#activatedRoute });
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
        this.#tenantMailMarketingService
            .getTemplateTypeDetail(templateType)
            .pipe(takeUntil(this.#destroy$))
            .subscribe({
                next: (detail) => {
                    this.detailSig.set(detail);
                    this.#patchTranslationForms(detail);
                    this.loadingSig.set(false);
                    this.#loadCampaignHistory(templateType);
                },
                error: (error) => {
                    this.loadingSig.set(false);
                    this.#notificationService.alert(error.error.message);
                },
            });
    }

    #loadDetailOnly(templateType: string): void {
        this.#tenantMailMarketingService
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

    #loadCampaignHistory(templateType: string): void {
        this.#tenantMailMarketingService
            .getCampaignList(templateType)
            .pipe(takeUntil(this.#destroy$))
            .subscribe({
                next: (campaigns) => this.campaignHistorySig.set(campaigns),
                error: () => {},
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
}
