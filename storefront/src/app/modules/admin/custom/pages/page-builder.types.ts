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

export interface CreateCategoryRequest {
  tenantId: number;
  name: string;
  slug: string;
  parentId?: number | null;
}

export interface UpdateCategoryRequest extends CreateCategoryRequest {
  id: number;
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

