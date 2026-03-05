import {
    ChangeDetectionStrategy,
    Component,
    EventEmitter,
    Input,
    OnChanges,
    OnDestroy,
    Output,
    SimpleChanges,
    ViewEncapsulation,
    inject,
} from '@angular/core';
import {
    FormBuilder,
    FormGroup,
    ReactiveFormsModule,
    Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaTextareaComponent } from '@shared/components/custom-ui/spa-textarea/spa-textarea.component';
import { SpaToggleComponent } from '@shared/components/custom-ui/spa-toggle/spa-toggle.component';
import { VALIDATION_LIMITS } from '@shared/constants/validation.constants';
import { NotificationService } from '@shared/notifications/notification.service';
import { FormUtils } from '@shared/utils/form.utils';
import { Subject, takeUntil } from 'rxjs';
import { SiteService } from '../../site.service';
import { SiteTechnicalResponse } from '../../site.types';

@Component({
    selector: 'spa-site-technical',
    templateUrl: './site-technical.component.html',
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: true,
    imports: [
        ReactiveFormsModule,
        MatButtonModule,
        MatIconModule,
        TranslocoModule,
        SpaInputComponent,
        SpaTextareaComponent,
        SpaToggleComponent,
    ],
})
export class SpaSiteTechnicalComponent implements OnChanges, OnDestroy {
    readonly #fb = inject(FormBuilder);
    readonly #siteService = inject(SiteService);
    readonly #notificationService = inject(NotificationService);
    readonly #destroy$ = new Subject<void>();

    @Input() technical: SiteTechnicalResponse | null = null;
    @Output() technicalUpdated = new EventEmitter<SiteTechnicalResponse>();

    form: FormGroup;
    saving = false;

    constructor() {
        this.#buildForm();
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['technical'] && this.technical) {
            this.#populateForm();
        }
    }

    ngOnDestroy(): void {
        this.#destroy$.next();
        this.#destroy$.complete();
    }

    save(): void {
        if (this.saving) return;

        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }

        this.saving = true;
        const formValue = this.form.value;

        const payload = {
            robotsTxt: formValue.robotsTxt,
            sitemapEnabled: formValue.sitemapEnabled,
            indexingEnabled: formValue.indexingEnabled,
            googleVerification: formValue.googleVerification,
            bingVerification: formValue.bingVerification,
            yandexVerification: formValue.yandexVerification,
            cookieConsentEnabled: formValue.cookieConsentEnabled,
            cookieConsentText: formValue.cookieConsentText,
        };

        this.#siteService
            .patchTechnicalSettings(payload)
            .pipe(takeUntil(this.#destroy$))
            .subscribe({
                next: (updatedTechnical) => {
                    this.saving = false;
                    this.#notificationService.success(
                        'admin.site.dashboard.messages.saveSuccess'
                    );
                    this.technicalUpdated.emit(updatedTechnical);
                },
                error: (err) => {
                    this.saving = false;

                    if (err.error?.data) {
                        FormUtils.setServerErrors(this.form, err.error.data);
                        this.#notificationService.alert(
                            err.error.message ||
                                'admin.common.messages.validationFailed'
                        );
                    } else {
                        this.#notificationService.alert(
                            'admin.site.dashboard.messages.saveFailed'
                        );
                    }
                },
            });
    }

    #buildForm(): void {
        this.form = this.#fb.group({
            robotsTxt: [
                '',
                [Validators.maxLength(VALIDATION_LIMITS.ROBOTS_TXT_MAX)],
            ],
            sitemapEnabled: [true],
            indexingEnabled: [true],
            googleVerification: [
                '',
                [Validators.maxLength(VALIDATION_LIMITS.VERIFICATION_CODE_MAX)],
            ],
            bingVerification: [
                '',
                [Validators.maxLength(VALIDATION_LIMITS.VERIFICATION_CODE_MAX)],
            ],
            yandexVerification: [
                '',
                [Validators.maxLength(VALIDATION_LIMITS.VERIFICATION_CODE_MAX)],
            ],
            cookieConsentEnabled: [false],
            cookieConsentText: [
                '',
                [
                    Validators.maxLength(
                        VALIDATION_LIMITS.COOKIE_CONSENT_TEXT_MAX
                    ),
                ],
            ],
        });
    }

    #populateForm(): void {
        if (!this.technical) return;

        this.form.patchValue({
            robotsTxt: this.technical.searchEngine?.robotsTxt || '',
            sitemapEnabled: this.technical.searchEngine?.sitemapEnabled ?? true,
            indexingEnabled:
                this.technical.searchEngine?.indexingEnabled ?? true,
            googleVerification:
                this.technical.searchEngine?.verification?.google || '',
            bingVerification:
                this.technical.searchEngine?.verification?.bing || '',
            yandexVerification:
                this.technical.searchEngine?.verification?.yandex || '',
            cookieConsentEnabled:
                this.technical.cookieConsent?.enabled ?? false,
            cookieConsentText: this.technical.cookieConsent?.text || '',
        });
    }
}
