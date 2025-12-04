export interface EntryFieldDefinition {
    id?: number;
    componentTypeId: number;
    fieldKey: string;
    fieldType: 'text' | 'textarea' | 'number' | 'boolean';
    isRequired: boolean;
    maxLength?: number;
    minValue?: number;
    maxValue?: number;
}

export interface CreateEntryFieldRequest {
    fieldKey: string;
    fieldType: 'text' | 'textarea' | 'number' | 'boolean';
    isRequired: boolean;
    maxLength?: number;
    minValue?: number;
    maxValue?: number;
}

export interface ImportSchemaRequest {
    fields: CreateEntryFieldRequest[];
}

export interface ImportResultResponse {
    successCount: number;
    failedCount: number;
    errors: string[];
}

export interface ComponentEntry {
    id: number;
    uuid: string;
    uid: string;
    componentId: number;
    sortOrder: number;
    isVisible: boolean;
    styleClasses?: string;
    status: 'DRAFT' | 'ACTIVE' | 'INACTIVE';
    createdAt: string;
    updatedAt: string;
}

export interface ComponentEntryDetailDto extends ComponentEntry {
    translations: {
        [language: string]: EntryI18nDto;
    };
    metadata?: {
        translationCount: number;
        publishedTranslationCount: number;
    };
}

export interface CreateEntryRequest {
    componentId: number;
    sortOrder: number;
    isVisible: boolean;
    styleClasses?: string;
}

export interface UpdateEntryRequest {
    sortOrder?: number;
    isVisible: boolean;
    styleClasses?: string;
    status: 'DRAFT' | 'ACTIVE' | 'INACTIVE';
}

export interface EntryI18nDto {
    id: number;
    entryId: number;
    language: 'TR' | 'EN';
    title?: string;
    description?: string;
    status: 'DRAFT' | 'ACTIVE' | 'INACTIVE';
    [key: string]: any;  // For dynamic entry fields
}

export interface EntryI18nRequest {
    title?: string;
    description?: string;
    [key: string]: any;  // For dynamic entry fields
}

export interface EntryFieldDefinitionResponse extends EntryFieldDefinition {
    id: number;
    createdAt: string;
    updatedAt: string;
}

export interface ValidationResult {
    valid: boolean;
    errors?: string[];
}

