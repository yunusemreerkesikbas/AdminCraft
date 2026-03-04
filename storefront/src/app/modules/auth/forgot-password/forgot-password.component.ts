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
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RouterLink } from '@angular/router';
import { TenantContextService } from '@core/tenant';
import { fuseAnimations } from '@fuse/animations';
import { FuseAlertComponent, FuseAlertType } from '@fuse/components/alert';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { AuthService } from 'app/core/auth/auth.service';
import { ConfigFlagsService } from 'app/core/config/config-flags.service';
import { RecaptchaService } from 'app/core/recaptcha/recaptcha.service';
import { SpaInputComponent } from 'app/shared/components/custom-ui/spa-input/spa-input.component';
import { finalize, Subject, take } from 'rxjs';

@Component({
    selector: 'spa-forgot-password',
    standalone: true,
    templateUrl: './forgot-password.component.html',
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    animations: fuseAnimations,
    imports: [
        FuseAlertComponent,
        FormsModule,
        ReactiveFormsModule,
        MatButtonModule,
        MatProgressSpinnerModule,
        RouterLink,
        TranslocoModule,
        SpaInputComponent,
    ],
})
export class AuthForgotPasswordComponent implements OnInit, OnDestroy {
    @ViewChild('forgotPasswordNgForm') forgotPasswordNgForm: NgForm;

    #authService = inject(AuthService);
    #configFlags = inject(ConfigFlagsService);
    #formBuilder = inject(UntypedFormBuilder);
    #tenantContext = inject(TenantContextService);
    #translocoService = inject(TranslocoService);
    #recaptchaService = inject(RecaptchaService);
    #destroySubject = new Subject<void>();

    forgotPasswordForm: UntypedFormGroup;

    protected alertSig = signal<{ type: FuseAlertType; message: string }>({
        type: 'success',
        message: '',
    });
    protected showAlertSig = signal(false);

    ngOnInit(): void {
        this.forgotPasswordForm = this.#formBuilder.group({
            email: ['', [Validators.required, Validators.email]],
        });
    }

    async #getRecaptchaToken(): Promise<string | undefined> {
        const enabled = this.#configFlags.flag('security.recaptcha.enabled', false);
        const siteKey = this.#configFlags.flag('security.recaptcha.site_key', '');
        if (!enabled || !siteKey) return undefined;

        return await this.#recaptchaService.execute('forgot_password', siteKey);
    }

    async sendResetLink(): Promise<void> {
        if (this.forgotPasswordForm.invalid) {
            return;
        }

        this.forgotPasswordForm.disable();
        this.showAlertSig.set(false);

        try {
            const recaptchaToken = await this.#getRecaptchaToken();
            const email = this.forgotPasswordForm.get('email').value;
            const subdomain = this.#tenantContext.subdomain();

            this.#authService
                .forgotPassword(email, subdomain, recaptchaToken)
                .pipe(
                    take(1),
                    finalize(() => {
                        this.forgotPasswordForm.enable();
                        this.forgotPasswordNgForm.resetForm();
                        this.showAlertSig.set(true);
                    })
                )
                .subscribe({
                    next: (response) => {
                        this.alertSig.set({
                            type: 'success',
                            message: response?.message
                                || this.#translocoService.translate('auth.forgotPassword.alerts.success'),
                        });
                    },
                    error: (error) => {
                        this.alertSig.set({
                            type: 'error',
                            message: error?.error?.message
                                || this.#translocoService.translate('auth.forgotPassword.alerts.error'),
                        });
                    }
                });
        } catch (error) {
            this.forgotPasswordForm.enable();
            this.showAlertSig.set(true);
            this.alertSig.set({
                type: 'error',
                message: this.#translocoService.translate('auth.forgotPassword.alerts.error'),
            });
        }
    }

    ngOnDestroy(): void {
        this.#destroySubject.next();
        this.#destroySubject.complete();
    }
}
