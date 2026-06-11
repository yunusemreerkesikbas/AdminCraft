import { CommonModule } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    EventEmitter,
    Input,
    OnChanges,
    Output,
    SimpleChanges,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { SpaSelectComponent } from '@shared/components/custom-ui/spa-select/spa-select.component';
import { ProductVariantOption } from '../../models/product-variant-option.types';
import {
    ProductVariantRequest,
    ProductVariantResponse,
} from '../../models/product.types';

export interface ProductVariantFormRow {
    key: string;
    optionValueIds: number[];
    label: string;
    sku: string;
    price: number;
    firstPrice?: number | null;
    vatRate: number;
    stockQuantity: number;
    active: boolean;
}

@Component({
    selector: 'spa-product-variants-tab',
    templateUrl: './product-variants-tab.component.html',
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        CommonModule,
        FormsModule,
        MatButtonModule,
        MatIconModule,
        SpaSelectComponent,
    ],
})
export class ProductVariantsTabComponent implements OnChanges {
    @Input() variantOptions: ProductVariantOption[] = [];
    @Input() initialVariants: ProductVariantResponse[] = [];
    @Input() baseSku = 'SKU';
    @Input() basePrice: number | null = 0;

    @Output() variantsChange = new EventEmitter<ProductVariantRequest[]>();

    protected selectedVariantOptionIds: number[] = [];
    protected selectedValueIdsByOption: Record<number, number[]> = {};
    protected variantRows: ProductVariantFormRow[] = [];

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['initialVariants']) {
            this.patchVariants(this.initialVariants ?? []);
        }
    }

    protected selectedValueIds(optionId: number): number[] {
        return this.selectedValueIdsByOption[optionId] ?? [];
    }

    protected variantOptionById(
        optionId: number
    ): ProductVariantOption | undefined {
        return this.variantOptions.find((option) => option.id === optionId);
    }

    protected onVariantOptionsSelected(optionIds: number[]): void {
        const selectedIds = (optionIds ?? []).slice(0, 2);
        this.selectedVariantOptionIds = selectedIds;
        const next: Record<number, number[]> = {};
        selectedIds.forEach((id) => {
            next[id] = this.selectedValueIdsByOption[id] ?? [];
        });
        this.selectedValueIdsByOption = next;
        this.generateVariantMatrix();
    }

    protected onVariantValuesSelected(optionId: number, valueIds: number[]): void {
        this.selectedValueIdsByOption = {
            ...this.selectedValueIdsByOption,
            [optionId]: valueIds ?? [],
        };
        this.generateVariantMatrix();
    }

    protected generateVariantMatrix(): void {
        const selectedOptions = this.selectedVariantOptionIds
            .map((id) => this.variantOptions.find((option) => option.id === id))
            .filter((option): option is ProductVariantOption => Boolean(option));
        const currentRowsByKey = new Map(
            this.variantRows.map((row) => [row.key, row])
        );
        const baseSku = this.baseSku || 'SKU';
        const basePrice = Number(this.basePrice ?? 0);

        if (selectedOptions.length === 0) {
            const existing = currentRowsByKey.get('default');
            this.variantRows = [
                existing ?? {
                    key: 'default',
                    optionValueIds: [],
                    label: 'Default',
                    sku: baseSku,
                    price: basePrice,
                    firstPrice: null,
                    vatRate: 20,
                    stockQuantity: 0,
                    active: true,
                },
            ];
            this.emitVariants();
            return;
        }

        const valueGroups = selectedOptions.map((option) =>
            option.values.filter((value) =>
                this.selectedValueIds(option.id).includes(value.id)
            )
        );
        if (valueGroups.some((values) => values.length === 0)) {
            this.variantRows = [];
            this.emitVariants();
            return;
        }

        const combinations =
            valueGroups.length === 1
                ? valueGroups[0].map((value) => [value])
                : valueGroups[0].flatMap((first) =>
                      valueGroups[1].map((second) => [first, second])
                  );

        this.variantRows = combinations.map((values) => {
            const ids = values.map((value) => value.id);
            const key = ids.join('-');
            const existing = currentRowsByKey.get(key);
            const label = values.map((value) => value.label).join(' / ');
            return (
                existing ?? {
                    key,
                    optionValueIds: ids,
                    label,
                    sku:
                        baseSku +
                        '-' +
                        values.map((value) => value.code.toUpperCase()).join('-'),
                    price: basePrice,
                    firstPrice: null,
                    vatRate: 20,
                    stockQuantity: 0,
                    active: true,
                }
            );
        });
        this.emitVariants();
    }

    protected updateVariantRow(
        key: string,
        patch: Partial<ProductVariantFormRow>
    ): void {
        this.variantRows = this.variantRows.map((row) =>
            row.key === key ? { ...row, ...patch } : row
        );
        this.emitVariants();
    }

    private patchVariants(variants: ProductVariantResponse[]): void {
        if (!variants.length) {
            this.generateVariantMatrix();
            return;
        }

        const optionIds = Array.from(
            new Set(
                variants.flatMap((variant) =>
                    (variant.optionValues ?? []).map((value) => value.optionId)
                )
            )
        ).slice(0, 2);
        const selectedValues: Record<number, number[]> = {};
        optionIds.forEach((optionId) => {
            selectedValues[optionId] = Array.from(
                new Set(
                    variants.flatMap((variant) =>
                        (variant.optionValues ?? [])
                            .filter((value) => value.optionId === optionId)
                            .map((value) => value.valueId)
                    )
                )
            );
        });

        this.selectedVariantOptionIds = optionIds;
        this.selectedValueIdsByOption = selectedValues;
        this.variantRows = variants.map((variant) => {
            const optionValueIds = (variant.optionValues ?? []).map(
                (value) => value.valueId
            );
            return {
                key: optionValueIds.length ? optionValueIds.join('-') : 'default',
                optionValueIds,
                label: optionValueIds.length
                    ? (variant.optionValues ?? [])
                          .map((value) => value.valueLabel)
                          .join(' / ')
                    : 'Default',
                sku: variant.sku,
                price: variant.price?.value ?? 0,
                firstPrice: variant.firstPrice?.value ?? null,
                vatRate: Number(variant.vatRate ?? 20),
                stockQuantity: variant.stockQuantity ?? 0,
                active: variant.active ?? true,
            };
        });
        this.emitVariants();
    }

    private emitVariants(): void {
        this.variantsChange.emit(this.buildVariantRequest());
    }

    private buildVariantRequest(): ProductVariantRequest[] {
        if (this.variantRows.length === 0) {
            return [
                {
                    sku: this.baseSku || 'SKU',
                    price: Number(this.basePrice ?? 0),
                    firstPrice: null,
                    vatRate: 20,
                    stockQuantity: 0,
                    active: true,
                    optionValueIds: [],
                },
            ];
        }
        return this.variantRows.map((row) => ({
            sku: row.sku,
            price: Number(row.price ?? 0),
            firstPrice:
                row.firstPrice === undefined || row.firstPrice === null
                    ? null
                    : Number(row.firstPrice),
            vatRate: Number(row.vatRate ?? 20),
            stockQuantity: Number(row.stockQuantity ?? 0),
            active: row.active,
            optionValueIds: row.optionValueIds,
        }));
    }
}
