import { ResponsiveMediaResponse } from "../../media/media.types";
import { ProductFieldType } from "./product-type.types";

export interface Product {
    id: number;
    uid: string;
    productTypeId: number;
    productTypeName: string;
    sku: string;
    basePrice: number;
    currency: string;
    status: ProductStatus;
    isVisible: boolean;
    createdAt: string;
    updatedAt: string;
    responsiveMedia?: ResponsiveMediaResponse;
    translations?: Record<string, ProductI18n>;
    attributes?: ProductAttributeResponse[];
    categories?: ProductCategoryResponse[];
    gallery?: ProductMediaResponse[];
}

export type ProductStatus = 'DRAFT' | 'PUBLISHED';

export interface ProductI18n {
    id?: number;
    language: string;
    name: string;
    shortDescription?: string;
    description?: string;
    seoTitle?: string;
    seoDescription?: string;
}

export interface ProductAttributeResponse {
    id: number;
    code: string;
    name: string;
    fieldType: ProductFieldType;
    value: any;
}

export interface ProductCategoryResponse {
    id: number;
    uid: string;
    code: string;
    name: string;
    isPrimary: boolean;
    parentId?: number;
}

export interface ProductMediaResponse {
    id: number;
    mediaId: number;
    mediaType: 'GALLERY' | 'PRIMARY' | 'THUMBNAIL';
    sortOrder: number;
    media: any;
}

export interface ProductCompositeRequest {
    productTypeId: number;
    sku: string;
    basePrice: number;
    currency: string;
    status: ProductStatus;
    isVisible: boolean;
    responsiveMediaId?: number;
    
    translations: Record<string, ProductI18nRequest>;
    attributes: Record<string, any>;
    categoryIds: number[];
    primaryCategoryId: number;
    galleryMediaIds: number[];
}

export interface ProductI18nRequest {
    name: string;
    shortDescription?: string;
    description?: string;
    seoTitle?: string;
    seoDescription?: string;
}

export interface ProductListItemResponse {
    id: number;
    uid: string;
    sku: string;
    status: ProductStatus;
    isVisible: boolean;
    basePrice: number;
    currency: string;
    name: string;
    productTypeName: string;
    thumbnailUrl?: string;
    primaryCategoryName?: string;
    createdAt: string;
    updatedAt: string;
}
