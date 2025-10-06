export type Language = 'TR' | 'EN' | 'ES' | 'RU' | 'AR';
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
  isHome: boolean;
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
  isHome: boolean;
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
  isHome: boolean;
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
  isHome?: boolean;
  sortOrder?: number;
}

export interface UpdatePageRequest {
  id: number;
  categoryId?: number | null;
  status?: PageStatus;
  featuredImage?: string | null;
  styleClasses?: string | null;
  isHome?: boolean;
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

export interface PageCategoryDto {
  id: number;
  tenantId: number;
  name: string;
  slug: string;
  parentId?: number | null;
}

export interface PageCategoryTreeNode {
  id: number;
  tenantId: number;
  name: string;
  slug: string;
  parentId?: number | null;
  level: number;
  path: string;
  sortOrder: number;
  children?: PageCategoryTreeNode[];
}

export interface CreateCategoryRequest {
  tenantId: number;
  name: string;
  slug: string;
  parentId?: number | null;
}

export interface UpdateCategoryRequest extends CreateCategoryRequest {
  id: number;
}

export interface MoveCategoryRequest {
  id: number;
  newParentId: number | null;
}

export interface ReorderCategoryRequest {
  parentId: number | null;
  orderedIds: number[];
}

export interface PageSectionDto {
  id: number;
  pageId: number;
  type?: string | null;
  displayOrder: number;
  data?: string | null;
}

export interface PageBlockDto {
  id: number;
  sectionId: number;
  type?: string | null;
  displayOrder: number;
  data?: string | null;
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

