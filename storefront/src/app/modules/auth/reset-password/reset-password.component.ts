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
import { RecaptchaService } from 'app/core/recaptcha/recaptcha.service';
import { SiteService } from 'app/modules/admin/custom/site/site.service';
import { finalize, firstValueFrom, Subject, take } from 'rxjs';

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
    #formBuilder = inject(UntypedFormBuilder);
    #route = inject(ActivatedRoute);
    #router = inject(Router);
    #tenantContext = inject(TenantContextService);
    #translocoService = inject(TranslocoService);
    #recaptchaService = inject(RecaptchaService);
    #siteService = inject(SiteService);
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
        this.token = this.#route.snapshot.queryParamMap.get('token');
        const subdomain = this.#route.snapshot.queryParamMap.get('subdomain');

        if (subdomain) {
            this.#tenantContext.setSubdomain(subdomain);
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
            .verifyResetToken(this.token)
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

        this.resetPasswordForm = this.#formBuilder.group({
            password: ['', [Validators.required, Validators.minLength(8)]],
            passwordConfirm: ['', Validators.required],
        });
    }

    async resetPassword(): Promise<void> {
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
            const security = await firstValueFrom(
                this.#siteService.getSecuritySettings()
            );

            if (security.recaptcha?.enabled && security.recaptcha.siteKey) {
                recaptchaToken = await this.#recaptchaService.execute(
                    'reset_password',
                    security.recaptcha.siteKey
                );
            }
        } catch (error) {
            console.error('reCAPTCHA error:', error);
        }

        this.#authService
            .resetPassword(this.token, password, passwordConfirm, recaptchaToken)
            .pipe(
                take(1),
                finalize(() => {
                    this.resetPasswordForm.enable();
                    this.showAlertSig.set(true);
                })
            )
            .subscribe({
                next: () => {
                    this.alertSig.set({
                        type: 'success',
                        message: this.#translocoService.translate('auth.resetPassword.success'),
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
