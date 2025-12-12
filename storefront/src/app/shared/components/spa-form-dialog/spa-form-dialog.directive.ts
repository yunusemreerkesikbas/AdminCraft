import { computed, Directive, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { NotificationService } from '@shared/notifications/notification.service';
import { Observable, take, takeUntil } from 'rxjs';
import { SpaDialogBase } from '../spa-dialog-base';
import { DialogMode, SpaFormDialogData, SubmitOptions } from '../spa-dialog-base/spa-dialog-base.types';

@Directive()
export abstract class SpaFormDialog<
    TResult = any,
    TData extends SpaFormDialogData = SpaFormDialogData
> extends SpaDialogBase<TResult, TData> implements OnInit {

    protected readonly fb = inject(FormBuilder);
    protected readonly notify = inject(NotificationService);

    protected abstract form: FormGroup;

    readonly mode = computed<DialogMode>(() => {
        return this.data?.mode || (this.data?.id ? 'edit' : 'create');
    });

    readonly isEditMode = computed(() => this.mode() === 'edit');
    readonly isCreateMode = computed(() => this.mode() === 'create');

    ngOnInit(): void {
        this.initializeForm();
    }

    protected initializeForm(): void {
        if (this.data?.initial && this.form) {
            this.form.patchValue(this.data.initial);
        }
    }

    protected submit<T>(
        operation$: Observable<T>,
        options: SubmitOptions = {}
    ): void {
        const {
            successKey = 'admin.common.success.saved',
            errorKey = 'admin.common.errors.saveFailed',
            closeOnSuccess = true
        } = options;

        this.form.markAllAsTouched();
        if (this.form.invalid) {
            this.notify.warning('admin.validation.invalidForm');
            return;
        }
        if (this.isSubmitting()) return;

        this.setSubmitting(true);
        operation$.pipe(
            take(1),
            takeUntil(this.destroy$)
        ).subscribe({
            next: (result) => {
                this.setSubmitting(false);
                this.notify.success(successKey);
                this.onSaveSuccess(result);
                if (closeOnSuccess) {
                    this.close(result as unknown as TResult);
                }
            },
            error: (error) => {
                this.setSubmitting(false);
                this.notify.alert(errorKey);
                this.onSaveError(error);
                this.onError(error);
            }
        });
    }

    abstract save(): void;

    protected onSaveSuccess(result: any): void {}

    protected onSaveError(error: any): void {}
}
