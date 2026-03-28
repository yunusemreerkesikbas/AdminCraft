import {
    ChangeDetectionStrategy,
    Component,
    ViewEncapsulation,
    inject,
    input,
    output,
    signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslocoModule } from '@jsverse/transloco';
import { ApiResponse } from '@core/crud';
import { TenantContextService } from 'app/core/tenant/tenant-context.service';
import { SpaInputComponent } from 'app/shared/components/custom-ui/spa-input/spa-input.component';
import { ConfigConsoleService } from '../console/config-console.service';
import {
    ConfigAuthChallengeResponse,
    ConfigAuthResponse,
    ConfigTokenState,
} from '../console/config-console.types';
import { fuseAnimations } from '@fuse/animations';

@Component({
    selector: 'spa-config-auth',
    standalone: true,
    templateUrl: './config-auth.component.html',
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    animations: fuseAnimations,
    host: { class: 'flex flex-auto flex-col' },
    imports: [
        ReactiveFormsModule,
        MatButtonModule,
        MatFormFieldModule,
        MatInputModule,
        MatIconModule,
        MatProgressSpinnerModule,
        TranslocoModule,
        SpaInputComponent,
    ],
})
export class ConfigAuthComponent {
    readonly #fb = inject(FormBuilder);
    readonly #service = inject(ConfigConsoleService);
    readonly #tenantContext = inject(TenantContextService);

    stage = input<'login' | 'otp'>('login');
    challenge = input<ConfigAuthChallengeResponse | null>(null);

    challengeReceived = output<ConfigAuthChallengeResponse>();
    authenticated = output<ConfigTokenState>();
    cancelOtp = output<void>();

    protected loadingSig = signal(false);
    protected errorSig = signal<string | null>(null);
    protected messageSig = signal<string | null>(null);

    protected loginForm = this.#fb.group({
        email: ['', [Validators.required, Validators.email]],
        password: ['', [Validators.required]],
    });

    protected otpForm = this.#fb.group({
        otpCode: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(6)]],
    });

    protected login(): void {
        if (this.loginForm.invalid || this.loadingSig()) {
            this.loginForm.markAllAsTouched();
            return;
        }

        this.errorSig.set(null);
        this.loadingSig.set(true);

        const subdomain =
            this.#tenantContext.getCurrentSubdomain() ??
            this.#tenantContext.extractSubdomainFromHost();
        if (subdomain) {
            this.#tenantContext.setSubdomain(subdomain);
        }
        const { email, password } = this.loginForm.getRawValue();

        this.#service.login(email!, password!, subdomain).subscribe({
            next: (response: ApiResponse<ConfigAuthChallengeResponse>) => {
                this.loadingSig.set(false);
                if (response.result !== 'SUCCESS' || !response.data) {
                    this.errorSig.set(response.message ?? null);
                    return;
                }
                this.messageSig.set(response.message ?? null);
                this.challengeReceived.emit(response.data);
            },
            error: (error) => {
                this.loadingSig.set(false);
                this.errorSig.set(error?.error?.message ?? null);
            },
        });
    }

    protected verifyOtp(): void {
        if (this.otpForm.invalid || this.loadingSig()) {
            this.otpForm.markAllAsTouched();
            return;
        }

        const ch = this.challenge();
        if (!ch) {
            this.errorSig.set('OTP challenge is missing');
            return;
        }

        this.errorSig.set(null);
        this.loadingSig.set(true);

        this.#service
            .verifyOtp(ch.pendingToken, this.otpForm.getRawValue().otpCode!, ch.subdomain, ch.tenantId)
            .subscribe({
                next: (response: ApiResponse<ConfigAuthResponse>) => {
                    this.loadingSig.set(false);
                    if (response.result !== 'SUCCESS' || !response.data) {
                        this.errorSig.set(response.message ?? null);
                        return;
                    }
                    const tokenState: ConfigTokenState = {
                        accessToken: response.data.accessToken,
                        refreshToken: response.data.refreshToken,
                        tokenType: response.data.tokenType,
                        expiresIn: response.data.expiresIn,
                        userId: response.data.userId,
                        email: response.data.email,
                        fullName: response.data.fullName,
                        role: response.data.role,
                        tenantId: response.data.tenantId,
                        subdomain: response.data.subdomain,
                        issuedAt: response.data.issuedAt,
                    };
                    this.authenticated.emit(tokenState);
                },
                error: (error) => {
                    this.loadingSig.set(false);
                    this.errorSig.set(error?.error?.message ?? null);
                },
            });
    }

    protected cancelOtpClick(): void {
        this.errorSig.set(null);
        this.messageSig.set(null);
        this.otpForm.reset();
        this.cancelOtp.emit();
    }
}
