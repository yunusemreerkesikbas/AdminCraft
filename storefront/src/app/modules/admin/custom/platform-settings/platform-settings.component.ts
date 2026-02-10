import {
    ChangeDetectionStrategy,
    Component,
    OnDestroy,
    OnInit,
    inject,
    signal,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatRadioModule } from '@angular/material/radio';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSliderModule } from '@angular/material/slider';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaSelectComponent } from '@shared/components/custom-ui/spa-select/spa-select.component';
import { VALIDATION_LIMITS, VALIDATION_PATTERNS } from '@shared/constants/validation.constants';
import { NotificationService } from '@shared/notifications/notification.service';
import { FormUtils } from '@shared/utils/form.utils';
import { Subject, takeUntil } from 'rxjs';
import { PlatformSettingsService } from './platform-settings.service';
import {
    PatchPlatformSettingsRequest,
    PlatformSettingsResponse,
    TwoFactorPolicy,
} from './platform-settings.types';

@Component({
    selector: 'spa-platform-settings',
    templateUrl: './platform-settings.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: true,
    imports: [
        ReactiveFormsModule,
        MatButtonModule,
        MatIconModule,
        MatRadioModule,
        MatSlideToggleModule,
        MatSliderModule,
        TranslocoModule,
        SpaInputComponent,
        SpaSelectComponent,
    ],
})
export class SpaPlatformSettingsComponent implements OnInit, OnDestroy {
    readonly #fb = inject(FormBuilder);
    readonly #service = inject(PlatformSettingsService);
    readonly #notify = inject(NotificationService);
    readonly #destroy$ = new Subject<void>();

    protected readonly loadingSig = signal<boolean>(true);
    protected readonly savingSig = signal<boolean>(false);

    protected form: FormGroup = this.#fb.group({
        platformName: ['', [Validators.required, Validators.maxLength(100)]],
        defaultLanguage: ['', Validators.required],
        defaultCurrency: ['', Validators.required],
        emailFromAddress: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
        emailFromName: ['', [Validators.required, Validators.maxLength(100)]],
        twoFactorPolicy: ['DISABLED', Validators.required],
        recaptchaEnabled: [false],
        recaptchaSiteKey: [
            '',
            [
                Validators.maxLength(VALIDATION_LIMITS.RECAPTCHA_KEY_LENGTH),
                Validators.pattern(VALIDATION_PATTERNS.RECAPTCHA_KEY),
            ],
        ],
        recaptchaSecretKey: [
            '',
            [
                Validators.maxLength(VALIDATION_LIMITS.RECAPTCHA_KEY_LENGTH),
                Validators.pattern(VALIDATION_PATTERNS.RECAPTCHA_KEY),
            ],
        ],
        recaptchaThreshold: [0.5, [Validators.min(0), Validators.max(1)]],
    });

    protected readonly languages = [
        { value: 'TR', label: 'Turkish' },
        { value: 'EN', label: 'English' },
    ];

    protected readonly currencies = [
        { value: 'TRY', label: 'TRY - Turkish Lira' },
        { value: 'USD', label: 'USD - US Dollar' },
        { value: 'EUR', label: 'EUR - Euro' },
        { value: 'GBP', label: 'GBP - British Pound' },
    ];

    protected readonly policyOptions: { value: TwoFactorPolicy; label: string; description: string }[] = [
        {
            value: 'DISABLED',
            label: 'admin.platform.settings.security.twoFactor.policy.disabled',
            description: 'admin.platform.settings.security.twoFactor.policy.disabledDesc',
        },
        {
            value: 'REQUIRED',
            label: 'admin.platform.settings.security.twoFactor.policy.required',
            description: 'admin.platform.settings.security.twoFactor.policy.requiredDesc',
        },
    ];

    ngOnInit(): void {
        this.#bindConditionalValidators();
        this.#loadSettings();
    }

    protected onSave(): void {
        if (this.form.invalid || this.form.pristine) return;

        const payload = FormUtils.getDirtyValues<PatchPlatformSettingsRequest>(this.form);
        if (payload.recaptchaSiteKey === '') {
            payload.recaptchaSiteKey = null;
        }
        if (payload.recaptchaSecretKey !== undefined && !payload.recaptchaSecretKey?.trim()) {
            delete payload.recaptchaSecretKey;
        }

        this.savingSig.set(true);
        this.#service
            .patchSettings(payload)
            .pipe(takeUntil(this.#destroy$))
            .subscribe({
                next: (data) => {
                    this.#populateForm(data);
                    this.form.patchValue({ recaptchaSecretKey: '' }, { emitEvent: false });
                    this.savingSig.set(false);
                    this.#notify.success('admin.platform.settings.messages.saveSuccess');
                },
                error: () => {
                    this.savingSig.set(false);
                    this.#notify.alert('admin.platform.settings.messages.saveFailed');
                },
            });
    }

    ngOnDestroy(): void {
        this.#destroy$.next();
        this.#destroy$.complete();
    }

    #loadSettings(): void {
        this.loadingSig.set(true);
        this.#service
            .getSettings()
            .pipe(takeUntil(this.#destroy$))
            .subscribe({
                next: (data) => {
                    this.#populateForm(data);
                    this.loadingSig.set(false);
                },
                error: () => {
                    this.loadingSig.set(false);
                    this.#notify.alert('admin.platform.settings.messages.loadFailed');
                },
            });
    }

    #populateForm(data: PlatformSettingsResponse): void {
        this.form.patchValue({
            platformName: data.platformName,
            defaultLanguage: data.defaultLanguage,
            defaultCurrency: data.defaultCurrency,
            emailFromAddress: data.emailFromAddress,
            emailFromName: data.emailFromName,
            twoFactorPolicy: data.twoFactorPolicy ?? 'DISABLED',
            recaptchaEnabled: data.recaptchaEnabled ?? false,
            recaptchaSiteKey: data.recaptchaSiteKey ?? '',
            recaptchaThreshold: data.recaptchaThreshold ?? 0.5,
            recaptchaSecretKey: '',
        });
        this.form.markAsPristine();
    }

    #bindConditionalValidators(): void {
        this.form.get('recaptchaEnabled')?.valueChanges
            .pipe(takeUntil(this.#destroy$))
            .subscribe((enabled) => {
                const siteKeyControl = this.form.get('recaptchaSiteKey');
                if (!siteKeyControl) {
                    return;
                }

                const validators = [
                    Validators.maxLength(VALIDATION_LIMITS.RECAPTCHA_KEY_LENGTH),
                    Validators.pattern(VALIDATION_PATTERNS.RECAPTCHA_KEY),
                ];

                if (enabled) {
                    validators.unshift(Validators.required);
                }

                siteKeyControl.setValidators(validators);
                siteKeyControl.updateValueAndValidity({ emitEvent: false });
            });
    }
}
