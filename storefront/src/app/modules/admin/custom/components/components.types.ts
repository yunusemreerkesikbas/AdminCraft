export type ComponentType =
  | 'NAVBAR'
  | 'LOGO'
  | 'CTA'
  | 'BRANDS'
  | 'FAQ'
  | 'BREADCRUMB';

export type ComponentStatus = 'ACTIVE' | 'INACTIVE';

export interface ComponentTranslation {
  title?: string;
  subtitle?: string;
  data?: string; // JSON string per type
}

export interface ComponentRequest {
  tenantId: number;
  type: ComponentType;
  key: string;
  status?: ComponentStatus;
  visible?: boolean;
  sortOrder?: number;
  translations: Record<string, ComponentTranslation>;
}

export interface ComponentResponse {
  id: number;
  tenantId: number;
  type: ComponentType;
  key: string;
  status: ComponentStatus;
  visible: boolean;
  sortOrder: number;
  tr?: ComponentTranslation;
  en?: ComponentTranslation;
}

export interface SiteComponentResponse {
  id: number;
  type: ComponentType;
  key: string;
  sortOrder: number;
  translation: ComponentTranslation;
}

export interface ApiResponse<T> {
  result: 'SUCCESS' | 'ERROR';
  message?: string;
  data: T;
}


