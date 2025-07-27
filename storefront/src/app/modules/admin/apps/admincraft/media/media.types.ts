export interface MediaFile {
    id: number;
    originalName: string;
    fileName: string;
    filePath: string;
    mimeType: string;
    fileSize: number;
    width?: number;
    height?: number;
    altTextTr?: string;
    altTextEn?: string;
    tenantId: number;
    uploadedBy: number;
    uploaderName?: string;
    createdAt: string;
    updatedAt?: string;
}

export interface MediaPagination {
    length: number;
    size: number;
    page: number;
    lastPage: number;
    startIndex: number;
    endIndex: number;
}

export enum MediaType {
    IMAGE = 'IMAGE',
    VIDEO = 'VIDEO',
    AUDIO = 'AUDIO',
    DOCUMENT = 'DOCUMENT',
    OTHER = 'OTHER'
}

export interface UploadMediaRequest {
    file: File;
    altTextTr?: string;
    altTextEn?: string;
}

export interface UpdateMediaRequest {
    originalName?: string;
    altTextTr?: string;
    altTextEn?: string;
}