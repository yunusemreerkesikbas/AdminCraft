export interface PageDto {
  id: number;
  tenantId: number;
  title: string;
  slug: string;
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED' | 'SCHEDULED';
  language: 'TR' | 'EN';
  categoryId?: number | null;
  metaTitle?: string | null;
  metaDescription?: string | null;
  canonicalUrl?: string | null;
  subtitle?: string | null;
  styleClasses?: string | null;
  description?: string | null;
  descriptionHtml?: string | null;
  featuredImage?: string | null;
  publishedAt?: string | null;
  scheduledAt?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface CreatePageRequest {
  tenantId: number;
  title: string;
  slug: string;
  language: 'TR' | 'EN';
  categoryId?: number | null;
  metaTitle?: string | null;
  metaDescription?: string | null;
  canonicalUrl?: string | null;
  subtitle?: string | null;
  styleClasses?: string | null;
  description?: string | null;
  featuredImage?: string | null;
}

export interface UpdatePageRequest extends CreatePageRequest {
  id: number;
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

