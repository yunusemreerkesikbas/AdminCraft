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
import { PublicTenantConfigService } from 'app/core/config/public-tenant-config.service';
import { RecaptchaConfig } from 'app/core/config/public-tenant-config.types';
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
    #formBuilder = inject(UntypedFormBuilder);
    #route = inject(ActivatedRoute);
    #router = inject(Router);
    #tenantContext = inject(TenantContextService);
    #translocoService = inject(TranslocoService);
    #recaptchaService = inject(RecaptchaService);
    #publicConfigService = inject(PublicTenantConfigService);
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
    protected recaptchaConfigSig = signal<RecaptchaConfig | null>(null);

    ngOnInit(): void {
        this.#loadPublicConfig();
        
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

    #loadPublicConfig(): void {
        const subdomain = this.#tenantContext.extractSubdomainFromHost();
        if (!subdomain) return;

        this.#publicConfigService
            .loadConfig(subdomain)
            .pipe(take(1))
            .subscribe(config => this.recaptchaConfigSig.set(config.recaptcha));
    }

    async #getRecaptchaToken(): Promise<string | undefined> {
        const config = this.recaptchaConfigSig();
        if (!config?.enabled || !config.siteKey) return undefined;

        return await this.#recaptchaService.execute('reset_password', config.siteKey);
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

        const recaptchaToken = await this.#getRecaptchaToken();

        this.#authService
            .resetPassword(this.token, password, passwordConfirm, undefined, recaptchaToken)
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
