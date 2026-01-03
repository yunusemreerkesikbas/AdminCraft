import { Injectable } from '@angular/core';
import { ApiResponse } from '@core/crud/api.types';
import { CrudEndpoints, CrudHttpService } from '@core/crud/crud-http.service';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import {
    Language,
    Media,
    MediaDetailResponse,
    MediaFormat,
    MediaI18n,
    MediaI18nRequest,
    MediaResponse,
    UpdateMediaCompositeRequest,
    UpdateMediaRequest
} from './media.types';

@Injectable({ providedIn: 'root' })
export class MediaService extends CrudHttpService<Media, FormData, UpdateMediaRequest> {

    protected override endpoints: CrudEndpoints = {
        list: 'media',
        getById: 'mediaById',
        create: 'mediaUpload',
        update: 'mediaById',
        delete: 'mediaById'
    };

    override list(): Observable<Media[]> {
        return this.api.get<ApiResponse<{ content: Media[] }>>(this.endpoints.list, undefined, {
            page: 0,
            size: 100
        }).pipe(
            map((response) => response.data.content)
        );
    }

    upload(file: File, uploadedBy: number): Observable<Media> {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('uploadedBy', uploadedBy.toString());
        return this.api.upload<ApiResponse<Media>>('mediaUpload', formData).pipe(
            map((response) => response.data)
        );
    }

    uploadComposite(file: File, uploadedBy: number, translations?: Record<Language, MediaI18nRequest>): Observable<MediaResponse> {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('uploadedBy', uploadedBy.toString());
        if (translations) {
            formData.append('translations', JSON.stringify(translations));
        }
        return this.api.upload<ApiResponse<MediaResponse>>('mediaComposite', formData).pipe(
            map((response) => response.data)
        );
    }

    getByUid(uid: string): Observable<Media> {
        return this.customGet<Media>('mediaByUid', { uid });
    }

    getDetails(id: number): Observable<MediaDetailResponse> {
        return this.customGet<MediaDetailResponse>('mediaDetail', { id });
    }

    updateComposite(id: number, request: UpdateMediaCompositeRequest): Observable<MediaResponse> {
        return this.customPut<MediaResponse>('mediaCompositeById', request, { id });
    }

    getI18n(mediaId: number, language: Language): Observable<MediaI18n> {
        return this.customGet<MediaI18n>('mediaI18n', { mediaId, language });
    }

    upsertI18n(mediaId: number, language: Language, request: MediaI18nRequest): Observable<MediaI18n> {
        return this.customPut<MediaI18n>('mediaI18n', request, { mediaId, language });
    }

    getFormats(): Observable<MediaFormat[]> {
        return this.customGet<MediaFormat[]>('mediaFormats');
    }

    getFormatById(id: number): Observable<MediaFormat> {
        return this.customGet<MediaFormat>('mediaFormatById', { id });
    }

    getCmsMedia(uid: string, format?: string): Observable<Media> {
        const queryParams = format ? { format } : undefined;
        return this.api.getPublic<ApiResponse<Media>>('cmsMedia', { uid }, queryParams).pipe(
            map((response) => response.data)
        );
    }
}
