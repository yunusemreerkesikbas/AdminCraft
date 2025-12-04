export enum ComponentStatus {
    DRAFT = 'DRAFT',
    ACTIVE = 'ACTIVE',
    INACTIVE = 'INACTIVE'
}


export interface ComponentTypeDto {
    id: number;
    code: string;
    name: string;
    category?: string;
    icon?: string;
    isSystem: boolean;
    createdAt: string;
}

export interface CreateComponentTypeRequest {
    code: string;
    name: string;
    category?: string;
    icon?: string;
}

export interface UpdateComponentTypeRequest {
    name: string;
    category?: string;
    icon?: string;
}

export interface ComponentDto {
    id: number;
    uuid: string;
    uid: string;
    componentTypeId: number;
    componentTypeName?: string;
    name: string;
    displayOrder?: number;
    isVisible?: boolean;
    styleClasses?: string;
    status: ComponentStatus;
    createdAt: string;
    updatedAt?: string;
}

export interface ComponentDetailDto extends ComponentDto {
    translations: {
        [language: string]: ComponentI18nDto;
    };
    metadata?: {
        translationCount: number;
        publishedTranslationCount: number;
    };
}

export interface CreateComponentRequest {
    componentTypeId: number;
    name: string;
    displayOrder?: number;
    isVisible?: boolean;
    styleClasses?: string;
    status: ComponentStatus;
}

export interface UpdateComponentRequest {
    name: string;
    displayOrder?: number;
    isVisible?: boolean;
    styleClasses?: string;
    status: ComponentStatus;
}

export interface ComponentI18nDto {
    id: number;
    uuid: string;
    uid: string;
    componentId: number;
    language: string;
    title?: string;
    subtitle?: string;
    description?: string;
    status: ComponentStatus;
    updatedAt?: string;
}

export interface ComponentI18nRequest {
    title?: string;
    subtitle?: string;
    description?: string;
    status: ComponentStatus;
}
