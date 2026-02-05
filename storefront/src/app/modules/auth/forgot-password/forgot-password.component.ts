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
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RouterLink } from '@angular/router';
import { TenantContextService } from '@core/tenant';
import { fuseAnimations } from '@fuse/animations';
import { FuseAlertComponent, FuseAlertType } from '@fuse/components/alert';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { AuthService } from 'app/core/auth/auth.service';
import { finalize, Subject, take, takeUntil } from 'rxjs';

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
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatProgressSpinnerModule,
        RouterLink,
        TranslocoModule,
    ],
})
export class AuthForgotPasswordComponent implements OnInit, OnDestroy {
    @ViewChild('forgotPasswordNgForm') forgotPasswordNgForm: NgForm;

    #authService = inject(AuthService);
    #formBuilder = inject(UntypedFormBuilder);
    #tenantContext = inject(TenantContextService);
    #translocoService = inject(TranslocoService);
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

    sendResetLink(): void {
        if (this.forgotPasswordForm.invalid) {
            return;
        }

        this.forgotPasswordForm.disable();
        this.showAlertSig.set(false);

        const email = this.forgotPasswordForm.get('email').value;
        const subdomain = this.#tenantContext.subdomain();

        this.#authService
            .forgotPassword(email, subdomain)
            .pipe(
                takeUntil(this.#destroySubject),
                finalize(() => {
                    this.forgotPasswordForm.enable();
                    this.forgotPasswordNgForm.resetForm();
                    this.showAlertSig.set(true);
                })
            )
            .subscribe({
                next: () => {
                    this.alertSig.set({
                        type: 'success',
                        message: this.#translocoService.translate('auth.forgotPassword.alerts.success'),
                    });
                },
                error: () => {
                    this.alertSig.set({
                        type: 'error',
                        message: this.#translocoService.translate('auth.forgotPassword.alerts.error'),
                    });
                }
            });
    }

    ngOnDestroy(): void {
        this.#destroySubject.next();
        this.#destroySubject.complete();
    }
}
