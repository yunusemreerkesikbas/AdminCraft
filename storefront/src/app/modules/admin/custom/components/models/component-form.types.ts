import { ComponentStatus, Link } from './component-library.types';

export interface ComponentI18nFormData {
    title?: string | null;
    subtitle?: string | null;
    description?: string | null;
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
    const validKeys = ['title', 'subtitle', 'description', 'links'];
    const keys = Object.keys(data);
    return keys.length > 0 && keys.some(key => validKeys.includes(key));
}

export interface EditComponentFormData extends CreateComponentFormData {
}

export interface ComponentTypeFormData {
    code?: string | null;
    name?: string | null;
    category?: string | null;
    icon?: string | null;
}
