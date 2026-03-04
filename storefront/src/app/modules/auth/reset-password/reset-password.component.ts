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
import { ConfigFlagsService } from 'app/core/config/config-flags.service';
import { RecaptchaService } from 'app/core/recaptcha/recaptcha.service';
import { VALIDATION_LIMITS, VALIDATION_PATTERNS } from '@shared/constants/validation.constants';
import { finalize, Subject, take } from 'rxjs';

@Component({
    selector: 'spa-reset-password',
    standalone: true,
    templateUrl: './reset-password.component.html',
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
        MatProgressSpinnerModule,
        RouterLink,
        TranslocoModule,
    ],
})
export class AuthResetPasswordComponent implements OnInit, OnDestroy {
    @ViewChild('resetPasswordNgForm') resetPasswordNgForm: NgForm;

    #authService = inject(AuthService);
    #configFlags = inject(ConfigFlagsService);
    #formBuilder = inject(UntypedFormBuilder);
    #route = inject(ActivatedRoute);
    #router = inject(Router);
    #tenantContext = inject(TenantContextService);
    #translocoService = inject(TranslocoService);
    #recaptchaService = inject(RecaptchaService);
    #destroySubject = new Subject<void>();

    resetPasswordForm: UntypedFormGroup;
    token: string | null = null;

    protected alertSig = signal<{ type: FuseAlertType; message: string }>({
        type: 'success',
        message: '',
    });
    protected showAlertSig = signal(false);
    protected tokenValidSig = signal(false);
    protected validatingTokenSig = signal(true);

    ngOnInit(): void {
        this.resetPasswordForm = this.#formBuilder.group({
            password: [
                '',
                [
                    Validators.required,
                    Validators.minLength(VALIDATION_LIMITS.USER_PASSWORD_MIN),
                    Validators.pattern(VALIDATION_PATTERNS.PASSWORD_COMPLEXITY),
                ],
            ],
            passwordConfirm: ['', Validators.required],
        });

        this.token = this.#route.snapshot.queryParamMap.get('token');
        const subdomain = this.#resolveSubdomain();
        if (!subdomain) {
            this.validatingTokenSig.set(false);
            this.alertSig.set({
                type: 'error',
                message: this.#translocoService.translate('auth.resetPassword.errors.tokenVerifyFailed'),
            });
            this.showAlertSig.set(true);
            return;
        }
        if (!this.token) {
            this.validatingTokenSig.set(false);
            this.alertSig.set({
                type: 'error',
                message: this.#translocoService.translate('auth.resetPassword.errors.tokenMissing'),
            });
            this.showAlertSig.set(true);
            return;
        }

        this.#authService
            .verifyResetToken(this.token, subdomain)
            .pipe(
                take(1),
                finalize(() => this.validatingTokenSig.set(false))
            )
            .subscribe({
                next: (response) => {
                    if (response.result === 'SUCCESS' && response.data?.valid) {
                        this.tokenValidSig.set(true);
                    } else {
                        this.alertSig.set({
                            type: 'error',
                            message: this.#translocoService.translate('auth.resetPassword.errors.tokenInvalid'),
                        });
                        this.showAlertSig.set(true);
                    }
                },
                error: () => {
                    this.alertSig.set({
                        type: 'error',
                        message: this.#translocoService.translate('auth.resetPassword.errors.tokenVerifyFailed'),
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

        return await this.#recaptchaService.execute('reset_password', siteKey);
    }

    async resetPassword(): Promise<void> {
        this.resetPasswordForm.markAllAsTouched();
        if (this.resetPasswordForm.invalid || !this.token) {
            return;
        }

        const password = this.resetPasswordForm.get('password')?.value;
        const passwordConfirm = this.resetPasswordForm.get('passwordConfirm')?.value;

        if (password !== passwordConfirm) {
            this.alertSig.set({
                type: 'error',
                message: this.#translocoService.translate('auth.resetPassword.errors.passwordsMustMatch'),
            });
            this.showAlertSig.set(true);
            return;
        }

        this.resetPasswordForm.disable();
        this.showAlertSig.set(false);

        let recaptchaToken: string | undefined;
        try {
            recaptchaToken = await this.#getRecaptchaToken();
        } catch {
            this.resetPasswordForm.enable();
            this.showAlertSig.set(true);
            this.alertSig.set({
                type: 'error',
                message: this.#translocoService.translate('auth.resetPassword.errors.resetFailed'),
            });
            return;
        }

        const subdomain = this.#tenantContext.subdomain();
        if (!subdomain) {
            this.showAlertSig.set(true);
            this.alertSig.set({
                type: 'error',
                message: this.#translocoService.translate('auth.resetPassword.errors.tokenVerifyFailed'),
            });
            return;
        }

        this.#authService
            .resetPassword(this.token, password, passwordConfirm, subdomain, recaptchaToken)
            .pipe(
                take(1),
                finalize(() => {
                    this.resetPasswordForm.enable();
                    this.showAlertSig.set(true);
                })
            )
            .subscribe({
                next: (response) => {
                    this.alertSig.set({
                        type: 'success',
                        message: response?.message
                            || this.#translocoService.translate('auth.resetPassword.success'),
                    });
                    this.resetPasswordNgForm.resetForm();

                    const lang = this.#translocoService.getActiveLang();
                    setTimeout(() => {
                        this.#router.navigate([`/${lang}/sign-in`]);
                    }, 3000);
                },
                error: (error) => {
                    this.alertSig.set({
                        type: 'error',
                        message: error?.error?.message
                            || this.#translocoService.translate('auth.resetPassword.errors.resetFailed'),
                    });
                },
            });
    }

    ngOnDestroy(): void {
        this.#destroySubject.next();
        this.#destroySubject.complete();
    }
}
