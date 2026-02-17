import { Language } from '@shared/types/common.types';
import { ResponsiveMediaResponse } from '../../media/media.types';

export enum ComponentStatus {
    DRAFT = 'DRAFT',
    PUBLISHED = 'PUBLISHED',
    SCHEDULED = 'SCHEDULED',
    ARCHIVED = 'ARCHIVED'
}

export enum NavigationType {
    MAINMENU = 'MAINMENU',
    STATICPAGE = 'STATICPAGE'
}


export interface ComponentTypeDto {
    id: number;
    uid: string;
    code?: string;
    name: string;
    category?: string;
    icon?: string;
    isSystem?: boolean;
    createdAt: string;
    updatedAt?: string;
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
    navigationNodeId?: number;
    navigationLinkNodeId?: number;
    navigationType?: NavigationType;
    searchBox?: boolean;
    status: ComponentStatus;
    createdAt: string;
    updatedAt?: string;
    responsiveMedia?: ResponsiveMediaResponse;
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
    navigationNodeId?: number;
    navigationLinkNodeId?: number;
    navigationType?: NavigationType;
    searchBox?: boolean;
    status: ComponentStatus;
}

export interface UpdateComponentRequest {
    componentTypeId?: number;
    name: string;
    displayOrder?: number;
    isVisible?: boolean;
    styleClasses?: string;
    navigationNodeId?: number;
    navigationLinkNodeId?: number;
    navigationType?: NavigationType;
    searchBox?: boolean;
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
    status?: ComponentStatus;
}


export interface CreateComponentCompositeRequest {
    componentTypeId: number;
    name: string;
    displayOrder?: number;
    isVisible?: boolean;
    styleClasses?: string;
    navigationNodeId?: number;
    navigationLinkNodeId?: number;
    navigationType?: NavigationType;
    searchBox?: boolean;
    status?: ComponentStatus;
    translations: Record<Language, ComponentI18nRequest>;
}

export interface UpdateComponentCompositeRequest {
    name?: string;
    displayOrder?: number;
    isVisible?: boolean;
    styleClasses?: string;
    navigationNodeId?: number;
    navigationLinkNodeId?: number;
    navigationType?: NavigationType;
    searchBox?: boolean;
    status?: ComponentStatus;
    translations: Record<Language, ComponentI18nRequest>;
}

export interface ComponentCompositeResponse {
    id: number;
    uuid: string;
    uid: string;
    componentTypeId: number;
    componentTypeName: string;
    name: string;
    displayOrder: number;
    isVisible: boolean;
    styleClasses?: string;
    navigationNodeId?: number;
    navigationLinkNodeId?: number;
    navigationType?: NavigationType;
    searchBox?: boolean;
    status: ComponentStatus;
    createdAt: string;
    updatedAt: string;
    responsiveMedia?: ResponsiveMediaResponse;
    translations: Record<Language, ComponentI18nDto>;
}

// ==================== Component Entry Composite Types ====================

export interface ComponentEntryI18nRequest {
    title?: string;
    description?: string;
    status?: ComponentStatus;
    dynamicFields?: Record<string, any>;
}

export interface ComponentEntryI18nDto {
    id: number;
    uuid: string;
    uid: string;
    entryId: number;
    language: string;
    title?: string;
    description?: string;
    status: ComponentStatus;
    updatedAt?: string;
    customFields?: Record<string, any>;
}

export interface CreateComponentEntryCompositeRequest {
    componentId: number;
    sortOrder?: number;
    isVisible?: boolean;
    styleClasses?: string;
    status?: ComponentStatus;
    translations: Record<Language, ComponentEntryI18nRequest>;
}

export interface UpdateComponentEntryCompositeRequest {
    sortOrder?: number;
    isVisible?: boolean;
    styleClasses?: string;
    status?: ComponentStatus;
    translations: Record<Language, ComponentEntryI18nRequest>;
}

export interface ComponentEntryCompositeResponse {
    id: number;
    uuid: string;
    uid: string;
    componentId: number;
    sortOrder: number;
    isVisible: boolean;
    styleClasses?: string;
    status: ComponentStatus;
    createdAt: string;
    updatedAt: string;
    translations: Record<Language, ComponentEntryI18nDto>;
}
