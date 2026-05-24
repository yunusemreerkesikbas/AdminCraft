import { NgClass } from '@angular/common';
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
    signal,
} from '@angular/core';
import {
    FormBuilder,
    FormGroup,
    ReactiveFormsModule,
    Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatRadioModule } from '@angular/material/radio';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { NotificationService } from '@shared/notifications/notification.service';
import {
    OtpVerificationModalHandle,
    OtpVerificationModalService,
} from '@shared/services/otp-verification-modal.service';
import {
    readApiErrorMessage,
    readOtpRateLimitRetrySeconds,
} from '@shared/utils/otp-rate-limit.util';
import { Subject, takeUntil } from 'rxjs';
import { SiteService } from '../../site.service';
import {
    PendingOtpPolicySession,
    SecuritySettingsResponse,
    TwoFactorPolicy,
    TwoFactorPolicyChangeRequestResponse,
} from '../../site.types';

@Component({
    selector: 'spa-site-security',
    templateUrl: './site-security.component.html',
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: true,
    imports: [
        NgClass,
        ReactiveFormsModule,
        MatButtonModule,
        MatIconModule,
        MatRadioModule,
        TranslocoModule,
    ],
})
export class SpaSiteSecurityComponent implements OnChanges, OnDestroy {
    readonly #fb = inject(FormBuilder);
    readonly #siteService = inject(SiteService);
    readonly #notificationService = inject(NotificationService);
    readonly #otpModal = inject(OtpVerificationModalService);
    readonly #transloco = inject(TranslocoService);
    readonly #destroy$ = new Subject<void>();

    @Input() security: SecuritySettingsResponse | null = null;
    @Output() securityUpdated = new EventEmitter<SecuritySettingsResponse>();

    form: FormGroup;
    protected savingSig = signal(false);
    protected saveCooldownSig = signal(0);

    #otpModalHandle: OtpVerificationModalHandle | null = null;
    #saveCooldownTimer: ReturnType<typeof setInterval> | null = null;
    #pendingSession: PendingOtpPolicySession | null = null;

    readonly policyOptions: { value: TwoFactorPolicy; label: string; description: string }[] = [
        {
            value: 'DISABLED',
            label: 'admin.site.dashboard.security.policy.disabled',
            description: 'admin.site.dashboard.security.policy.disabledDesc',
        },
        {
            value: 'REQUIRED',
            label: 'admin.site.dashboard.security.policy.required',
            description: 'admin.site.dashboard.security.policy.requiredDesc',
        },
    ];

    constructor() {
        this.form = this.#fb.group({
            twoFactorPolicy: ['DISABLED', [Validators.required]],
        });
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['security'] && this.security) {
            this.#populateForm();
        }
    }

    selectPolicy(policy: TwoFactorPolicy): void {
        const control = this.form.get('twoFactorPolicy');
        if (!control || control.value === policy) {
            return;
        }

        if (this.#pendingSession?.targetPolicy !== policy) {
            this.#pendingSession = null;
        }

        control.setValue(policy);
        control.markAsDirty();
        this.form.markAsDirty();
    }

    ngOnDestroy(): void {
        this.#clearSaveCooldown();
        this.#otpModalHandle?.close();
        this.#destroy$.next();
        this.#destroy$.complete();
    }

    save(): void {
        if (
            this.savingSig() ||
            this.saveCooldownSig() > 0 ||
            this.form.invalid ||
            !this.form.dirty
        ) {
            return;
        }

        const targetPolicy = this.form.value.twoFactorPolicy as TwoFactorPolicy;
        if (this.#isPendingSessionValid(targetPolicy)) {
            this.#openOtpModal(this.#pendingSession!);
            return;
        }

        this.#requestChange(targetPolicy);
    }

    #requestChange(targetPolicy: TwoFactorPolicy): void {
        this.savingSig.set(true);

        this.#siteService
            .requestTwoFactorPolicyChange({ twoFactorPolicy: targetPolicy })
            .pipe(takeUntil(this.#destroy$))
            .subscribe({
                next: (response) => {
                    this.savingSig.set(false);
                    this.#cacheSession(response.data);
                    if (response.data.emailSent !== false) {
                        this.#notificationService.success(
                            response.message
                                || 'admin.site.dashboard.security.twoFactor.otpSent'
                        );
                    }
                    this.#openOtpModal(response.data);
                },
                error: (error) => {
                    this.savingSig.set(false);
                    const retrySeconds = readOtpRateLimitRetrySeconds(error);
                    if (retrySeconds !== null) {
                        this.#startSaveCooldown(retrySeconds);
                    }
                    this.#notificationService.alert(
                        readApiErrorMessage(
                            error,
                            this.#transloco.translate(
                                'admin.site.dashboard.security.twoFactor.otpRequestFailed'
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
                'admin.site.dashboard.security.twoFactor.otpTitle'
            ),
            maskedEmail: data.maskedEmail,
            description: this.#transloco.translate(
                'admin.site.dashboard.security.twoFactor.otpDescription'
            ),
            confirmLabel: this.#transloco.translate(
                'admin.site.dashboard.security.twoFactor.confirm'
            ),
            expiresAt,
            expiresInLabel: this.#transloco.translate(
                'admin.site.dashboard.security.twoFactor.expiresIn'
            ),
            expiredLabel: this.#transloco.translate(
                'admin.site.dashboard.security.twoFactor.expired'
            ),
            disableClose: false,
            onConfirm: (otpCode) => this.#confirmChange(pendingChangeId, otpCode),
        });

        this.#otpModalHandle.dialogRef
            .afterClosed()
            .pipe(takeUntil(this.#destroy$))
            .subscribe(() => {
                this.#otpModalHandle = null;
            });
    }

    #confirmChange(pendingChangeId: string, otpCode: string): void {
        const handle = this.#otpModalHandle;
        if (!handle) {
            return;
        }

        handle.setConfirming(true);
        handle.setErrorMessage(null);

        this.#siteService
            .confirmTwoFactorPolicyChange({ pendingChangeId, otpCode })
            .pipe(takeUntil(this.#destroy$))
            .subscribe({
                next: (response) => {
                    handle.setConfirming(false);
                    handle.close();
                    this.#otpModalHandle = null;
                    this.#pendingSession = null;
                    this.#populateForm(response.data);
                    this.#notificationService.success(
                        response.message || 'admin.site.dashboard.security.messages.saveSuccess'
                    );
                    this.securityUpdated.emit(response.data);
                },
                error: (error) => {
                    handle.setConfirming(false);
                    handle.setErrorMessage(error?.error?.message ?? null);
                },
            });
    }

    #populateForm(security: SecuritySettingsResponse | null = this.security): void {
        if (!security) {
            return;
        }

        this.form.patchValue({
            twoFactorPolicy: security.twoFactor?.policy || 'DISABLED',
        });
        this.form.markAsPristine();
    }
}
