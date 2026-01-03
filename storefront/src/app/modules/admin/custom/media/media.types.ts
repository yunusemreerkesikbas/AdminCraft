import { SpaDialogData } from '@shared/components/spa-dialog-base/spa-dialog-base.types';
import { Language } from '@shared/types/common.types';

export type { Language };

export enum MediaStatus {
    PROCESSING = 'PROCESSING',
    ACTIVE = 'ACTIVE',
    ARCHIVED = 'ARCHIVED',
    FAILED = 'FAILED'
}

export enum StorageProvider {
    LOCAL = 'LOCAL',
    S3 = 'S3',
    AZURE = 'AZURE',
    CLOUDINARY = 'CLOUDINARY'
}

export enum CropMode {
    FIT = 'FIT',
    FILL = 'FILL',
    COVER = 'COVER'
}

export enum MediaType {
    IMAGE = 'IMAGE',
    VIDEO = 'VIDEO',
    AUDIO = 'AUDIO',
    DOCUMENT = 'DOCUMENT',
    OTHER = 'OTHER'
}

export interface Media {
    id: number;
    uuid: string;
    uid: string;
    originalName: string;
    fileName: string;
    filePath: string;
    mimeType: string;
    fileExtension?: string;
    fileSize: number;
    width?: number;
    height?: number;
    duration?: number;
    tags?: string[];
    isPublic: boolean;
    uploadedBy?: number;
    uploaderName?: string;
    storageProvider: StorageProvider;
    externalUrl?: string;
    status: MediaStatus;
    usageCount: number;
    createdAt: string;
    updatedAt?: string;
    url?: string;
    type?: MediaType;
    fileType: string;
    fileSizeFormatted: string;
    formats?: MediaFormat[];
    altText?: string;
    dimensions?: string;
    publicUrl?: string;
}

export interface MediaI18n {
    id: number;
    mediaId: number;
    language: Language;
    altText?: string;
    title?: string;
    description?: string;
}

export interface MediaFormat {
    id: number;
    uuid: string;
    uid: string;
    code: string;
    name: string;
    width: number;
    height: number;
    quality: number;
    cropMode: CropMode;
    isSystem: boolean;
}

export interface MediaContainer {
    id: number;
    uid: string;
    code: string;
    masterMediaId: number;
    variants: MediaVariantResponse[];
    createdAt?: string;
    updatedAt?: string;
}

/** Media format variant - matches backend MediaContainerResponse.MediaVariantResponse */
export interface MediaVariantResponse {
    formatId: number;
    formatCode: string;
    formatName: string;
    mediaId: number;
    mediaUrl: string;
    width: number;
    height: number;
}

export interface MediaResponse extends Media {
    translations?: Record<Language, MediaI18n>;
}

export interface MediaDetailResponse extends MediaResponse {
    container?: MediaContainer;
}

export interface UploadMediaRequest {
    file: File;
}

export interface UploadMediaCompositeRequest {
    file: File;
    translations: Record<Language, MediaI18nRequest>;
}

export interface UpdateMediaRequest {
    originalName?: string;
    tags?: string[];
    isPublic?: boolean;
}

export interface UpdateMediaCompositeRequest extends UpdateMediaRequest {
    translations: Record<Language, MediaI18nRequest>;
}

export interface MediaI18nRequest {
    altText?: string;
    title?: string;
    description?: string;
}

export interface CreateFormatRequest {
    code: string;
    name: string;
    width: number;
    height: number;
    quality?: number;
    cropMode?: CropMode;
}

export interface MediaPagination {
    length: number;
    size: number;
    page: number;
    lastPage: number;
    startIndex: number;
    endIndex: number;
}

export interface MediaUploadDialogData extends SpaDialogData {
    languages: Language[];
}

export interface MediaDetailDialogData extends SpaDialogData {
    mode: 'view' | 'edit' | 'create';
    media: Media;
    languages: Language[];
}

export interface MediaPickerDialogData extends SpaDialogData {
    mode: 'single' | 'multiple';
    allowedTypes?: string[];
}
