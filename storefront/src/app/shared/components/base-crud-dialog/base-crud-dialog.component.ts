import { Directive, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { CrudEntity } from '@core/crud/api.types';
import { CrudHttpService } from '@core/crud/crud-http.service';
import { NotificationService } from '@shared/notifications/notification.service';
import { take, takeUntil } from 'rxjs/operators';
import { BaseDialogComponent } from '../base-dialog/base-dialog.component';
import { CrudDialogData } from '../base-dialog/base-dialog.types';

@Directive()
export abstract class BaseCrudDialogComponent<
  TDto extends CrudEntity,
  TCreate = Partial<TDto>,
  TUpdate = Partial<TDto>
> extends BaseDialogComponent<TDto, CrudDialogData<TDto>> implements OnInit {

  protected readonly fb = inject(FormBuilder);
  protected readonly notify = inject(NotificationService);
  protected abstract service: CrudHttpService<TDto, TCreate, TUpdate>;
  protected abstract form: FormGroup;

  get mode(): 'create' | 'edit' {
    return this.data?.mode || (this.data?.id ? 'edit' : 'create');
  }

  get isEditMode(): boolean {
    return this.mode === 'edit';
  }

  get isCreateMode(): boolean {
    return this.mode === 'create';
  }

  ngOnInit(): void {
    this.initializeForm();
  }

  protected initializeForm(): void {
    if (this.data?.initial && this.form) {
      this.form.patchValue(this.data.initial);
    }
  }

  protected override onSave(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid) {
      this.notify.warning('admin.validation.invalidForm');
      return;
    }
    if (this.isSubmitting()) {
      return;
    }

    this.setSubmitting(true);
    const payload = this.buildPayload();
    const operation$ = this.isEditMode
      ? this.service.update(this.data.id! as number, payload as TUpdate)
      : this.service.create(payload as TCreate);
    operation$.pipe(
      take(1),
      takeUntil(this.destroy$)
    ).subscribe({
      next: (result) => {
        this.setSubmitting(false);
        this.onSaveSuccess(result);
        this.close(result);
      },
      error: (error) => {
        this.setSubmitting(false);
        this.onSaveError(error);
        this.onError(error);
      }
    });
  }

  save(): void {
    this.onSave();
  }

  protected abstract buildPayload(): TCreate | TUpdate;

  protected onSaveSuccess(result: TDto): void {
    const messageKey = this.isEditMode
      ? 'admin.common.success.updated'
      : 'admin.common.success.created';
    this.notify.success(messageKey);
  }

  protected onSaveError(error: any): void {
    const messageKey = this.isEditMode
      ? 'admin.common.errors.updateFailed'
      : 'admin.common.errors.createFailed';
    this.notify.alert(messageKey);
  }
}
