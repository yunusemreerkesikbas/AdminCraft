export interface ProductType {
    id: number;
    uid: string;
    code: string;
    name: string;
    category: string;
    createdAt?: string;
    updatedAt?: string;
}

export interface CreateProductTypeRequest {
    code: string;
    name: string;
    category?: string;
}

export interface UpdateProductTypeRequest {
    name: string;
    category?: string;
}

export interface AttributeDefinition {
    id: number;
    uid: string;
    productTypeId: number;
    code: string;
    name: string;
    fieldType: ProductFieldType;
    isRequired: boolean;
    isSearchable: boolean;
    sortOrder: number;
    validationConfig?: Record<string, any>;
}

export interface CreateAttributeDefinitionRequest {
    code: string;
    name: string;
    fieldType: ProductFieldType;
    isRequired?: boolean;
    isSearchable?: boolean;
    sortOrder?: number;
    validationConfig?: Record<string, any>;
}

export interface UpdateAttributeDefinitionRequest {
    name: string;
    isRequired?: boolean;
    isSearchable?: boolean;
    sortOrder?: number;
    validationConfig?: Record<string, any>;
}

export type ProductFieldType = 'TEXT' | 'RICHTEXT' | 'NUMBER' | 'BOOLEAN' | 'DATE' | 'MEDIA';

export const PRODUCT_FIELD_TYPES: { value: ProductFieldType; label: string }[] = [
    { value: 'TEXT', label: 'Text' },
    { value: 'RICHTEXT', label: 'Rich Text' },
    { value: 'NUMBER', label: 'Number' },
    { value: 'BOOLEAN', label: 'Boolean' },
    { value: 'DATE', label: 'Date' },
    { value: 'MEDIA', label: 'Media' }
];
