import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import {
    FormBuilder,
    FormGroup,
    ReactiveFormsModule,
    Validators,
} from '@angular/forms';
import {
    MAT_DIALOG_DATA,
    MatDialogModule,
    MatDialogRef,
} from '@angular/material/dialog';
import { TranslocoPipe } from '@jsverse/transloco';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaDialogContentComponent } from '@shared/components/spa-dialog';
import { SpaDialogFooterComponent } from '@shared/components/spa-dialog/spa-dialog-footer/spa-dialog-footer.component';
import { SpaDialogHeaderComponent } from '@shared/components/spa-dialog/spa-dialog-header/spa-dialog-header.component';
import { VALIDATION_LIMITS } from '@shared/constants/validation.constants';
import { User } from '../../users.types';

export interface UserPasswordDialogData {
    user: User;
    mode: 'change' | 'reset';
}

export interface PasswordDialogResult {
    mode: 'change' | 'reset';
    changePasswordData?: {
        currentPassword: string;
        newPassword: string;
        confirmPassword: string;
    };
}

@Component({
    selector: 'app-user-password-dialog',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatDialogModule,
        TranslocoPipe,
        SpaInputComponent,
        SpaDialogHeaderComponent,
        SpaDialogFooterComponent,
        SpaDialogContentComponent,
    ],
    templateUrl: './user-password-dialog.component.html',
})
export class UserPasswordDialogComponent implements OnInit {
    #fb = inject(FormBuilder);
    #dialogRef = inject(MatDialogRef<UserPasswordDialogComponent>);
    protected data = inject<UserPasswordDialogData>(MAT_DIALOG_DATA);

    form!: FormGroup;
    mode: 'change' | 'reset' = this.data.mode;
    user: User = this.data.user;

    ngOnInit(): void {
        if (this.mode === 'change') {
            this.form = this.#fb.group(
                {
                    currentPassword: ['', [Validators.required]],
                    newPassword: [
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
            this.form = this.#fb.group({});
        }
    }

    #passwordMatchValidator(g: FormGroup) {
        const newPassword = g.get('newPassword')?.value;
        const confirmPassword = g.get('confirmPassword')?.value;
        return newPassword === confirmPassword ? null : { mismatch: true };
    }

    onSubmit(): void {
        if (this.mode === 'reset') {
            const result: PasswordDialogResult = { mode: 'reset' };
            this.#dialogRef.close(result);
        } else if (this.form.valid) {
            const formValue = this.form.value;
            const result: PasswordDialogResult = {
                mode: 'change',
                changePasswordData: formValue,
            };
            this.#dialogRef.close(result);
        }
    }

    onCancel(): void {
        this.#dialogRef.close();
    }
}
