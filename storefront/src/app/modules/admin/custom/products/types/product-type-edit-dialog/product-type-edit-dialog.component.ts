import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaDialogContentComponent, SpaDialogFooterComponent, SpaDialogHeaderComponent } from '@shared/components/spa-dialog';
import { SpaLocalizedFormDialog } from '@shared/components/spa-localized-form-dialog';
import { SpaTabContainerComponent, SpaTabContentDirective } from '@shared/components/spa-tab-container';
import { VALIDATION_LIMITS, VALIDATION_PATTERNS } from '@shared/constants/validation.constants';
import { NotificationService } from '@shared/notifications/notification.service';
import { SpaDateTimePipe } from '@shared/pipes/spa-date-time.pipe';
import { ConfirmationService } from '@shared/services/confirmation.service';
import { take } from 'rxjs';
import { AttributeDefinition, CreateProductTypeRequest, ProductType } from '../../models/product-type.types';
import { ProductTypeService } from '../../services/product-type.service';
import { ProductAttributeDialogComponent } from '../product-attribute-dialog/product-attribute-dialog.component';

export interface ProductTypeEditDialogData {
    mode: 'create' | 'edit';
    item?: ProductType;
}

@Component({
    selector: 'spa-product-type-edit-dialog',
    templateUrl: './product-type-edit-dialog.component.html',
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        TranslocoModule,
        MatButtonModule,
        MatIconModule,
        MatTableModule,
        MatProgressSpinnerModule,
        SpaDialogHeaderComponent,
        SpaDialogContentComponent,
        SpaDialogFooterComponent,
        SpaInputComponent,
        SpaTabContainerComponent,
        SpaTabContentDirective
    ]
})
export class ProductTypeEditDialogComponent extends SpaLocalizedFormDialog<boolean, ProductTypeEditDialogData> implements OnInit {
    override data = inject<ProductTypeEditDialogData>(MAT_DIALOG_DATA);
    #productTypeService = inject(ProductTypeService);
    #matDialog = inject(MatDialog);
    #confirmationService = inject(ConfirmationService);
    #notificationService = inject(NotificationService);
    #datePipe = new SpaDateTimePipe();

    attributes = signal<AttributeDefinition[]>([]);
    isLoadingAttributes = signal(false);

    displayedColumns = ['code', 'name', 'fieldType', 'required', 'actions'];

    tabs = [
        { id: 'general', label: 'admin.common.general', icon: 'settings' },
        { id: 'attributes', label: 'admin.products.types.tabs.attributes', icon: 'list_alt', disabled: this.data.mode === 'create' }
    ];

    activeTab = 0;

    override ngOnInit(): void {
        super.ngOnInit();
        if (this.data.mode === 'edit' && this.data.item) {
            this.loadAttributes();
        }
    }

    protected buildGeneralForm(): FormGroup {
        return this.fb.group({
            code: [this.data.item?.code || '', [
                Validators.required,
                Validators.maxLength(VALIDATION_LIMITS.PRODUCT_TYPE_CODE_MAX),
                Validators.pattern(VALIDATION_PATTERNS.CODE)
            ]],
            name: [this.data.item?.name || '', [
                Validators.required,
                Validators.maxLength(VALIDATION_LIMITS.PRODUCT_TYPE_NAME_MAX)
            ]],
            category: [this.data.item?.category || '', [
                Validators.maxLength(VALIDATION_LIMITS.PRODUCT_TYPE_CATEGORY_MAX)
            ]],
            createdAt: [{ value: this.#formatDateTime(this.data.item?.createdAt), disabled: true }],
            updatedAt: [{ value: this.#formatDateTime(this.data.item?.updatedAt), disabled: true }]
        });
    }

    #formatDateTime(value: string | Date | null | undefined): string {
        return this.#datePipe.transform(value);
    }

    protected buildI18nForm(lang: string): FormGroup {
        return this.fb.group({});
    }

    loadAttributes(): void {
        if (!this.data.item) return;
        this.isLoadingAttributes.set(true);
        this.#productTypeService.getAttributes(this.data.item.id).pipe(take(1)).subscribe({
            next: (attrs) => {
                this.attributes.set(attrs);
                this.isLoadingAttributes.set(false);
            },
            error: () => {
                this.#notificationService.alert('admin.products.types.errors.loadAttributesFailed');
                this.isLoadingAttributes.set(false);
            }
        });
    }

    save(): void {
        if (this.generalForm.invalid) {
            this.generalForm.markAllAsTouched();
            return;
        }

        this.setSubmitting(true);
        const formValue = this.generalForm.value;

        let request$;
        if (this.data.mode === 'create') {
            const request: CreateProductTypeRequest = {
                code: formValue.code,
                name: formValue.name,
                category: formValue.category || undefined
            };
            request$ = this.#productTypeService.create(request);
        } else {
            request$ = this.#productTypeService.update(this.data.item!.id, {
                name: formValue.name,
                category: formValue.category || undefined
            });
        }

        request$.pipe(take(1)).subscribe({
            next: (res) => {
                this.setSubmitting(false);
                this.#notificationService.success('admin.common.messages.saveSuccess');
                this.close(true);
            },
            error: (err) => {
                this.setSubmitting(false);
                this.#notificationService.alert(err?.error?.message || 'admin.common.errors.saveFailed');
            }
        });
    }

    addAttribute(): void {
        this.#matDialog.open(ProductAttributeDialogComponent, {
            width: '600px',
            disableClose: true,
            data: { mode: 'create' }
        }).afterClosed().pipe(take(1)).subscribe(result => {
            if (result) {
                this.#productTypeService.createAttribute(this.data.item!.id, result).pipe(take(1)).subscribe({
                    next: () => {
                        this.#notificationService.success('admin.products.attributes.createSuccess');
                        this.loadAttributes();
                    },
                    error: (err) => this.#notificationService.alert(err?.error?.message || 'admin.products.attributes.createFailed')
                });
            }
        });
    }

    editAttribute(attr: AttributeDefinition): void {
        this.#matDialog.open(ProductAttributeDialogComponent, {
            width: '600px',
            disableClose: true,
            data: { mode: 'edit', attribute: attr }
        }).afterClosed().pipe(take(1)).subscribe(result => {
            if (result) {
                this.#productTypeService.updateAttribute(this.data.item!.id, attr.id, result).pipe(take(1)).subscribe({
                    next: () => {
                        this.#notificationService.success('admin.products.attributes.updateSuccess');
                        this.loadAttributes();
                    },
                    error: (err) => this.#notificationService.alert(err?.error?.message || 'admin.products.attributes.updateFailed')
                });
            }
        });
    }

    deleteAttribute(attr: AttributeDefinition): void {
        this.#confirmationService.confirm('admin.products.attributes.delete.title', 'admin.products.attributes.delete.message')
            .pipe(take(1))
            .subscribe(confirmed => {
                if (confirmed) {
                    this.#productTypeService.deleteAttribute(this.data.item!.id, attr.id).pipe(take(1)).subscribe({
                        next: () => {
                            this.#notificationService.success('admin.products.attributes.deleteSuccess');
                            this.loadAttributes();
                        },
                        error: (err) => this.#notificationService.alert(err?.error?.message || 'admin.products.attributes.deleteFailed')
                    });
                }
            });
    }
}
