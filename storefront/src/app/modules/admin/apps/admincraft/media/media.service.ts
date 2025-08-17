import { HttpClient, HttpHeaders } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { MediaFile, MediaPagination, UploadMediaRequest, UpdateMediaRequest, MediaType } from './media.types';
import { BehaviorSubject, Observable, tap, switchMap, map } from 'rxjs';
import { SPA_ENDPOINTS_CONFIG, resolveEndpoint } from '@modules/admin/api-endpoints';
import { environment } from '@environments/environment';

@Injectable({ providedIn: 'root' })
export class MediaService {
    private _httpClient = inject(HttpClient);
    private readonly apiUrl = environment.apiBaseUrl;

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
        const headers = new HttpHeaders({
            'Content-Type': 'application/json',
            'Accept-Language': 'tr',
            'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
        });

        let url = `${this.apiUrl}/${SPA_ENDPOINTS_CONFIG.media}`;
        const params = new URLSearchParams();
        if (type) {
            params.append('type', type);
        }
        if (params.toString()) {
            url += `?${params.toString()}`;
        }

        return this._httpClient.get<any>(url, { headers }).pipe(
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
        const headers = new HttpHeaders({
            'Content-Type': 'application/json',
            'Accept-Language': 'tr',
            'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
        });

        const url = `${this.apiUrl}/${resolveEndpoint(SPA_ENDPOINTS_CONFIG.mediaById, { id })}`;
        return this._httpClient.get<any>(url, { headers }).pipe(
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

        const headers = new HttpHeaders({
            'Accept-Language': 'tr',
            'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
        });

        const url = `${this.apiUrl}/${SPA_ENDPOINTS_CONFIG.mediaUpload}`;
        return this._httpClient.post<any>(url, formData, { headers }).pipe(
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
        const headers = new HttpHeaders({
            'Content-Type': 'application/json',
            'Accept-Language': 'tr',
            'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
        });

        const url = `${this.apiUrl}/${resolveEndpoint(SPA_ENDPOINTS_CONFIG.mediaById, { id })}`;
        return this._httpClient.put<any>(url, media, { headers }).pipe(
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
        const headers = new HttpHeaders({
            'Content-Type': 'application/json',
            'Accept-Language': 'tr',
            'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
        });

        const url = `${this.apiUrl}/${resolveEndpoint(SPA_ENDPOINTS_CONFIG.mediaById, { id })}`;
        return this._httpClient.delete<any>(url, { headers }).pipe(
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