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
 * Product field definition interface
 * Note: Backend may still return code, isRequired, isVisibleInList, sortOrder, defaultValue, validationConfig
 * but these fields are not used in the frontend
 */
export interface ProductFieldDefinition {
    id: number;
    uuid: string;
    uid: string;
    code: string;
    name: string;
    fieldType: ProductFieldType;
    createdAt?: string;
    updatedAt?: string;
}

/**
 * Request for creating a new product field
 */
export interface CreateProductFieldRequest {
    uid: string;
    name: string;
    fieldType: ProductFieldType;
}

/**
 * Request for updating an existing product field
 */
export interface UpdateProductFieldRequest {
    name?: string;
    fieldType?: ProductFieldType;
}

/**
 * Product custom fields map (code -> value)
 */
export type ProductCustomFields = Record<string, unknown>;
