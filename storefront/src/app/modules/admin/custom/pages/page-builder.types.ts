import { Language } from '@shared/types/common.types';
export { Language };
export type PageStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED' | 'SCHEDULED';
export type RobotTag = 'INDEX_FOLLOW' | 'NOINDEX_FOLLOW' | 'INDEX_NOFOLLOW' | 'NOINDEX_NOFOLLOW';

// PageDto: Add dates
export interface PageDto {
  id: number;
  uuid: string;
  uid: string;
  tenantId: number;
  templateId?: number | null;
  status: PageStatus;
  styleClasses?: string | null;
  robotTag: RobotTag;

  publishedAt?: string | null;
  scheduledAt?: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface PageI18nDto {
  id: number;
  uuid: string;
  uid: string;
  tenantId: number;

  name: string; // Mapped from title
  title: string;
  canonicalUrl: string; // Mapped from urlPath
  description?: string | null;

  updatedAt: string;
  // Removed: publishedAt, scheduledAt (moved to Page)
}

export interface PageListDto {
  id: number;
  uuid: string;
  uid: string;
  tenantId: number;
  templateId?: number | null;
  status: PageStatus;
  styleClasses?: string | null;
  robotTag: RobotTag;

  createdAt: string;
  updatedAt: string;
  translations: {
    [key in Language]?: boolean;
  };
}

export interface PageDetailDto { // Mapping PageDetailResponse
  id: number;
  uuid: string;
  uid: string;
  tenantId: number;
  templateId?: number | null;
  templateUid?: string | null;
  status: PageStatus;
  styleClasses?: string | null;
  robotTag: RobotTag;

  publishedAt?: string | null;
  scheduledAt?: string | null;
  createdAt: string;
  updatedAt: string;
  translations: {
    [key in Language]?: PageI18nDto;
  };
}

export interface CreatePageRequest {
  templateId?: number | null;
  status?: PageStatus;
  styleClasses?: string | null;
  robotTag?: RobotTag;
  uid?: string;
}

export interface UpdatePageRequest {
  id: number;
  templateId?: number | null;
  status?: PageStatus;
  styleClasses?: string | null;
  robotTag?: RobotTag;
}

export interface PageI18nRequest {
  language?: Language;
  canonicalUrl?: string | null;
  title?: string | null;
  description?: string | null;
  status?: PageStatus;
  // scheduledAt removed from I18n request (moved to Page level or handled by publish)
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

export interface PageValidationErrors {
  title?: string;
  slug?: string;
  language?: string;
  tenantId?: string;
}

// Composite Request/Response types
export interface PageI18nCompositeRequest {
  canonicalUrl?: string | null;
  title?: string | null;
  description?: string | null;
  status?: PageStatus;
}

export interface CreatePageCompositeRequest {
  templateId?: number | null;
  status?: PageStatus;
  styleClasses?: string | null;
  robotTag?: RobotTag;
  uid?: string;
  translations: Record<string, PageI18nCompositeRequest>;
}

export interface UpdatePageCompositeRequest {
  uid?: string | null;
  templateId?: number | null;
  status?: PageStatus;
  styleClasses?: string | null;
  robotTag?: RobotTag;
  translations?: Record<string, PageI18nCompositeRequest>;
}
