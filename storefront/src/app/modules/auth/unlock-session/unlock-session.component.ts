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
import { fuseAnimations } from '@fuse/animations';
import { FuseAlertComponent, FuseAlertType } from '@fuse/components/alert';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { AuthService } from 'app/core/auth/auth.service';
import { UserService } from 'app/core/user/user.service';
import { Subject, take } from 'rxjs';

@Component({
    selector: 'spa-unlock-session',
    standalone: true,
    templateUrl: './unlock-session.component.html',
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
export class AuthUnlockSessionComponent implements OnInit, OnDestroy {
    @ViewChild('unlockSessionNgForm') unlockSessionNgForm: NgForm;

    #activatedRoute = inject(ActivatedRoute);
    #authService = inject(AuthService);
    #formBuilder = inject(UntypedFormBuilder);
    #router = inject(Router);
    #userService = inject(UserService);
    #translocoService = inject(TranslocoService);
    #destroy$ = new Subject<void>();

    unlockSessionForm: UntypedFormGroup;
    #email: string;

    protected nameSig = signal('');
    protected alertSig = signal<{ type: FuseAlertType; message: string }>({
        type: 'success',
        message: '',
    });
    protected showAlertSig = signal(false);

    ngOnInit(): void {
        this.#userService.user$
            .pipe(take(1))
            .subscribe((user) => {
                this.nameSig.set(user.name);
                this.#email = user.email;
                if (!this.unlockSessionForm) {
                    this.unlockSessionForm = this.#formBuilder.group({
                        name: [{ value: this.nameSig(), disabled: true }],
                        password: ['', Validators.required],
                    });
                } else {
                    this.unlockSessionForm.patchValue({
                        name: this.nameSig(),
                    });
                }
            });
    }

    unlock(): void {
        if (this.unlockSessionForm.invalid) {
            return;
        }

        this.unlockSessionForm.disable();
        this.showAlertSig.set(false);

        this.#authService
            .unlockSession({
                email: this.#email ?? '',
                password: this.unlockSessionForm.get('password').value,
            })
            .pipe(take(1))
            .subscribe({
                next: () => {
                    const redirectURL =
                        this.#activatedRoute.snapshot.queryParamMap.get('redirectURL') || '/signed-in-redirect';
                    this.#router.navigateByUrl(redirectURL);
                },
                error: () => {
                    this.unlockSessionForm.enable();
                    this.unlockSessionNgForm.resetForm({
                        name: { value: this.nameSig(), disabled: true },
                    });
                    this.alertSig.set({
                        type: 'error',
                        message: this.#translocoService.translate('auth.unlockSession.errors.invalidPassword'),
                    });
                    this.showAlertSig.set(true);
                }
            });
    }

    ngOnDestroy(): void {
        this.#destroy$.next();
        this.#destroy$.complete();
    }
}
