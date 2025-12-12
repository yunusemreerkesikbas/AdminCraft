import { Directive, inject, OnDestroy, signal } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Subject } from 'rxjs';
import { SpaDialogData } from './spa-dialog-base.types';

@Directive()
export abstract class SpaDialogBase<
    TResult = any,
    TData extends SpaDialogData = SpaDialogData
> implements OnDestroy {

    protected readonly dialogRef = inject(MatDialogRef<any>);
    protected readonly data = inject<TData>(MAT_DIALOG_DATA, { optional: true });
    protected readonly destroy$ = new Subject<void>();

    readonly isSubmitting = signal(false);
    activeTab = 0;

    cancel(): void {
        if (!this.isSubmitting()) {
            this.onCancel();
            this.dialogRef.close(null);
        }
    }

    close(result?: TResult): void {
        this.dialogRef.close(result);
    }

    protected setSubmitting(value: boolean): void {
        this.isSubmitting.set(value);
    }

    protected setActiveTab(index: number): void {
        this.activeTab = index;
    }

    ngOnDestroy(): void {
        this.destroy$.next();
        this.destroy$.complete();
    }

    protected onCancel(): void {}

    protected onError(error: any): void {}
}
