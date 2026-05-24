import {
    ChangeDetectionStrategy,
    Component,
    DestroyRef,
    OnInit,
    inject,
    signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
    FormBuilder,
    FormGroup,
    ReactiveFormsModule,
    Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatRadioModule } from '@angular/material/radio';
import { ApiResponse } from '@core/crud/api.types';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaSelectComponent } from '@shared/components/custom-ui/spa-select/spa-select.component';
import { NotificationService } from '@shared/notifications/notification.service';
import {
    OtpVerificationModalHandle,
    OtpVerificationModalService,
} from '@shared/services/otp-verification-modal.service';
import { FormUtils } from '@shared/utils/form.utils';
import {
    readApiErrorMessage,
    readOtpRateLimitRetrySeconds,
} from '@shared/utils/otp-rate-limit.util';
import { map, of, switchMap } from 'rxjs';
import { PlatformSettingsService } from './platform-settings.service';
import {
    PatchPlatformSettingsRequest,
    PendingOtpPolicySession,
    PlatformSettingsResponse,
    TwoFactorPolicy,
    TwoFactorPolicyChangeRequestResponse,
} from './platform-settings.types';

@Component({
    selector: 'spa-platform-settings',
    templateUrl: './platform-settings.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: true,
    imports: [
        ReactiveFormsModule,
        MatButtonModule,
        MatRadioModule,
        TranslocoModule,
        SpaInputComponent,
        SpaSelectComponent,
    ],
})
export class SpaPlatformSettingsComponent implements OnInit {
    readonly #destroyRef = inject(DestroyRef);
    readonly #fb = inject(FormBuilder);
    readonly #service = inject(PlatformSettingsService);
    readonly #notify = inject(NotificationService);
    readonly #otpModal = inject(OtpVerificationModalService);
    readonly #transloco = inject(TranslocoService);

    protected readonly loadingSig = signal<boolean>(true);
    protected readonly savingSig = signal<boolean>(false);
    protected readonly saveCooldownSig = signal(0);
    readonly #pendingPatchSig = signal<PatchPlatformSettingsRequest>({});

    #otpModalHandle: OtpVerificationModalHandle | null = null;
    #saveCooldownTimer: ReturnType<typeof setInterval> | null = null;
    #pendingSession: PendingOtpPolicySession | null = null;

    protected form: FormGroup = this.#fb.group({
        platformName: ['', [Validators.required, Validators.maxLength(100)]],
        defaultLanguage: ['', Validators.required],
        defaultCurrency: ['', Validators.required],
        emailFromAddress: [
            '',
            [Validators.required, Validators.email, Validators.maxLength(255)],
        ],
        emailFromName: ['', [Validators.required, Validators.maxLength(100)]],
        twoFactorPolicy: ['DISABLED', Validators.required],
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

    protected readonly policyOptions: {
        value: TwoFactorPolicy;
        label: string;
        description: string;
    }[] = [
        {
            value: 'DISABLED',
            label: 'admin.platform.settings.security.twoFactor.policy.disabled',
            description:
                'admin.platform.settings.security.twoFactor.policy.disabledDesc',
        },
        {
            value: 'REQUIRED',
            label: 'admin.platform.settings.security.twoFactor.policy.required',
            description:
                'admin.platform.settings.security.twoFactor.policy.requiredDesc',
        },
    ];

    ngOnInit(): void {
        this.#destroyRef.onDestroy(() => this.#clearSaveCooldown());
        this.#loadSettings();
        this.form
            .get('twoFactorPolicy')
            ?.valueChanges.pipe(takeUntilDestroyed(this.#destroyRef))
            .subscribe((policy: TwoFactorPolicy) => {
                if (this.#pendingSession?.targetPolicy !== policy) {
                    this.#pendingSession = null;
                }
            });
    }

    protected onSave(): void {
        if (
            this.form.invalid ||
            this.form.pristine ||
            this.savingSig() ||
            this.saveCooldownSig() > 0
        ) {
            return;
        }

        const dirty = FormUtils.getDirtyValues<PatchPlatformSettingsRequest>(
            this.form
        );
        const { twoFactorPolicy, ...rest } = dirty;

        if (twoFactorPolicy) {
            this.#pendingPatchSig.set(rest);
            if (this.#isPendingSessionValid(twoFactorPolicy)) {
                this.#openOtpModal(this.#pendingSession!);
                return;
            }
            this.#requestTwoFactorChange(twoFactorPolicy);
            return;
        }

        if (Object.keys(rest).length > 0) {
            this.#patchSettings(rest);
        }
    }

    #requestTwoFactorChange(targetPolicy: TwoFactorPolicy): void {
        this.savingSig.set(true);
        this.#service
            .requestTwoFactorPolicyChange({ twoFactorPolicy: targetPolicy })
            .pipe(takeUntilDestroyed(this.#destroyRef))
            .subscribe({
                next: (response) => {
                    this.savingSig.set(false);
                    this.#cacheSession(response.data);
                    if (response.data.emailSent !== false) {
                        this.#notify.success(
                            response.message ||
                                'admin.platform.settings.security.twoFactor.otpSent'
                        );
                    }
                    this.#openOtpModal(response.data);
                },
                error: (error) => {
                    this.savingSig.set(false);
                    this.#pendingPatchSig.set({});
                    const retrySeconds = readOtpRateLimitRetrySeconds(error);
                    if (retrySeconds !== null) {
                        this.#startSaveCooldown(retrySeconds);
                    }
                    this.#notify.alert(
                        readApiErrorMessage(
                            error,
                            this.#transloco.translate(
                                'admin.platform.settings.security.twoFactor.otpRequestFailed'
                            )
                        )
                    );
                },
            });
    }

    #startSaveCooldown(seconds: number): void {
        this.#clearSaveCooldownTimer();
        this.saveCooldownSig.set(seconds);
        if (seconds <= 0) {
            return;
        }
        this.#saveCooldownTimer = setInterval(() => {
            const remaining = this.saveCooldownSig() - 1;
            if (remaining <= 0) {
                this.saveCooldownSig.set(0);
                this.#clearSaveCooldownTimer();
            } else {
                this.saveCooldownSig.set(remaining);
            }
        }, 1000);
    }

    #clearSaveCooldownTimer(): void {
        if (this.#saveCooldownTimer !== null) {
            clearInterval(this.#saveCooldownTimer);
            this.#saveCooldownTimer = null;
        }
    }

    #clearSaveCooldown(): void {
        this.#clearSaveCooldownTimer();
        this.saveCooldownSig.set(0);
    }

    #cacheSession(data: TwoFactorPolicyChangeRequestResponse): void {
        this.#pendingSession = {
            pendingChangeId: data.pendingChangeId,
            maskedEmail: data.maskedEmail,
            targetPolicy: data.targetPolicy,
            expiresAt: Date.now() + data.expiresInSeconds * 1000,
            resendCooldownSeconds: data.resendCooldownSeconds ?? 180,
        };
    }

    #isPendingSessionValid(targetPolicy: TwoFactorPolicy): boolean {
        const session = this.#pendingSession;
        if (!session || session.targetPolicy !== targetPolicy) {
            return false;
        }
        return Date.now() < session.expiresAt;
    }

    #openOtpModal(
        data: TwoFactorPolicyChangeRequestResponse | PendingOtpPolicySession
    ): void {
        this.#otpModalHandle?.close();

        const pendingChangeId = data.pendingChangeId;
        const expiresAt =
            'expiresAt' in data
                ? data.expiresAt
                : Date.now() + data.expiresInSeconds * 1000;

        this.#otpModalHandle = this.#otpModal.open({
            title: this.#transloco.translate(
                'admin.platform.settings.security.twoFactor.otpTitle'
            ),
            maskedEmail: data.maskedEmail,
            description: this.#transloco.translate(
                'admin.platform.settings.security.twoFactor.otpDescription'
            ),
            confirmLabel: this.#transloco.translate(
                'admin.platform.settings.security.twoFactor.confirm'
            ),
            expiresAt,
            expiresInLabel: this.#transloco.translate(
                'admin.platform.settings.security.twoFactor.expiresIn'
            ),
            expiredLabel: this.#transloco.translate(
                'admin.platform.settings.security.twoFactor.expired'
            ),
            onConfirm: (otpCode) =>
                this.#confirmTwoFactorChange(pendingChangeId, otpCode),
        });

        this.#otpModalHandle.dialogRef
            .afterClosed()
            .pipe(takeUntilDestroyed(this.#destroyRef))
            .subscribe(() => {
                this.#otpModalHandle = null;
            });
    }

    #confirmTwoFactorChange(pendingChangeId: string, otpCode: string): void {
        const handle = this.#otpModalHandle;
        if (!handle) {
            return;
        }

        handle.setConfirming(true);
        handle.setErrorMessage(null);

        this.#service
            .confirmTwoFactorPolicyChange({ pendingChangeId, otpCode })
            .pipe(
                switchMap((response) => {
                    const pending = this.#pendingPatchSig();
                    if (Object.keys(pending).length === 0) {
                        return of(response);
                    }
                    return this.#service.patchSettings(pending).pipe(
                        map(
                            (
                                patched
                            ): ApiResponse<PlatformSettingsResponse> => ({
                                ...response,
                                data: {
                                    ...patched,
                                    twoFactorPolicy:
                                        response.data.twoFactorPolicy,
                                },
                            })
                        )
                    );
                }),
                takeUntilDestroyed(this.#destroyRef)
            )
            .subscribe({
                next: (response) => {
                    handle.setConfirming(false);
                    handle.close();
                    this.#otpModalHandle = null;
                    this.#pendingSession = null;
                    this.#pendingPatchSig.set({});
                    this.#populateForm(response.data);
                    this.#notify.success(
                        response.message ||
                            'admin.platform.settings.messages.saveSuccess'
                    );
                },
                error: (error) => {
                    handle.setConfirming(false);
                    handle.setErrorMessage(error?.error?.message ?? null);
                },
            });
    }

    #patchSettings(payload: PatchPlatformSettingsRequest): void {
        this.savingSig.set(true);
        this.#service
            .patchSettings(payload)
            .pipe(takeUntilDestroyed(this.#destroyRef))
            .subscribe({
                next: (data) => {
                    this.#populateForm(data);
                    this.savingSig.set(false);
                    this.#notify.success(
                        'admin.platform.settings.messages.saveSuccess'
                    );
                },
                error: (error) => {
                    this.savingSig.set(false);
                    this.#notify.alert(
                        error?.error?.message ||
                            'admin.platform.settings.messages.saveFailed'
                    );
                },
            });
    }

    #loadSettings(): void {
        this.loadingSig.set(true);
        this.#service
            .getSettings()
            .pipe(takeUntilDestroyed(this.#destroyRef))
            .subscribe({
                next: (data) => {
                    this.#populateForm(data);
                    this.loadingSig.set(false);
                },
                error: () => {
                    this.loadingSig.set(false);
                    this.#notify.alert(
                        'admin.platform.settings.messages.loadFailed'
                    );
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
        });
        this.form.markAsPristine();
    }
}
