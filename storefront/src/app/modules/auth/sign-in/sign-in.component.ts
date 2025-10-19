import { Component, OnInit, ViewChild, ViewEncapsulation } from '@angular/core';
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
import { FuseAlertComponent, FuseAlertType } from '@fuse/components/alert';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { AuthService } from 'app/core/auth/auth.service';
import { TenantContextService } from 'app/core/tenant/tenant-context.service';
import { UserService } from 'app/core/user/user.service';
import { SpaInputComponent } from 'app/shared/components/custom-ui/spa-input/spa-input.component';

@Component({
    selector: 'auth-sign-in',
    templateUrl: './sign-in.component.html',
    encapsulation: ViewEncapsulation.None,
    animations: fuseAnimations,
    imports: [
        RouterLink,
        FuseAlertComponent,
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
export class AuthSignInComponent implements OnInit {
    @ViewChild('signInNgForm') signInNgForm: NgForm;

    alert: { type: FuseAlertType; message: string } = {
        type: 'success',
        message: '',
    };
    signInForm: UntypedFormGroup;
    showAlert: boolean = false;
    formSubmitted: boolean = false;

    constructor(
        private _activatedRoute: ActivatedRoute,
        private _authService: AuthService,
        private _formBuilder: UntypedFormBuilder,
        private _router: Router,
        private _tenantContext: TenantContextService,
        private _userService: UserService,
        private _translocoService: TranslocoService
    ) {}

    ngOnInit(): void {
        this.signInForm = this._formBuilder.group({
            email: [
                '',
                [Validators.required, Validators.email],
            ],
            password: ['', Validators.required],
            rememberMe: [''],
        });
    }

    signIn(): void {
        this.formSubmitted = true;
        this.signInForm.markAllAsTouched();
        if (this.signInForm.invalid) {
            return;
        }
        this.signInForm.disable();

        this.showAlert = false;
        this._authService.signIn(this.signInForm.value).subscribe(
            () => {
                const lang = this._translocoService.getActiveLang();
                const user = this._userService.user;
                if (user?.role === 'SUPER_ADMIN') {
                    this._router.navigateByUrl(`/${lang}/dashboards/project`);
                    return;
                }
                const returnUrl = this._activatedRoute.snapshot
                    .queryParamMap.get('redirectURL');
                const safe = (url: string | null) => {
                    if (!url) { return null; }
                    try {
                        const u = new URL(url, window.location.origin);
                        if (u.origin !== window.location.origin) {
                            return null;
                        }
                        return u.pathname + u.search + u.hash;
                    } catch { return null; }
                };
                const safeUrl = safe(returnUrl);
                const fallback = `/${lang}/project`;
                this._router.navigateByUrl(safeUrl || fallback);
            },
            (response) => {
                this.signInForm.enable();
                this.signInNgForm.resetForm();
                this.alert = {
                    type: 'error',
                    message: typeof response === 'string' ? response : 'Wrong email or password',
                };
                this.showAlert = true;
            }
        );
    }
}
