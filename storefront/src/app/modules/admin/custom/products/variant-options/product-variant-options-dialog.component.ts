import { CommonModule } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    inject,
    OnInit,
    signal,
} from '@angular/core';
import {
    FormBuilder,
    FormsModule,
    ReactiveFormsModule,
    Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaCheckboxComponent } from '@shared/components/custom-ui/spa-checkbox/spa-checkbox.component';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaSelectComponent } from '@shared/components/custom-ui/spa-select/spa-select.component';
import {
    SpaDialogContentComponent,
    SpaDialogFooterComponent,
    SpaDialogHeaderComponent,
} from '@shared/components/spa-dialog';
import { NotificationService } from '@shared/notifications/notification.service';
import { take } from 'rxjs';
import {
    CreateProductVariantOptionRequest,
    ProductVariantOption,
    ProductVariantOptionDisplayType,
    ProductVariantOptionValueRequest,
} from '../models/product-variant-option.types';
import { ProductVariantOptionService } from '../services/product-variant-option.service';

interface VariantOptionValueDraft {
    id?: number;
    label: string;
    swatchValue?: string;
    active: boolean;
}

@Component({
    selector: 'spa-product-variant-options-dialog',
    templateUrl: './product-variant-options-dialog.component.html',
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        TranslocoModule,
        MatButtonModule,
        MatIconModule,
        SpaDialogHeaderComponent,
        SpaDialogContentComponent,
        SpaDialogFooterComponent,
        SpaInputComponent,
        SpaSelectComponent,
        SpaCheckboxComponent,
    ],
})
export class ProductVariantOptionsDialogComponent implements OnInit {
    #fb = inject(FormBuilder);
    #dialogRef = inject(MatDialogRef<ProductVariantOptionsDialogComponent>);
    #optionService = inject(ProductVariantOptionService);
    #notificationService = inject(NotificationService);

    protected optionsSig = signal<ProductVariantOption[]>([]);
    protected valuesSig = signal<VariantOptionValueDraft[]>([
        { label: '', swatchValue: '', active: true },
    ]);
    protected isSavingSig = signal(false);
    protected displayTypes: {
        value: ProductVariantOptionDisplayType;
        label: string;
    }[] = [
        { value: 'TEXT', label: 'Text' },
        { value: 'COLOR', label: 'Color' },
    ];

    protected form = this.#fb.group({
        name: ['', [Validators.required, Validators.maxLength(100)]],
        displayType: [
            'TEXT' as ProductVariantOptionDisplayType,
            Validators.required,
        ],
        active: [true],
    });

    ngOnInit(): void {
        this.#loadOptions();
    }

    protected addValue(): void {
        this.valuesSig.update((values) => [
            ...values,
            { label: '', swatchValue: '', active: true },
        ]);
    }

    protected removeValue(index: number): void {
        this.valuesSig.update((values) =>
            values.length <= 1 ? values : values.filter((_, i) => i !== index)
        );
    }

    protected updateValue(
        index: number,
        patch: Partial<VariantOptionValueDraft>
    ): void {
        this.valuesSig.update((values) =>
            values.map((value, i) =>
                i === index ? { ...value, ...patch } : value
            )
        );
    }

    protected saveOption(): void {
        this.form.markAllAsTouched();
        const values = this.valuesSig()
            .map((value, index): ProductVariantOptionValueRequest => ({
                id: value.id,
                label: value.label?.trim(),
                swatchValue: value.swatchValue?.trim() || undefined,
                sortOrder: index,
                active: value.active,
            }))
            .filter((value) => value.label);

        if (this.form.invalid || values.length === 0) {
            this.#notificationService.warning(
                'admin.common.validation.generalFormInvalid'
            );
            return;
        }

        const request: CreateProductVariantOptionRequest = {
            name: this.form.value.name!,
            displayType: this.form.value.displayType!,
            active: this.form.value.active ?? true,
            values,
        };

        this.isSavingSig.set(true);
        this.#optionService
            .create(request)
            .pipe(take(1))
            .subscribe({
                next: () => {
                    this.#notificationService.success(
                        'admin.common.messages.saveSuccess'
                    );
                    this.form.reset({
                        name: '',
                        displayType: 'TEXT',
                        active: true,
                    });
                    this.valuesSig.set([
                        { label: '', swatchValue: '', active: true },
                    ]);
                    this.#loadOptions();
                    this.isSavingSig.set(false);
                },
                error: () => {
                    this.#notificationService.alert(
                        'admin.common.errors.saveFailed'
                    );
                    this.isSavingSig.set(false);
                },
            });
    }

    protected deleteOption(option: ProductVariantOption): void {
        if (!confirm(`Delete ${option.name}?`)) {
            return;
        }
        this.#optionService
            .delete(option.id)
            .pipe(take(1))
            .subscribe({
                next: () => {
                    this.#notificationService.success(
                        'admin.common.messages.deleteSuccess'
                    );
                    this.#loadOptions();
                },
                error: () =>
                    this.#notificationService.alert(
                        'admin.common.errors.deleteFailed'
                    ),
            });
    }

    protected close(): void {
        this.#dialogRef.close(true);
    }

    #loadOptions(): void {
        this.#optionService
            .getAll()
            .pipe(take(1))
            .subscribe((options) => this.optionsSig.set(options ?? []));
    }
}
