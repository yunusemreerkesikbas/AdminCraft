import { HttpClient, HttpHeaders } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Content, ContentPagination, CreateContentRequest, UpdateContentRequest, ContentStatus, ContentType } from './content.types';
import { BehaviorSubject, Observable, tap, switchMap, map } from 'rxjs';
import { SPA_ENDPOINTS_CONFIG, resolveEndpoint } from '@modules/admin/api-endpoints';
import { environment } from '@environments/environment';

@Injectable({ providedIn: 'root' })
export class ContentService {
    private _httpClient = inject(HttpClient);
    private readonly apiUrl = environment.apiBaseUrl;

    private _contents: BehaviorSubject<Content[]> = new BehaviorSubject<Content[]>([]);
    private _pagination: BehaviorSubject<ContentPagination | null> = new BehaviorSubject<ContentPagination | null>(null);
    private _contentTypes: BehaviorSubject<ContentType[]> = new BehaviorSubject<ContentType[]>([]);

    // -----------------------------------------------------------------------------------------------------
    // @ Accessors
    // -----------------------------------------------------------------------------------------------------

    /**
     * Getter for contents
     */
    get contents$(): Observable<Content[]> {
        return this._contents.asObservable();
    }

    /**
     * Getter for pagination
     */
    get pagination$(): Observable<ContentPagination | null> {
        return this._pagination.asObservable();
    }

    /**
     * Getter for content types
     */
    get contentTypes$(): Observable<ContentType[]> {
        return this._contentTypes.asObservable();
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Public methods
    // -----------------------------------------------------------------------------------------------------

    /**
     * Get contents
     */
    getContents(page: number = 0, size: number = 10, sort: string = 'title', order: 'asc' | 'desc' = 'asc', search: string = ''): Observable<{ pagination: ContentPagination; contents: Content[] }> {
        const headers = new HttpHeaders({
            'Content-Type': 'application/json',
            'Accept-Language': 'tr',
            'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
        });

        const url = `${this.apiUrl}/${SPA_ENDPOINTS_CONFIG.contents}`;
        return this._httpClient.get<any>(url, { headers }).pipe(
            map((response) => {
                let contents: Content[] = [];
                
                if (response.result === 'SUCCESS' && response.data) {
                    contents = response.data.map((item: any) => ({
                        id: item.id,
                        title: item.title,
                        slug: item.slug,
                        data: item.data,
                        status: item.status,
                        language: item.language,
                        parentContentId: item.parentContentId,
                        contentTypeId: item.contentTypeId,
                        contentTypeName: item.contentTypeName,
                        tenantId: item.tenantId,
                        metaTitle: item.metaTitle,
                        metaDescription: item.metaDescription,
                        createdAt: item.createdAt,
                        updatedAt: item.updatedAt,
                        publishedAt: item.publishedAt,
                        authorId: item.authorId,
                        authorName: item.authorName
                    }));

                    // Client-side filtering and search
                    if (search) {
                        const searchLower = search.toLowerCase();
                        contents = contents.filter(content => 
                            content.title.toLowerCase().includes(searchLower) ||
                            content.slug.toLowerCase().includes(searchLower)
                        );
                    }

                    // Client-side sorting
                    contents.sort((a, b) => {
                        const aValue = a[sort] || '';
                        const bValue = b[sort] || '';
                        const comparison = aValue.toString().localeCompare(bValue.toString());
                        return order === 'desc' ? -comparison : comparison;
                    });
                }

                // Client-side pagination
                const totalLength = contents.length;
                const startIndex = page * size;
                const endIndex = Math.min(startIndex + size, totalLength);
                const paginatedContents = contents.slice(startIndex, endIndex);

                const pagination: ContentPagination = {
                    length: totalLength,
                    size: size,
                    page: page,
                    lastPage: Math.ceil(totalLength / size) - 1,
                    startIndex: startIndex,
                    endIndex: endIndex - 1
                };

                // Update internal state
                this._contents.next(paginatedContents);
                this._pagination.next(pagination);

                return { pagination, contents: paginatedContents };
            })
        );
    }

    /**
     * Get content by id
     */
    getContentById(id: number): Observable<Content> {
        const headers = new HttpHeaders({
            'Content-Type': 'application/json',
            'Accept-Language': 'tr',
            'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
        });

        const url = `${this.apiUrl}/${resolveEndpoint(SPA_ENDPOINTS_CONFIG.contentById, { id })}`;
        return this._httpClient.get<any>(url, { headers }).pipe(
            map((response) => {
                if (response.result === 'SUCCESS' && response.data) {
                    const item = response.data;
                    return {
                        id: item.id,
                        title: item.title,
                        slug: item.slug,
                        data: item.data,
                        status: item.status,
                        language: item.language,
                        parentContentId: item.parentContentId,
                        contentTypeId: item.contentTypeId,
                        contentTypeName: item.contentTypeName,
                        tenantId: item.tenantId,
                        metaTitle: item.metaTitle,
                        metaDescription: item.metaDescription,
                        createdAt: item.createdAt,
                        updatedAt: item.updatedAt,
                        publishedAt: item.publishedAt,
                        authorId: item.authorId,
                        authorName: item.authorName
                    };
                }
                throw new Error('Content not found');
            })
        );
    }

    /**
     * Create content
     */
    createContent(content: CreateContentRequest): Observable<Content> {
        const headers = new HttpHeaders({
            'Content-Type': 'application/json',
            'Accept-Language': 'tr',
            'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
        });

        const url = `${this.apiUrl}/${SPA_ENDPOINTS_CONFIG.contents}`;
        return this._httpClient.post<any>(url, content, { headers }).pipe(
            map((response) => {
                if (response.result === 'SUCCESS' && response.data) {
                    return response.data;
                }
                throw new Error('Failed to create content');
            }),
            tap(() => {
                // Refresh the contents list
                this.getContents().subscribe();
            })
        );
    }

    /**
     * Update content
     */
    updateContent(id: number, content: UpdateContentRequest): Observable<Content> {
        const headers = new HttpHeaders({
            'Content-Type': 'application/json',
            'Accept-Language': 'tr',
            'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
        });

        const url = `${this.apiUrl}/${resolveEndpoint(SPA_ENDPOINTS_CONFIG.contentById, { id })}`;
        return this._httpClient.put<any>(url, content, { headers }).pipe(
            map((response) => response.data),
            tap(() => {
                // Refresh the contents list
                this.getContents().subscribe();
            })
        );
    }

    /**
     * Delete content
     */
    deleteContent(id: number): Observable<boolean> {
        const headers = new HttpHeaders({
            'Content-Type': 'application/json',
            'Accept-Language': 'tr',
            'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
        });

        const url = `${this.apiUrl}/${resolveEndpoint(SPA_ENDPOINTS_CONFIG.contentById, { id })}`;
        return this._httpClient.delete<any>(url, { headers }).pipe(
            map((response) => response.result === 'SUCCESS'),
            tap(() => {
                // Refresh the contents list
                this.getContents().subscribe();
            })
        );
    }

    /**
     * Publish content
     */
    publishContent(id: number): Observable<Content> {
        const headers = new HttpHeaders({
            'Content-Type': 'application/json',
            'Accept-Language': 'tr',
            'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
        });

        const url = `${this.apiUrl}/${resolveEndpoint(SPA_ENDPOINTS_CONFIG.contentPublish, { id })}`;
        return this._httpClient.post<any>(url, {}, { headers }).pipe(
            map((response) => response.data),
            tap(() => {
                // Refresh the contents list
                this.getContents().subscribe();
            })
        );
    }

    /**
     * Archive content
     */
    archiveContent(id: number): Observable<Content> {
        const headers = new HttpHeaders({
            'Content-Type': 'application/json',
            'Accept-Language': 'tr',
            'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
        });

        const url = `${this.apiUrl}/${resolveEndpoint(SPA_ENDPOINTS_CONFIG.contentArchive, { id })}`;
        return this._httpClient.post<any>(url, {}, { headers }).pipe(
            map((response) => response.data),
            tap(() => {
                // Refresh the contents list
                this.getContents().subscribe();
            })
        );
    }

    /**
     * Get content types
     */
    getContentTypes(): Observable<ContentType[]> {
        const headers = new HttpHeaders({
            'Content-Type': 'application/json',
            'Accept-Language': 'tr',
            'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
        });

        const url = `${this.apiUrl}/${SPA_ENDPOINTS_CONFIG.contentTypes}`;
        return this._httpClient.get<any>(url, { headers }).pipe(
            map((response) => {
                if (response.result === 'SUCCESS' && response.data) {
                    const contentTypes = response.data.map((item: any) => ({
                        id: item.id,
                        name: item.name,
                        displayName: item.displayName,
                        fields: item.fields,
                        tenantId: item.tenantId,
                        supportsMultiLanguage: item.supportsMultiLanguage,
                        createdAt: item.createdAt,
                        updatedAt: item.updatedAt
                    }));
                    
                    this._contentTypes.next(contentTypes);
                    return contentTypes;
                }
                return [];
            })
        );
    }
}