export type ProductVariantOptionDisplayType = 'TEXT' | 'COLOR';

export interface ProductVariantOptionValue {
    id: number;
    uid: string;
    code: string;
    label: string;
    swatchValue?: string;
    sortOrder: number;
    active: boolean;
}

export interface ProductVariantOption {
    id: number;
    uid: string;
    code: string;
    name: string;
    displayType: ProductVariantOptionDisplayType;
    sortOrder: number;
    active: boolean;
    values: ProductVariantOptionValue[];
}

export interface ProductVariantOptionValueRequest {
    id?: number;
    label: string;
    swatchValue?: string;
    sortOrder?: number;
    active?: boolean;
}

export interface CreateProductVariantOptionRequest {
    name: string;
    displayType: ProductVariantOptionDisplayType;
    sortOrder?: number;
    active?: boolean;
    values: ProductVariantOptionValueRequest[];
}

export type UpdateProductVariantOptionRequest =
    Partial<CreateProductVariantOptionRequest>;
