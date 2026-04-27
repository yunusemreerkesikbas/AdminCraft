import {
    ChangeDetectionStrategy,
    Component,
    OnInit,
    ViewChild,
    ViewEncapsulation,
    inject,
    signal,
} from '@angular/core';
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
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TenantContextService } from '@core/tenant';
import { fuseAnimations } from '@fuse/animations';
import { FuseAlertComponent, FuseAlertType } from '@fuse/components/alert';
import { TranslocoModule } from '@jsverse/transloco';
import { AuthService } from 'app/core/auth/auth.service';
import { ConfigFlagsService } from 'app/core/config/config-flags.service';
import { RecaptchaService } from 'app/core/recaptcha/recaptcha.service';
import { SpaInputComponent } from 'app/shared/components/custom-ui/spa-input/spa-input.component';
import { finalize, take } from 'rxjs';

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
export class AuthForgotPasswordComponent implements OnInit {
    @ViewChild('forgotPasswordNgForm') forgotPasswordNgForm: NgForm;

    #authService = inject(AuthService);
    #configFlags = inject(ConfigFlagsService);
    #formBuilder = inject(UntypedFormBuilder);
    #tenantContext = inject(TenantContextService);
    #recaptchaService = inject(RecaptchaService);
    #activatedRoute = inject(ActivatedRoute);

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

        try {
            return await this.#recaptchaService.execute('forgot_password', siteKey);
        } catch {
            return undefined;
        }
    }

    #resolveSubdomain(): string | null {
        const hostSubdomain = this.#resolveHostnameSubdomain();
        if (hostSubdomain && hostSubdomain !== 'admin') {
            this.#tenantContext.setSubdomain(hostSubdomain);
            return hostSubdomain;
        }

        const routeSubdomain =
            this.#tenantContext.normalizeSubdomain(
                this.#activatedRoute.snapshot.queryParamMap.get('subdomain')
            );
        if (
            routeSubdomain &&
            routeSubdomain !== 'admin' &&
            this.#tenantContext.isValidSubdomain(routeSubdomain)
        ) {
            this.#tenantContext.setSubdomain(routeSubdomain);
            return routeSubdomain;
        }

        const currentSubdomain = this.#tenantContext.getCurrentSubdomain();
        if (currentSubdomain && currentSubdomain !== 'admin') {
            return currentSubdomain;
        }

        return null;
    }

    #resolveHostnameSubdomain(): string | null {
        const hostname = window.location.hostname;
        if (hostname === 'localhost') {
            return null;
        }

        const subdomain = this.#tenantContext.normalizeSubdomain(
            hostname.split('.')[0]
        );
        if (!subdomain) {
            return null;
        }
        if (subdomain === 'admin' || subdomain === 'app' || subdomain === 's1-app') {
            return null;
        }

        return this.#tenantContext.isValidSubdomain(subdomain) ? subdomain : null;
    }

    async sendResetLink(): Promise<void> {
        if (this.forgotPasswordForm.invalid) {
            return;
        }

        this.forgotPasswordForm.disable();
        this.showAlertSig.set(false);

        const email = this.forgotPasswordForm.get('email').value;
        const subdomain = this.#resolveSubdomain();
        const recaptchaToken = await this.#getRecaptchaToken();

        this.#authService
            .forgotPassword(email, subdomain ?? undefined, recaptchaToken)
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
                        message: response?.message ?? '',
                    });
                },
                error: (error) => {
                    this.alertSig.set({
                        type: 'error',
                        message: error?.error?.message ?? '',
                    });
                }
            });
    }
}
