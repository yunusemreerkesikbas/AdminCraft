/**
 * Field types for global product fields
 * Matches backend ProductFieldType enum
 */
export type ProductFieldType =
    | 'TEXT'
    | 'NUMBER'
    | 'BOOLEAN'
    | 'DATE'
    | 'RICHTEXT'
    | 'MEDIA';

/**
 * Validation config for fields
 */
export interface ValidationConfig {
    minLength?: number;
    maxLength?: number;
    pattern?: string;
    minValue?: number;
    maxValue?: number;
    minDate?: string;
    maxDate?: string;
}

/**
 * Product field definition interface
 */
export interface ProductFieldDefinition {
    id: number;
    uuid: string;
    uid: string;
    code: string;
    name: string;
    fieldType: ProductFieldType;
    isRequired: boolean;
    isVisibleInList: boolean;
    sortOrder: number;
    defaultValue?: string;
    validationConfig?: ValidationConfig;
    createdAt?: string;
    updatedAt?: string;
}

/**
 * Request for creating a new product field
 */
export interface CreateProductFieldRequest {
    uid: string;
    code: string;
    name: string;
    fieldType: ProductFieldType;
    isRequired?: boolean;
    isVisibleInList?: boolean;
    sortOrder?: number;
    defaultValue?: string;
    validationConfig?: ValidationConfig;
}

/**
 * Request for updating an existing product field
 */
export interface UpdateProductFieldRequest {
    name?: string;
    fieldType?: ProductFieldType;
    isRequired?: boolean;
    isVisibleInList?: boolean;
    sortOrder?: number;
    defaultValue?: string;
    validationConfig?: ValidationConfig;
}

/**
 * Product custom fields map (code -> value)
 */
export type ProductCustomFields = Record<string, unknown>;
