export interface Category {
    id: number;
    uid: string;
    code: string;
    parentId?: number;
    sortOrder: number;
    isVisible: boolean;
    createdAt?: string;
    updatedAt?: string;
    name?: string; 
    description?: string;
    children?: Category[];
}

export interface CategoryI18n {
    id?: number;
    language: string;
    name: string;
    description?: string;
}

export interface CategoryCompositeRequest {
    code: string;
    parentId?: number;
    sortOrder?: number;
    isVisible?: boolean;
    translations: Record<string, CategoryI18nRequest>;
}

export interface CategoryI18nRequest {
    name: string;
    description?: string;
}

export interface CategoryTreeResponse {
    id: number;
    uid: string;
    code: string;
    name: string;
    parentId?: number;
    sortOrder: number;
    isVisible: boolean;
    children: CategoryTreeResponse[];
}
