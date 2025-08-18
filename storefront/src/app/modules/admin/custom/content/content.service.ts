import { inject, Injectable } from '@angular/core';
import { Content, ContentPagination, CreateContentRequest, UpdateContentRequest, ContentStatus, ContentType } from './content.types';
import { BehaviorSubject, Observable, tap, switchMap, map } from 'rxjs';
import { ApiClientService } from '@core/api/api-client.service';

@Injectable({ providedIn: 'root' })
export class ContentService {
    private readonly _apiClient = inject(ApiClientService);

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
        return this._apiClient.get<any>('contents').pipe(
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
        return this._apiClient.get<any>('contentById', { id }).pipe(
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
        return this._apiClient.post<any>('contents', content).pipe(
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
        return this._apiClient.put<any>('contentById', content, { id }).pipe(
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
        return this._apiClient.delete<any>('contentById', { id }).pipe(
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
        return this._apiClient.post<any>('contentPublish', {}, { id }).pipe(
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
        return this._apiClient.post<any>('contentArchive', {}, { id }).pipe(
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
        return this._apiClient.get<any>('contentTypes').pipe(
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