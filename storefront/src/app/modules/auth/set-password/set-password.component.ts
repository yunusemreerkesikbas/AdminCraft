import { ChangeDetectionStrategy, Component, OnDestroy, OnInit, ViewChild, ViewEncapsulation, inject, signal } from '@angular/core';
import {
    FormsModule,
    NgForm,
    ReactiveFormsModule,
    UntypedFormBuilder,
    UntypedFormGroup,
    Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TenantContextService } from '@core/tenant';
import { fuseAnimations } from '@fuse/animations';
import { FuseAlertComponent, FuseAlertType } from '@fuse/components/alert';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { AuthService } from 'app/core/auth/auth.service';
import { DeviceFingerprintService } from 'app/core/auth/device-fingerprint.service';
import { ConfigFlagsService } from 'app/core/config/config-flags.service';
import { RecaptchaService } from 'app/core/recaptcha/recaptcha.service';
import { VALIDATION_LIMITS, VALIDATION_PATTERNS } from '@shared/constants/validation.constants';
import { finalize, Subject, take } from 'rxjs';

@Component({
    selector: 'spa-set-password',
    standalone: true,
    templateUrl: './set-password.component.html',
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    animations: fuseAnimations,
    imports: [
        FuseAlertComponent,
        FormsModule,
        ReactiveFormsModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatIconModule,
        MatCheckboxModule,
        MatProgressSpinnerModule,
        RouterLink,
        TranslocoModule,
    ],
})
export class AuthSetPasswordComponent implements OnInit, OnDestroy {
    @ViewChild('setPasswordNgForm') setPasswordNgForm: NgForm;

    #authService = inject(AuthService);
    #configFlags = inject(ConfigFlagsService);
    #deviceFingerprintService = inject(DeviceFingerprintService);
    #formBuilder = inject(UntypedFormBuilder);
    #route = inject(ActivatedRoute);
    #router = inject(Router);
    #tenantContext = inject(TenantContextService);
    #translocoService = inject(TranslocoService);
    #recaptchaService = inject(RecaptchaService);
    #destroySubject = new Subject<void>();

    setPasswordForm: UntypedFormGroup;
    token: string | null = null;

    protected alertSig = signal<{ type: FuseAlertType; message: string }>({
        type: 'success',
        message: '',
    });
    protected showAlertSig = signal(false);
    protected tokenValidSig = signal(false);
    protected validatingTokenSig = signal(true);
    protected maskedEmailSig = signal('');

    ngOnInit(): void {
        this.setPasswordForm = this.#formBuilder.group({
            password: [
                '',
                [
                    Validators.required,
                    Validators.minLength(VALIDATION_LIMITS.USER_PASSWORD_MIN),
                    Validators.pattern(VALIDATION_PATTERNS.PASSWORD_COMPLEXITY),
                ],
            ],
            passwordConfirm: ['', Validators.required],
            trustDevice: [true],
        });

        this.token = this.#route.snapshot.queryParamMap.get('token');
        const subdomain = this.#resolveSubdomain();
        if (!subdomain) {
            this.validatingTokenSig.set(false);
            this.alertSig.set({
                type: 'error',
                message: this.#translocoService.translate('auth.setPassword.errors.tokenVerifyFailed'),
            });
            this.showAlertSig.set(true);
            return;
        }
        if (!this.token) {
            this.validatingTokenSig.set(false);
            this.alertSig.set({
                type: 'error',
                message: this.#translocoService.translate('auth.setPassword.errors.tokenMissing'),
            });
            this.showAlertSig.set(true);
            return;
        }

        this.#authService
            .verifyEmailToken(this.token, subdomain)
            .pipe(
                take(1),
                finalize(() => this.validatingTokenSig.set(false))
            )
            .subscribe({
                next: (response) => {
                    if (response.result === 'SUCCESS' && response.data?.valid) {
                        this.tokenValidSig.set(true);
                        this.maskedEmailSig.set(response.data.email || '');
                    } else {
                        this.alertSig.set({
                            type: 'error',
                            message: this.#translocoService.translate('auth.setPassword.errors.tokenInvalid'),
                        });
                        this.showAlertSig.set(true);
                    }
                },
                error: () => {
                    this.alertSig.set({
                        type: 'error',
                        message: this.#translocoService.translate('auth.setPassword.errors.tokenVerifyFailed'),
                    });
                    this.showAlertSig.set(true);
                },
            });
    }

    #resolveSubdomain(): string | null {
        const hostSubdomain = this.#tenantContext.extractSubdomainFromHost();
        if (hostSubdomain && hostSubdomain !== 'admin') {
            this.#tenantContext.setSubdomain(hostSubdomain);
            return hostSubdomain;
        }
        return null;
    }

    async #getRecaptchaToken(): Promise<string | undefined> {
        const enabled = this.#configFlags.flag('security.recaptcha.enabled', false);
        const siteKey = this.#configFlags.flag('security.recaptcha.site_key', '');
        if (!enabled || !siteKey) return undefined;

        return await this.#recaptchaService.execute('set_password', siteKey);
    }

    async setPassword(): Promise<void> {
        this.setPasswordForm.markAllAsTouched();
        if (!this.token || this.setPasswordForm.invalid) {
            return;
        }

        const password = this.setPasswordForm.get('password')?.value;
        const passwordConfirm = this.setPasswordForm.get('passwordConfirm')?.value;
        const trustDevice = this.setPasswordForm.get('trustDevice')?.value || false;
        const subdomain = this.#tenantContext.subdomain();
        if (!subdomain) {
            this.alertSig.set({
                type: 'error',
                message: this.#translocoService.translate('auth.setPassword.errors.tokenVerifyFailed'),
            });
            this.showAlertSig.set(true);
            return;
        }

        if (password !== passwordConfirm) {
            this.alertSig.set({
                type: 'error',
                message: this.#translocoService.translate('auth.setPassword.errors.passwordsMismatch'),
            });
            this.showAlertSig.set(true);
            return;
        }

        this.setPasswordForm.disable();
        this.showAlertSig.set(false);

        let recaptchaToken: string | undefined;
        let deviceFingerprint: string;
        let deviceName: string;

        try {
            recaptchaToken = await this.#getRecaptchaToken();
            deviceFingerprint = await this.#deviceFingerprintService.getDeviceFingerprint();
            deviceName = this.#deviceFingerprintService.getDeviceName();
        } catch {
            this.setPasswordForm.enable();
            this.alertSig.set({
                type: 'error',
                message: this.#translocoService.translate('auth.setPassword.errors.setFailed'),
            });
            this.showAlertSig.set(true);
            return;
        }

        this.#authService
            .setInitialPassword(
                this.token,
                password,
                passwordConfirm,
                deviceFingerprint,
                trustDevice,
                deviceName,
                subdomain,
                recaptchaToken
            )
            .pipe(
                take(1),
                finalize(() => {
                    this.setPasswordForm.enable();
                })
            )
            .subscribe({
                next: (response) => {
                    if (response.result === 'SUCCESS' || response.success) {
                        this.alertSig.set({
                            type: 'success',
                            message: response.message
                                || this.#translocoService.translate('auth.setPassword.success'),
                        });
                        this.showAlertSig.set(true);

                        setTimeout(() => {
                            const subdomain = this.#tenantContext.subdomain();
                            this.#router.navigate(['/sign-in'], {
                                queryParams: subdomain && subdomain !== 'admin' ? { subdomain } : {},
                            });
                        }, 3000);
                    } else {
                        this.alertSig.set({
                            type: 'error',
                            message: response.message
                                || this.#translocoService.translate('auth.setPassword.errors.setFailed'),
                        });
                        this.showAlertSig.set(true);
                    }
                },
                error: (error) => {
                    this.alertSig.set({
                        type: 'error',
                        message: error?.error?.message
                            || this.#translocoService.translate('auth.setPassword.errors.setFailed'),
                    });
                    this.showAlertSig.set(true);
                },
            });
    }

    ngOnDestroy(): void {
        this.#destroySubject.next();
        this.#destroySubject.complete();
    }
}
