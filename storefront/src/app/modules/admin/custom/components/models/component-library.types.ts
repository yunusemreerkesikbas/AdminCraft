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
    navigationAware: boolean;
    icon?: string;
    isSystem?: boolean;
    createdAt: string;
    updatedAt?: string;
}


export interface CreateComponentTypeRequest {
    name: string;
    category?: string;
    navigationAware: boolean;
}

export interface UpdateComponentTypeRequest {
    name: string;
    category?: string;
    navigationAware: boolean;
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
    navigationType?: NavigationType;
    searchBox?: boolean;
    status: ComponentStatus;
    createdAt: string;
    updatedAt?: string;
    responsiveMedia?: ResponsiveMediaResponse;
}

export interface ComponentI18nContentDto {
    title?: string;
    subtitle?: string;
    description?: string;
}

export interface ComponentDetailDto {
    id: number;
    uuid: string;
    uid: string;
    name?: string;
    componentTypeId: number;
    componentTypeName?: string;
    displayOrder?: number;
    isVisible?: boolean;
    styleClasses?: string;
    navigationNodeId?: number;
    navigationType?: NavigationType;
    searchBox?: boolean;
    status: ComponentStatus;
    createdAt: string;
    updatedAt?: string;
    responsiveMedia?: ResponsiveMediaResponse;
    translations: {
        [language: string]: ComponentI18nContentDto;
    };
    metadata?: {
        translationCount: number;
        publishedTranslationCount: number;
    };
}

export interface CreateComponentRequest {
    componentTypeId: number;
    uid: string;
    name: string;
    displayOrder?: number;
    isVisible?: boolean;
    styleClasses?: string;
    navigationNodeId?: number;
    navigationType?: NavigationType;
    searchBox?: boolean;
    status: ComponentStatus;
}

export interface UpdateComponentRequest {
    componentTypeId?: number;
    uid?: string;
    name: string;
    displayOrder?: number;
    isVisible?: boolean;
    styleClasses?: string;
    navigationNodeId?: number;
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
    title?: string | null;
    subtitle?: string | null;
    description?: string | null;
}


export interface CreateComponentCompositeRequest {
    componentTypeId: number;
    uid: string;
    name: string;
    displayOrder?: number;
    isVisible?: boolean;
    styleClasses?: string;
    navigationNodeId?: number;
    navigationType?: NavigationType;
    searchBox?: boolean;
    translations: Record<Language, ComponentI18nRequest>;
}

export interface UpdateComponentCompositeRequest {
    uid?: string;
    name?: string;
    displayOrder?: number;
    isVisible?: boolean;
    styleClasses?: string;
    navigationNodeId?: number;
    navigationType?: NavigationType;
    searchBox?: boolean;
    responsiveMediaId?: number | null;
    translations?: Record<Language, ComponentI18nRequest>;
}

export interface ComponentCompositeResponse {
    id: number;
    uuid: string;
    uid: string;
    componentTypeId: number;
    componentTypeName: string;
    displayOrder: number;
    isVisible: boolean;
    styleClasses?: string;
    navigationNodeId?: number;
    navigationType?: NavigationType;
    searchBox?: boolean;
    status: ComponentStatus;
    createdAt: string;
    updatedAt: string;
    responsiveMedia?: ResponsiveMediaResponse;
    translations: Record<Language, ComponentI18nContentDto>;
}

export type { BulkDeleteError, BulkDeleteRequest, BulkDeleteResult } from '@core/crud/bulk-delete.types';

// ==================== Component Entry Composite Types ====================

export interface ComponentEntryI18nRequest {
    title?: string;
    description?: string;
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
    translations: Record<Language, ComponentEntryI18nRequest>;
}

export interface UpdateComponentEntryCompositeRequest {
    sortOrder?: number;
    isVisible?: boolean;
    styleClasses?: string;
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
