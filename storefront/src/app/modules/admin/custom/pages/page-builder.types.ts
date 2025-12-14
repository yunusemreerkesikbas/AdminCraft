import { Language } from '@shared/types/common.types';
export { Language };
export type PageStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED' | 'SCHEDULED';

export interface PageDto {
  id: number;
  uuid: string;
  uid: string;
  tenantId: number;
  templateId?: number | null;
  status: PageStatus;
  featuredImage?: string | null;
  styleClasses?: string | null;
  sortOrder: number;
  createdAt: string;
  updatedAt: string;
}

export interface PageI18nDto {
  id: number;
  uuid: string;
  uid: string;
  pageId: number;
  tenantId: number;
  language: Language;
  urlPath?: string | null;
  title?: string | null;
  subtitle?: string | null;
  metaTitle?: string | null;
  metaDescription?: string | null;
  description?: string | null;
  descriptionHtml?: string | null;
  status: PageStatus;
  publishedAt?: string | null;
  scheduledAt?: string | null;
  updatedAt: string;
  fallbackLanguage?: boolean;
}

export interface PageListDto {
  id: number;
  uuid: string;
  uid: string;
  tenantId: number;
  templateId?: number | null;
  status: PageStatus;
  featuredImage?: string | null;
  styleClasses?: string | null;
  sortOrder: number;
  createdAt: string;
  updatedAt: string;
  translations: {
    [key in Language]?: boolean;
  };
}

export interface PageDetailDto {
  id: number;
  uuid: string;
  uid: string;
  tenantId: number;
  templateId?: number | null;
  status: PageStatus;
  featuredImage?: string | null;
  styleClasses?: string | null;
  sortOrder: number;
  createdAt: string;
  updatedAt: string;
  translations: {
    [key in Language]?: PageI18nDto;
  };
  metadata: {
    translationCount: number;
    publishedTranslationCount: number;
  };
}

export interface CreatePageRequest {
  templateId?: number | null;
  status?: PageStatus;
  featuredImage?: string | null;
  styleClasses?: string | null;
  sortOrder?: number;
}

export interface UpdatePageRequest {
  id: number;
  templateId?: number | null;
  status?: PageStatus;
  featuredImage?: string | null;
  styleClasses?: string | null;
  sortOrder?: number;
}

export interface PageI18nRequest {
  language?: Language;
  urlPath?: string | null;
  title?: string | null;
  subtitle?: string | null;
  metaTitle?: string | null;
  metaDescription?: string | null;
  description?: string | null;
  descriptionHtml?: string | null;
  status?: PageStatus;
  scheduledAt?: string | null;
}

export interface PublishPageI18nRequest {
  scheduledAt?: string | null;
}

// API Response types
export interface ApiResponse<T> {
  data: T;
  success: boolean;
  message?: string;
}

export interface PaginatedResponse<T> {
  data: T[];
  total: number;
  page: number;
  pageSize: number;
}

// Validation schemas
export interface PageValidationErrors {
  title?: string;
  slug?: string;
  language?: string;
  tenantId?: string;
}

