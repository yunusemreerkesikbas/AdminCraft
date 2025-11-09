import { ComponentStatus, ExtendedFieldsSchema, Link } from './component-library.types';

export interface ComponentI18nFormData {
    title?: string | null;
    subtitle?: string | null;
    description?: string | null;
    imageUrl?: string | null;
    imageAlt?: string | null;
    buttonText?: string | null;
    buttonUrl?: string | null;
    buttonStyle?: string | null;
    links?: Link[] | null;
    [extendedFieldKey: string]: any;
}

export interface CreateComponentFormData {
    componentTypeId?: number | null;
    code?: string | null;
    name?: string | null;
    status?: ComponentStatus;
    order?: number;
    isVisible?: boolean;
    styleClasses?: string | null;
    [languageCode: string]: ComponentI18nFormData | number | string | boolean | Link[] | null | undefined;
}

export function isComponentI18nFormData(value: unknown): value is ComponentI18nFormData {
    if (!value || typeof value !== 'object') {
        return false;
    }
    const data = value as Record<string, unknown>;
    const validKeys = ['title', 'subtitle', 'description', 'imageUrl', 'imageAlt', 'buttonText', 'buttonUrl', 'buttonStyle', 'links'];
    const keys = Object.keys(data);
    return keys.length > 0 && keys.every(key => validKeys.includes(key));
}

export interface EditComponentFormData extends CreateComponentFormData {
}

export interface ComponentTypeFormData {
    code?: string | null;
    name?: string | null;
    category?: string | null;
    icon?: string | null;
    extendedFieldsSchema?: ExtendedFieldsSchema | null;
}
