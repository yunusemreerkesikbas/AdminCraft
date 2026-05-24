import {
    ChangeDetectionStrategy,
    Component,
    OnDestroy,
    OnInit,
    ViewChild,
    ViewEncapsulation,
    computed,
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
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { fuseAnimations } from '@fuse/animations';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { SUPER_ADMIN_ROLE } from '@shared/constants';
import { AuthService } from 'app/core/auth/auth.service';
import { DeviceFingerprintService } from 'app/core/auth/device-fingerprint.service';
import { ConfigFlagsService } from 'app/core/config/config-flags.service';
import { RecaptchaService } from 'app/core/recaptcha/recaptcha.service';
import { TenantContextService } from 'app/core/tenant/tenant-context.service';
import { UserService } from 'app/core/user/user.service';
import { SpaInputComponent } from 'app/shared/components/custom-ui/spa-input/spa-input.component';
import { Subject, takeUntil } from 'rxjs';

@Component({
    selector: 'spa-sign-in',
    standalone: true,
    templateUrl: './sign-in.component.html',
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    animations: fuseAnimations,
    imports: [
        RouterLink,
        FormsModule,
        ReactiveFormsModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatIconModule,
        MatCheckboxModule,
        MatProgressSpinnerModule,
        TranslocoModule,
        SpaInputComponent,
    ],
})
export class AuthSignInComponent implements OnInit, OnDestroy {
    @ViewChild('signInNgForm') signInNgForm: NgForm;

    #activatedRoute = inject(ActivatedRoute);
    #authService = inject(AuthService);
    #configFlags = inject(ConfigFlagsService);
    #deviceFingerprintService = inject(DeviceFingerprintService);
    #recaptchaService = inject(RecaptchaService);
    #tenantContext = inject(TenantContextService);
    #formBuilder = inject(UntypedFormBuilder);
    #router = inject(Router);
    #userService = inject(UserService);
    #translocoService = inject(TranslocoService);
    #destroySubject = new Subject<void>();

    protected isPlatformHostSig = signal(false);

    signInForm: UntypedFormGroup;
    otpForm: UntypedFormGroup;

    protected formSubmittedSig = signal(false);
    protected otpSubmittedSig = signal(false);

    readonly requires2FASig = this.#authService.requires2FASig;
    readonly twoFactorPendingSig = this.#authService.twoFactorPendingSig;
    readonly otpResendCooldownSig = this.#authService.otpResendCooldownSig;
    readonly showOtpFormSig = computed(() => this.requires2FASig());
    readonly canResendOtpSig = computed(
        () => this.otpResendCooldownSig() <= 0
    );

    #lastLoginCredentials: {
        email: string;
        password: string;
        workspace?: string;
        rememberMe?: boolean;
        deviceFingerprint?: string;
        recaptchaToken?: string;
    } | null = null;

    ngOnInit(): void {
        const routeSubdomain = this.#tenantContext.normalizeSubdomain(
            this.#activatedRoute.snapshot.queryParamMap.get('subdomain')
        );
        if (routeSubdomain) {
            this.#tenantContext.setSubdomain(routeSubdomain);
            this.#router.navigate([], {
                relativeTo: this.#activatedRoute,
                queryParams: { subdomain: null },
                queryParamsHandling: 'merge',
                replaceUrl: true,
            });
        }
        const subdomain =
            routeSubdomain ?? this.#tenantContext.getCurrentSubdomain();
        const tenantSubdomain =
            subdomain && subdomain !== 'admin' ? subdomain : '';
        this.isPlatformHostSig.set(!tenantSubdomain);

        this.signInForm = this.#formBuilder.group({
            workspace: [tenantSubdomain],
            email: ['', [Validators.required, Validators.email]],
            password: ['', Validators.required],
            rememberMe: [false],
        });

        this.signInForm.get('workspace')!.valueChanges
            .pipe(takeUntil(this.#destroySubject))
            .subscribe((val: string) => {
                this.isPlatformHostSig.set(!(val?.trim()));
            });

        this.otpForm = this.#formBuilder.group({
            otpCode: [
                '',
                [
                    Validators.required,
                    Validators.minLength(6),
                    Validators.maxLength(6),
                ],
            ],
            trustDevice: [false],
        });
    }

    async #getRecaptchaToken(): Promise<string | undefined> {
        const enabled = this.#configFlags.flag(
            'security.recaptcha.enabled',
            false
        );
        const siteKey = this.#configFlags.flag(
            'security.recaptcha.site_key',
            ''
        );
        if (!enabled || !siteKey) return undefined;

        return await this.#recaptchaService.execute('login', siteKey);
    }

    async signIn(): Promise<void> {
        this.formSubmittedSig.set(true);
        this.signInForm.markAllAsTouched();
        if (this.signInForm.invalid) {
            return;
        }
        this.signInForm.disable();

        const workspace =
            this.#tenantContext.normalizeSubdomain(
                this.signInForm.get('workspace')?.value
            ) ?? '';
        if (workspace) {
            this.#tenantContext.setSubdomain(workspace);
        }

        try {
            const credentials = {
                ...this.signInForm.value,
                workspace,
                deviceFingerprint:
                    await this.#deviceFingerprintService.getDeviceFingerprint(),
                recaptchaToken: await this.#getRecaptchaToken(),
                rememberMe: this.signInForm.get('rememberMe')?.value ?? false,
            };
            this.#lastLoginCredentials = {
                email: credentials.email,
                password: credentials.password,
                workspace: credentials.workspace,
                rememberMe: credentials.rememberMe,
            };

            this.#authService
                .signIn(credentials)
                .pipe(takeUntil(this.#destroySubject))
                .subscribe({
                    next: (result) => {
                        if (result === 'requires2FA') {
                            this.formSubmittedSig.set(false);
                        } else if (result === true) {
                            this.#navigateAfterLogin();
                        } else {
                            this.signInForm.enable();
                            this.formSubmittedSig.set(false);
                        }
                    },
                    error: () => {
                        this.signInForm.enable();
                        this.formSubmittedSig.set(false);
                    },
                });
        } catch (error) {
            this.signInForm.enable();
            this.formSubmittedSig.set(false);
        }
    }

    async verifyOtp(): Promise<void> {
        this.otpSubmittedSig.set(true);
        this.otpForm.markAllAsTouched();
        if (this.otpForm.invalid) {
            return;
        }
        this.otpForm.disable();

        const pending = this.twoFactorPendingSig();
        if (!pending) {
            this.otpForm.enable();
            this.otpSubmittedSig.set(false);
            return;
        }

        const trustDevice = this.isPlatformHostSig()
            ? false
            : this.otpForm.get('trustDevice')?.value || false;

        const request = {
            pendingToken: pending.pendingToken,
            otpCode: this.otpForm.get('otpCode')?.value,
            trustDevice,
            deviceFingerprint:
                await this.#deviceFingerprintService.getDeviceFingerprint(),
            deviceName: this.isPlatformHostSig()
                ? undefined
                : this.#deviceFingerprintService.getDeviceName(),
            tenantId: pending.tenantId,
            subdomain: pending.subdomain,
        };

        this.#authService
            .verifyOtp(request)
            .pipe(takeUntil(this.#destroySubject))
            .subscribe({
                next: (success) => {
                    if (success) {
                        this.#navigateAfterLogin();
                    } else {
                        this.otpForm.enable();
                        this.otpSubmittedSig.set(false);
                    }
                },
                error: () => {
                    this.otpForm.enable();
                    this.otpSubmittedSig.set(false);
                },
            });
    }

    async resendOtp(): Promise<void> {
        if (!this.canResendOtpSig() || !this.#lastLoginCredentials) {
            return;
        }
        this.otpForm.disable();
        try {
            const pending = this.#lastLoginCredentials;
            const credentials = {
                email: pending.email,
                password: pending.password,
                workspace: pending.workspace,
                rememberMe: pending.rememberMe,
                recaptchaToken: await this.#getRecaptchaToken(),
                deviceFingerprint:
                    await this.#deviceFingerprintService.getDeviceFingerprint(),
            };
            this.#authService
                .signIn(credentials)
                .pipe(takeUntil(this.#destroySubject))
                .subscribe({
                    next: (result) => {
                        this.otpForm.enable();
                        if (result === 'requires2FA') {
                            this.otpForm.reset();
                            this.otpSubmittedSig.set(false);
                        }
                    },
                    error: () => {
                        this.otpForm.enable();
                    },
                });
        } catch {
            this.otpForm.enable();
        }
    }

    cancelOtp(): void {
        this.#authService.cancel2FA();
        this.#lastLoginCredentials = null;
        this.signInForm.enable();
        this.signInForm.reset();
        this.otpForm.reset();
        this.formSubmittedSig.set(false);
        this.otpSubmittedSig.set(false);
    }

    #navigateAfterLogin(): void {
        const lang = this.#translocoService.getActiveLang();
        const user = this.#userService.user();

        if (user?.role === SUPER_ADMIN_ROLE) {
            this.#router.navigateByUrl(`/${lang}/tenants`);
            return;
        }

        const returnUrl =
            this.#activatedRoute.snapshot.queryParamMap.get('redirectURL');
        const safeUrl = this.#getSafeUrl(returnUrl);
        const fallback = `/${lang}/site`;

        this.#router.navigateByUrl(safeUrl || fallback);
    }

    #getSafeUrl(url: string | null): string | null {
        if (!url) {
            return null;
        }
        try {
            const u = new URL(url, window.location.origin);
            if (u.origin !== window.location.origin) {
                return null;
            }
            return u.pathname + u.search + u.hash;
        } catch {
            return null;
        }
    }

    ngOnDestroy(): void {
        this.#destroySubject.next();
        this.#destroySubject.complete();
    }
}
