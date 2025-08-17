import { inject, Injectable } from '@angular/core';
import { MediaFile, MediaPagination, UploadMediaRequest, UpdateMediaRequest, MediaType } from './media.types';
import { BehaviorSubject, Observable, tap, switchMap, map } from 'rxjs';
import { ApiClientService } from '@core/api/api-client.service';

@Injectable({ providedIn: 'root' })
export class MediaService {
    private readonly _apiClient = inject(ApiClientService);

    private _mediaFiles: BehaviorSubject<MediaFile[]> = new BehaviorSubject<MediaFile[]>([]);
    private _pagination: BehaviorSubject<MediaPagination | null> = new BehaviorSubject<MediaPagination | null>(null);

    // -----------------------------------------------------------------------------------------------------
    // @ Accessors
    // -----------------------------------------------------------------------------------------------------

    /**
     * Getter for media files
     */
    get mediaFiles$(): Observable<MediaFile[]> {
        return this._mediaFiles.asObservable();
    }

    /**
     * Getter for pagination
     */
    get pagination$(): Observable<MediaPagination | null> {
        return this._pagination.asObservable();
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Public methods
    // -----------------------------------------------------------------------------------------------------

    /**
     * Get media files
     */
    getMediaFiles(page: number = 0, size: number = 10, sort: string = 'originalName', order: 'asc' | 'desc' = 'asc', search: string = '', type?: MediaType): Observable<{ pagination: MediaPagination; mediaFiles: MediaFile[] }> {
        const queryParams: Record<string, any> = {};
        if (type) {
            queryParams.type = type;
        }

        return this._apiClient.get<any>('media', undefined, queryParams).pipe(
            map((response) => {
                let mediaFiles: MediaFile[] = [];
                
                if (response.result === 'SUCCESS' && response.data) {
                    mediaFiles = response.data.map((item: any) => ({
                        id: item.id,
                        originalName: item.originalName,
                        fileName: item.fileName,
                        filePath: item.filePath,
                        mimeType: item.mimeType,
                        fileSize: item.fileSize,
                        width: item.width,
                        height: item.height,
                        altTextTr: item.altTextTr,
                        altTextEn: item.altTextEn,
                        tenantId: item.tenantId,
                        uploadedBy: item.uploadedBy,
                        uploaderName: item.uploaderName,
                        createdAt: item.createdAt,
                        updatedAt: item.updatedAt
                    }));

                    // Client-side filtering and search
                    if (search) {
                        const searchLower = search.toLowerCase();
                        mediaFiles = mediaFiles.filter(media => 
                            media.originalName.toLowerCase().includes(searchLower) ||
                            media.fileName.toLowerCase().includes(searchLower) ||
                            media.mimeType.toLowerCase().includes(searchLower)
                        );
                    }

                    // Client-side sorting
                    mediaFiles.sort((a, b) => {
                        const aValue = a[sort] || '';
                        const bValue = b[sort] || '';
                        const comparison = aValue.toString().localeCompare(bValue.toString());
                        return order === 'desc' ? -comparison : comparison;
                    });
                }

                // Client-side pagination
                const totalLength = mediaFiles.length;
                const startIndex = page * size;
                const endIndex = Math.min(startIndex + size, totalLength);
                const paginatedMediaFiles = mediaFiles.slice(startIndex, endIndex);

                const pagination: MediaPagination = {
                    length: totalLength,
                    size: size,
                    page: page,
                    lastPage: Math.ceil(totalLength / size) - 1,
                    startIndex: startIndex,
                    endIndex: endIndex - 1
                };

                // Update internal state
                this._mediaFiles.next(paginatedMediaFiles);
                this._pagination.next(pagination);

                return { pagination, mediaFiles: paginatedMediaFiles };
            })
        );
    }

    /**
     * Get media file by id
     */
    getMediaFileById(id: number): Observable<MediaFile> {
        return this._apiClient.get<any>('mediaById', { id }).pipe(
            map((response) => {
                if (response.result === 'SUCCESS' && response.data) {
                    const item = response.data;
                    return {
                        id: item.id,
                        originalName: item.originalName,
                        fileName: item.fileName,
                        filePath: item.filePath,
                        mimeType: item.mimeType,
                        fileSize: item.fileSize,
                        width: item.width,
                        height: item.height,
                        altTextTr: item.altTextTr,
                        altTextEn: item.altTextEn,
                        tenantId: item.tenantId,
                        uploadedBy: item.uploadedBy,
                        uploaderName: item.uploaderName,
                        createdAt: item.createdAt,
                        updatedAt: item.updatedAt
                    };
                }
                throw new Error('Media file not found');
            })
        );
    }

    /**
     * Upload media file
     */
    uploadMediaFile(uploadRequest: UploadMediaRequest): Observable<MediaFile> {
        const formData = new FormData();
        formData.append('file', uploadRequest.file);
        if (uploadRequest.altTextTr) {
            formData.append('altTextTr', uploadRequest.altTextTr);
        }
        if (uploadRequest.altTextEn) {
            formData.append('altTextEn', uploadRequest.altTextEn);
        }

        return this._apiClient.upload<any>('mediaUpload', formData).pipe(
            map((response) => {
                if (response.result === 'SUCCESS' && response.data) {
                    return response.data;
                }
                throw new Error('Failed to upload media file');
            }),
            tap(() => {
                // Refresh the media files list
                this.getMediaFiles().subscribe();
            })
        );
    }

    /**
     * Update media file
     */
    updateMediaFile(id: number, media: UpdateMediaRequest): Observable<MediaFile> {
        return this._apiClient.put<any>('mediaById', media, { id }).pipe(
            map((response) => response.data),
            tap(() => {
                // Refresh the media files list
                this.getMediaFiles().subscribe();
            })
        );
    }

    /**
     * Delete media file
     */
    deleteMediaFile(id: number): Observable<boolean> {
        return this._apiClient.delete<any>('mediaById', { id }).pipe(
            map((response) => response.result === 'SUCCESS'),
            tap(() => {
                // Refresh the media files list
                this.getMediaFiles().subscribe();
            })
        );
    }

    /**
     * Get media type from mime type
     */
    getMediaType(mimeType: string): MediaType {
        if (mimeType.startsWith('image/')) {
            return MediaType.IMAGE;
        } else if (mimeType.startsWith('video/')) {
            return MediaType.VIDEO;
        } else if (mimeType.startsWith('audio/')) {
            return MediaType.AUDIO;
        } else if (mimeType.includes('pdf') || mimeType.includes('document') || mimeType.includes('text')) {
            return MediaType.DOCUMENT;
        } else {
            return MediaType.OTHER;
        }
    }

    /**
     * Format file size
     */
    formatFileSize(bytes: number): string {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }
}