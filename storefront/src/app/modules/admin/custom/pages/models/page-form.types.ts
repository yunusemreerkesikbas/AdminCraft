import { PageStatus } from '../page-builder.types';

export interface PageI18nFormData {
  canonicalUrl?: string | null;
  title?: string | null;

  description?: string | null;
}

export interface CreatePageFormData {
  templateId?: number | null;
  status?: PageStatus;
  styleClasses?: string | null;

  [languageCode: string]: PageI18nFormData | number | string | boolean | null | undefined;
}

export function isPageI18nFormData(value: unknown): value is PageI18nFormData {
  if (!value || typeof value !== 'object') {
    return false;
  }

  const data = value as Record<string, unknown>;
  const validKeys = ['canonicalUrl', 'title', 'description'];
  const keys = Object.keys(data);

  return keys.length > 0 && keys.every(key => validKeys.includes(key));
}

export interface EditPageFormData extends CreatePageFormData {
  // Inherits all fields from CreatePageFormData
  // Used when editing existing pages with pre-populated data
}
