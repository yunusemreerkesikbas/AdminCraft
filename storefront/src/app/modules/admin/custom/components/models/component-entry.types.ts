export interface EntryFieldDefinition {
    id?: number;
    componentTypeId: number;
    fieldKey: string;
    fieldType: 'text' | 'textarea' | 'number' | 'boolean' | 'media';
}

export interface CreateEntryFieldRequest {
    fieldKey: string;
    fieldType: 'text' | 'textarea' | 'number' | 'boolean' | 'media';
}

export interface ComponentEntry {
    id: number;
    uuid: string;
    uid: string;
    componentId: number;
    sortOrder: number;
    isVisible: boolean;
    styleClasses?: string;
    status: 'DRAFT' | 'PUBLISHED' | 'SCHEDULED' | 'ARCHIVED';
    responsiveMedia?: ResponsiveMediaDto;
    createdAt: string;
    updatedAt: string;
}

export interface ResponsiveMediaDto {
    id: number;
    uuid?: string;
    uid?: string;
    code?: string;
    desktopMedia?: MediaSummaryDto;
    mobileMedia?: MediaSummaryDto;
}

export interface MediaSummaryDto {
    id: number;
    uid: string;
    fileName?: string | null;
    originalName?: string | null;
    mimeType?: string | null;
    publicUrl?: string | null;
    width?: number | null;
    height?: number | null;
    fileSizeFormatted?: string | null;
}

export interface EntryCustomFields {
    mediaUid?: string;
    media?: MediaSummaryDto;
    [key: string]: any;
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
    status: 'DRAFT' | 'PUBLISHED' | 'SCHEDULED' | 'ARCHIVED';
}

export interface EntryI18nDto {
    id: number;
    entryId: number;
    language: 'TR' | 'EN';
    title?: string;
    description?: string;
    status: 'DRAFT' | 'PUBLISHED' | 'SCHEDULED' | 'ARCHIVED';
    customFields?: EntryCustomFields;
    [key: string]: any;  // For dynamic entry fields
}

export interface EntryI18nRequest {
    title?: string | null;
    description?: string | null;
    dynamicFields?: Record<string, any> | null;
    [key: string]: any;  // For dynamic entry fields
}

export interface EntryFieldDefinitionResponse extends EntryFieldDefinition {
    id: number;
    createdAt: string;
    updatedAt?: string;
}

export interface CreateComponentEntryCompositeRequest {
    componentId: number;
    sortOrder?: number;
    isVisible?: boolean;
    styleClasses?: string;
    responsiveMediaId?: number | null;
    translations: Record<string, EntryI18nRequest>;
}

export interface UpdateComponentEntryCompositeRequest {
    sortOrder?: number;
    isVisible?: boolean;
    styleClasses?: string;
    responsiveMediaId?: number | null;
    translations: Record<string, EntryI18nRequest>;
}

export interface ComponentEntryCompositeResponse {
    id: number;
    uuid: string;
    uid: string;
    componentId: number;
    sortOrder: number;
    isVisible: boolean;
    styleClasses?: string;
    status: 'DRAFT' | 'PUBLISHED' | 'SCHEDULED' | 'ARCHIVED';
    responsiveMedia?: ResponsiveMediaDto;
    createdAt: string;
    updatedAt: string;
    translations: Record<string, EntryI18nDto>;
}
