export enum ComponentStatus {
    DRAFT = 'DRAFT',
    ACTIVE = 'ACTIVE',
    INACTIVE = 'INACTIVE'
}

export interface Link {
    label: string;
    url: string;
    target?: '_blank' | '_self';
}

export interface ComponentBaseData {
    order?: number;
    isVisible?: boolean;
    styleClasses?: string;
}

export interface ComponentBaseLocalizedData {
    title?: string;
    subtitle?: string;
    description?: string;
    imageUrl?: string;
    imageAlt?: string;
    buttonText?: string;
    buttonUrl?: string;
    buttonStyle?: string;
    links?: Link[];
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
    code: string;
    name: string;
    baseData: ComponentBaseData;
    status: ComponentStatus;
    createdAt: string;
    updatedAt?: string;
}

export interface ComponentDetailDto extends ComponentDto {
    translations?: ComponentI18nDto[];
}

export interface CreateComponentRequest {
    componentTypeId: number;
    code: string;
    name: string;
    baseData: ComponentBaseData;
    status: ComponentStatus;
}

export interface UpdateComponentRequest {
    name: string;
    baseData: ComponentBaseData;
    status: ComponentStatus;
}

export interface ComponentI18nDto {
    id: number;
    uuid: string;
    uid: string;
    componentId: number;
    language: string;
    baseLocalizedData: ComponentBaseLocalizedData;
    status: ComponentStatus;
    publishedAt?: string;
    createdAt: string;
    updatedAt?: string;
}

export interface ComponentI18nRequest {
    baseLocalizedData: ComponentBaseLocalizedData;
    status: ComponentStatus;
}
