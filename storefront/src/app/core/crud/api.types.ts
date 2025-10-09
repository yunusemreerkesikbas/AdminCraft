export interface ApiResponse<T> {
  data: T;
  success: boolean;
  message?: string;
  result?: string;
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

export interface QueryParams {
  [key: string]: string | number | boolean | undefined;
}

