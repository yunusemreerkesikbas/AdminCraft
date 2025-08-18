export interface Content {
    id: number;
    title: string;
    slug: string;
    data: string; // JSON content
    status: ContentStatus;
    language: Language;
    parentContentId?: number; // For translations
    contentTypeId: number;
    contentTypeName?: string;
    tenantId: number;
    metaTitle?: string;
    metaDescription?: string;
    createdAt: string;
    updatedAt?: string;
    publishedAt?: string;
    authorId: number;
    authorName?: string;
}

export interface ContentPagination {
    length: number;
    size: number;
    page: number;
    lastPage: number;
    startIndex: number;
    endIndex: number;
}

export enum ContentStatus {
    DRAFT = 'DRAFT',
    PUBLISHED = 'PUBLISHED',
    ARCHIVED = 'ARCHIVED'
}

export enum Language {
    TR = 'TR',
    EN = 'EN'
}

export interface ContentType {
    id: number;
    name: string;
    displayName: string;
    fields: string; // JSON schema
    tenantId: number;
    supportsMultiLanguage: boolean;
    createdAt: string;
    updatedAt?: string;
}

export interface CreateContentRequest {
    title: string;
    slug: string;
    data: string;
    status: ContentStatus;
    language: Language;
    parentContentId?: number;
    contentTypeId: number;
    metaTitle?: string;
    metaDescription?: string;
}

export interface UpdateContentRequest {
    title?: string;
    slug?: string;
    data?: string;
    status?: ContentStatus;
    language?: Language;
    parentContentId?: number;
    contentTypeId?: number;
    metaTitle?: string;
    metaDescription?: string;
}