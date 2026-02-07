import { ChangeDetectionStrategy, Component, OnDestroy, OnInit, ViewChild, ViewEncapsulation, inject, signal } from '@angular/core';
import {
    FormsModule,
    NgForm,
    ReactiveFormsModule,
    UntypedFormBuilder,
    UntypedFormGroup,
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
import { RecaptchaService } from 'app/core/recaptcha/recaptcha.service';
import { PublicTenantConfigService } from 'app/core/config/public-tenant-config.service';
import { RecaptchaConfig } from 'app/core/config/public-tenant-config.types';
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
    #deviceFingerprintService = inject(DeviceFingerprintService);
    #formBuilder = inject(UntypedFormBuilder);
    #route = inject(ActivatedRoute);
    #router = inject(Router);
    #tenantContext = inject(TenantContextService);
    #translocoService = inject(TranslocoService);
    #recaptchaService = inject(RecaptchaService);
    #publicConfigService = inject(PublicTenantConfigService);
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
                message: this.#translocoService.translate('auth.setPassword.errors.tokenMissing'),
            });
            this.showAlertSig.set(true);
            return;
        }

        this.#authService
            .verifyEmailToken(this.token, subdomain || undefined)
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

        this.setPasswordForm = this.#formBuilder.group({
            password: [''],
            passwordConfirm: [''],
            trustDevice: [true],
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

        return await this.#recaptchaService.execute('set_password', config.siteKey);
    }

    async setPassword(): Promise<void> {
        if (!this.token) {
            return;
        }

        const password = this.setPasswordForm.get('password')?.value;
        const passwordConfirm = this.setPasswordForm.get('passwordConfirm')?.value;
        const trustDevice = this.setPasswordForm.get('trustDevice')?.value || false;
        const subdomain = this.#route.snapshot.queryParamMap.get('subdomain');

        this.setPasswordForm.disable();
        this.showAlertSig.set(false);

        const recaptchaToken = await this.#getRecaptchaToken();
        const deviceFingerprint = await this.#deviceFingerprintService.getDeviceFingerprint();
        const deviceName = this.#deviceFingerprintService.getDeviceName();

        this.#authService
            .setInitialPassword(
                this.token,
                password,
                passwordConfirm,
                deviceFingerprint,
                trustDevice,
                deviceName,
                subdomain || undefined,
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
                    if (response.result === 'SUCCESS' && response.data) {
                        this.#authService.completeSignInWithResponse(response.data);

                        this.alertSig.set({
                            type: 'success',
                            message: this.#translocoService.translate('auth.setPassword.successAutoLogin'),
                        });
                        this.showAlertSig.set(true);

                        const lang = this.#translocoService.getActiveLang();
                        setTimeout(() => {
                            this.#router.navigate([`/${lang}/site`]);
                        }, 1500);
                    } else {
                        this.alertSig.set({
                            type: 'error',
                            message: this.#translocoService.translate('auth.setPassword.errors.setFailed'),
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
