import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    OnDestroy,
    ViewEncapsulation,
    inject,
    signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import {
    MAT_DIALOG_DATA,
    MatDialogModule,
    MatDialogRef,
} from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaOtpInputComponent } from '@shared/components/custom-ui/spa-otp-input/spa-otp-input.component';
import { NotificationService } from '@shared/notifications/notification.service';
import {
    ModalConfig,
    OtpVerificationModalData,
} from './spa-generic-modal.types';

@Component({
    selector: 'spa-generic-modal',
    standalone: true,
    imports: [
        ReactiveFormsModule,
        MatDialogModule,
        MatButtonModule,
        MatIconModule,
        MatDividerModule,
        MatTooltipModule,
        MatProgressSpinnerModule,
        TranslocoModule,
        SpaOtpInputComponent,
    ],
    templateUrl: './spa-generic-modal.component.html',
    styleUrls: ['./spa-generic-modal.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SpaGenericModalComponent<T = unknown> implements OnDestroy {
    readonly #dialogRef = inject(MatDialogRef<SpaGenericModalComponent<T>>);
    readonly #notification = inject(NotificationService);
    readonly #fb = inject(FormBuilder);
    readonly #cdr = inject(ChangeDetectorRef);

    readonly config = inject<ModalConfig<T>>(MAT_DIALOG_DATA);

    protected readonly errorMessageSig = signal<string | null>(null);
    protected readonly confirmingSig = signal(false);
    protected readonly expiresRemainingSig = signal<string | null>(null);
    protected readonly otpExpiredSig = signal(false);

    #expiryTimerId: ReturnType<typeof setInterval> | null = null;

    protected readonly otpForm = this.#fb.group({
        otpCode: [
            '',
            [
                Validators.required,
                Validators.minLength(6),
                Validators.maxLength(6),
                Validators.pattern(/^\d{6}$/),
            ],
        ],
    });

    constructor() {
        this.#startExpiryCountdown();
    }

    ngOnDestroy(): void {
        this.#clearExpiryTimer();
    }

    get modalClass(): string {
        return `modal-${this.config.type}`;
    }

    get isOtpVariant(): boolean {
        return this.config.variant === 'otp-verification';
    }

    get otpData(): OtpVerificationModalData | null {
        return this.isOtpVariant
            ? (this.config.data as OtpVerificationModalData)
            : null;
    }

    setErrorMessage(message: string | null): void {
        this.errorMessageSig.set(message);
        this.#cdr.markForCheck();
    }

    setConfirming(loading: boolean): void {
        this.confirmingSig.set(loading);
        if (loading) {
            this.otpForm.disable({ emitEvent: false });
        } else if (!this.otpExpiredSig()) {
            this.otpForm.enable({ emitEvent: false });
        }
        this.#cdr.markForCheck();
    }

    protected copyToClipboard(value: string, label: string): void {
        navigator.clipboard.writeText(value).then(
            () => {
                this.#notification.success(`${label} copied to clipboard`);
            },
            () => {
                this.#notification.alert('Failed to copy to clipboard');
            }
        );
    }

    protected executeAction(action?: {
        handler?: () => void;
        value?: unknown;
    }): void {
        if (action?.handler) {
            action.handler();
        }
        this.#dialogRef.close(action?.value);
    }

    protected close(): void {
        this.#dialogRef.close();
    }

    protected submitOtp(): void {
        if (this.confirmingSig() || this.otpExpiredSig()) {
            return;
        }

        if (this.otpForm.invalid) {
            this.otpForm.markAllAsTouched();
            return;
        }

        const otpCode = this.otpForm.getRawValue().otpCode ?? '';
        this.otpData?.onConfirm?.(otpCode);
    }

    #startExpiryCountdown(): void {
        const expiresAt = this.otpData?.expiresAt;
        if (!expiresAt) {
            return;
        }

        const tick = (): void => {
            const remainingMs = expiresAt - Date.now();
            if (remainingMs <= 0) {
                this.expiresRemainingSig.set('00:00');
                this.otpExpiredSig.set(true);
                this.otpForm.disable({ emitEvent: false });
                this.#clearExpiryTimer();
            } else {
                this.expiresRemainingSig.set(
                    this.#formatRemaining(remainingMs)
                );
                this.otpExpiredSig.set(false);
            }
            this.#cdr.markForCheck();
        };

        tick();
        this.#expiryTimerId = setInterval(tick, 1000);
    }

    #formatRemaining(remainingMs: number): string {
        const totalSeconds = Math.ceil(remainingMs / 1000);
        const minutes = Math.floor(totalSeconds / 60);
        const seconds = totalSeconds % 60;
        return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
    }

    #clearExpiryTimer(): void {
        if (this.#expiryTimerId !== null) {
            clearInterval(this.#expiryTimerId);
            this.#expiryTimerId = null;
        }
    }
}
