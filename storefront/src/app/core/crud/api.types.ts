export interface ApiResponse<T> {
  data: T;
  result: 'SUCCESS' | 'ERROR';
  message?: string;
  /** Stable machine-readable code on some errors (e.g. media upload validation). */
  errorCode?: string;
  code?: number;
  success?: boolean;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
  empty: boolean;
}
export interface SortOption {
  code: string;
  labelKey: string;
  isDefault?: boolean;
}

export interface CurrentSort {
  field: string;
  direction: 'asc' | 'desc';
  code: string;
}

export interface SortConfig {
  currentSort: CurrentSort | null;
  availableSorts: SortOption[];
}
export interface PageWithSort<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  sortConfig?: SortConfig;
}

export interface PageRequest {
  page?: number;
  size?: number;
  sort?: string;
}

export interface SearchRequest extends PageRequest {
  search?: string;
}

export interface CrudEntity {
  id: number;
  uid?: string;
}

export interface CrudOptions {
  endpoint: string;
  unwrapData?: boolean;
}
