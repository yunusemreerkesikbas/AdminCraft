import { SpaDialogContentComponent } from '@/app/shared/components/spa-dialog';
import { CommonModule } from '@angular/common';
import {
    Component,
    OnInit,
    ViewEncapsulation,
    inject,
    signal,
} from '@angular/core';
import {
    FormBuilder,
    FormGroup,
    ReactiveFormsModule,
    Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import {
    MAT_DIALOG_DATA,
    MatDialogModule,
    MatDialogRef,
} from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaCheckboxComponent } from '@shared/components/custom-ui/spa-checkbox/spa-checkbox.component';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaSelectComponent } from '@shared/components/custom-ui/spa-select/spa-select.component';
import { SpaTextareaComponent } from '@shared/components/custom-ui/spa-textarea/spa-textarea.component';
import { SpaDialogFooterComponent } from '@shared/components/spa-dialog/spa-dialog-footer/spa-dialog-footer.component';
import { SpaDialogHeaderComponent } from '@shared/components/spa-dialog/spa-dialog-header/spa-dialog-header.component';
import { VALIDATION_LIMITS } from '@shared/constants/validation.constants';
import { NotificationService } from '@shared/notifications/notification.service';
import { take } from 'rxjs';
import { UsersService } from '../../users.service';
import {
    CreateUserRequest,
    UpdateUserRequest,
    User,
    UserRole,
} from '../../users.types';

export interface UserFormDialogData {
    user?: User;
    mode: 'create' | 'edit';
}

@Component({
    selector: 'app-user-form-dialog',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatDialogModule,
        MatButtonModule,
        MatIconModule,
        TranslocoModule,
        SpaInputComponent,
        SpaSelectComponent,
        SpaCheckboxComponent,
        SpaTextareaComponent,
        SpaDialogHeaderComponent,
        SpaDialogFooterComponent,
        SpaDialogContentComponent,
    ],
    templateUrl: './user-form-dialog.component.html',
    encapsulation: ViewEncapsulation.None,
})
export class UserFormDialogComponent implements OnInit {
    #fb = inject(FormBuilder);
    #dialogRef = inject(MatDialogRef<UserFormDialogComponent>);
    protected data = inject<UserFormDialogData>(MAT_DIALOG_DATA);
    #usersService = inject(UsersService);
    #notificationService = inject(NotificationService);

    form!: FormGroup;
    mode: 'create' | 'edit' = this.data.mode;
    isLoadingSig = signal(false);
    passwordVisible = signal(false);
    confirmPasswordVisible = signal(false);

    roleOptions = Object.values(UserRole)
        .filter((role) => role !== UserRole.SUPER_ADMIN)
        .map((role) => ({
            value: role,
            labelKey: 'admin.common.status.' + role.toLowerCase(),
        }));

    ngOnInit(): void {
        this.#buildForm();
        if (this.mode === 'edit' && this.data.user) {
            this.form.patchValue(this.data.user);
        }
    }

    get isEditMode(): boolean {
        return this.mode === 'edit';
    }

    #buildForm(): void {
        const baseControls = {
            email: ['', [Validators.required, Validators.email]],
            firstName: [
                '',
                [Validators.maxLength(VALIDATION_LIMITS.USER_FIRST_NAME_MAX)],
            ],
            lastName: [
                '',
                [Validators.maxLength(VALIDATION_LIMITS.USER_LAST_NAME_MAX)],
            ],
            role: [UserRole.VIEWER, [Validators.required]],
            phone: [
                '',
                [Validators.maxLength(VALIDATION_LIMITS.USER_PHONE_MAX)],
            ],
            jobTitle: [
                '',
                [Validators.maxLength(VALIDATION_LIMITS.USER_JOB_TITLE_MAX)],
            ],
            department: [
                '',
                [Validators.maxLength(VALIDATION_LIMITS.USER_DEPARTMENT_MAX)],
            ],
            isActive: [true],
            notes: [
                '',
                [Validators.maxLength(VALIDATION_LIMITS.USER_NOTES_MAX)],
            ],
        };

        if (this.mode === 'create') {
            this.form = this.#fb.group(
                {
                    ...baseControls,
                    password: [
                        '',
                        [
                            Validators.required,
                            Validators.minLength(
                                VALIDATION_LIMITS.USER_PASSWORD_MIN
                            ),
                            Validators.maxLength(
                                VALIDATION_LIMITS.USER_PASSWORD_MAX
                            ),
                        ],
                    ],
                    confirmPassword: ['', [Validators.required]],
                },
                { validators: this.#passwordMatchValidator }
            );
        } else {
            this.form = this.#fb.group({
                ...baseControls,
            });
        }
    }

    #passwordMatchValidator(g: FormGroup) {
        const password = g.get('password')?.value;
        const confirmPassword = g.get('confirmPassword')?.value;
        return password === confirmPassword ? null : { mismatch: true };
    }

    onSubmit(): void {
        if (this.form.valid) {
            this.isLoadingSig.set(true);

            if (this.isEditMode) {
                const request: UpdateUserRequest = {
                    ...this.form.value,
                    role: this.form.value.role,
                };

                this.#usersService
                    .updateWithResponse(this.data.user!.id, request)
                    .pipe(take(1))
                    .subscribe({
                        next: (response) => {
                            this.isLoadingSig.set(false);
                            this.#notificationService.success(
                                response.message!
                            );
                            this.#dialogRef.close(true);
                        },
                        error: (error) => {
                            this.isLoadingSig.set(false);
                            this.#notificationService.alert(
                                error.error.message
                            );
                        },
                    });
            } else {
                const { confirmPassword, ...formValue } = this.form.value;
                const request: CreateUserRequest = {
                    ...formValue,
                    role: this.form.value.role,
                };

                this.#usersService
                    .createWithResponse(request)
                    .pipe(take(1))
                    .subscribe({
                        next: (response) => {
                            this.isLoadingSig.set(false);
                            this.#notificationService.success(
                                response.message!
                            );
                            this.#dialogRef.close(true);
                        },
                        error: (error) => {
                            this.isLoadingSig.set(false);
                            this.#notificationService.alert(
                                error.error.message
                            );
                        },
                    });
            }
        }
    }

    onCancel(): void {
        this.#dialogRef.close();
    }
}
