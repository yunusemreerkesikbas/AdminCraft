import { Language } from '@shared/types/common.types';
export { Language };
export type PageStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED' | 'SCHEDULED';

export interface PageDto {
  id: number;
  uuid: string;
  uid: string;
  tenantId: number;
  categoryId?: number | null;
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
  categoryId?: number | null;
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
  categoryId?: number | null;
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
  categoryId?: number | null;
  status?: PageStatus;
  featuredImage?: string | null;
  styleClasses?: string | null;
  sortOrder?: number;
}

export interface UpdatePageRequest {
  id: number;
  categoryId?: number | null;
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

export interface PageCategoryListDto {
  id: number;
  uuid: string;
  uid: string;
  parentId?: number | null;
  active: boolean;
  styleClasses?: string | null;
  sortOrder: number;
  createdAt: string;
  updatedAt: string;
  translations: {
    [key in Language]?: boolean;
  };
}

export interface PageCategoryI18nDto {
  id: number;
  uuid: string;
  uid: string;
  language: Language;
  url?: string | null;
  title?: string | null;
  metaTitle?: string | null;
  metaDescription?: string | null;
  active: boolean;
  updatedAt: string;
  fallbackLanguage: boolean;
}

export interface PageCategoryDetailDto {
  id: number;
  uuid: string;
  uid: string;
  parentId?: number | null;
  active: boolean;
  styleClasses?: string | null;
  sortOrder: number;
  createdAt: string;
  updatedAt: string;
  translations: {
    [key in Language]?: PageCategoryI18nDto;
  };
  metadata: {
    translationCount: number;
    publishedTranslationCount: number;
  };
}

export interface CreateCategoryRequest {
  uid?: string | null;
  parentId?: number | null;
  active?: boolean;
  styleClasses?: string | null;
  sortOrder?: number;
}

export interface UpdateCategoryRequest {
  parentId?: number | null;
  active?: boolean;
  styleClasses?: string | null;
  sortOrder?: number;
}

export interface UpsertCategoryI18nRequest {
  url?: string | null;
  title?: string | null;
  metaTitle?: string | null;
  metaDescription?: string | null;
  active?: boolean;
}

export type PageCategoryDto = PageCategoryListDto;

// Removed Move/Reorder and Sections/Blocks types per refactor

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

