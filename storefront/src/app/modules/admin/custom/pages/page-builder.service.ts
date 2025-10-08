import { inject, Injectable } from '@angular/core';
import { ApiClientService } from '@core/api/api-client.service';
import { map, Observable, Subject } from 'rxjs';
import {
    ApiResponse,
    CreateCategoryRequest,
    CreatePageRequest,
    Language,
    PageCategoryDto,
    PageDetailDto,
    PageDto,
    PageI18nDto,
    PageI18nRequest,
    PageListDto,
    PublishPageI18nRequest,
    UpdateCategoryRequest,
    UpdatePageRequest
} from './page-builder.types';

@Injectable({ providedIn: 'root' })
export class PageBuilderService {
  #api = inject(ApiClientService);
  #createRequested = new Subject<void>();
  readonly createRequested$ = this.#createRequested.asObservable();

  listPages(): Observable<PageListDto[]> {
    return this.#api.get<ApiResponse<PageListDto[]>>('pages').pipe(
      map((r) => {
        const data = r?.data ?? [];
        return Array.isArray(data) ? data : [];
      })
    );
  }

  requestCreate(): void {
    this.#createRequested.next();
  }

  getPageById(id: number): Observable<PageDto> {
    return this.#api.get<ApiResponse<PageDto>>('pageById', { id }).pipe(map((r) => r.data));
  }

  getPageDetail(id: number): Observable<PageDetailDto> {
    return this.#api
      .get<ApiResponse<PageDetailDto>>('pageById', { id }, { include: 'translations' })
      .pipe(map((r) => r.data));
  }

  createPage(req: CreatePageRequest): Observable<PageDto> {
    return this.#api.post<ApiResponse<PageDto>>('pages', req).pipe(map((r) => r.data));
  }

  updatePage(id: number, req: UpdatePageRequest): Observable<PageDto> {
    return this.#api.put<ApiResponse<PageDto>>('pageById', req, { id }).pipe(map((r) => r.data));
  }

  deletePage(id: number): Observable<void> {
    return this.#api.delete<ApiResponse<void>>('pageById', { id }).pipe(map(() => undefined));
  }

  getPageI18n(pageId: number, language: Language): Observable<PageI18nDto> {
    return this.#api.get<ApiResponse<PageI18nDto>>('pageI18n', { pageId, language }).pipe(map((r) => r.data));
  }

  updatePageI18n(pageId: number, language: Language, req: PageI18nRequest): Observable<PageI18nDto> {
    return this.#api.put<ApiResponse<PageI18nDto>>('pageI18n', req, { pageId, language }).pipe(map((r) => r.data));
  }

  publishPageI18n(pageId: number, language: Language, req?: PublishPageI18nRequest): Observable<PageI18nDto> {
    return this.#api.post<ApiResponse<PageI18nDto>>('pageI18nPublish', req || {}, { pageId, language }).pipe(map((r) => r.data));
  }

  listCategories(parentId?: number): Observable<PageCategoryDto[]> {
    const qp: Record<string, any> = { ...(parentId !== undefined && { parentId }) };
    return this.#api.get<ApiResponse<PageCategoryDto[]>>('pageCategories', undefined, qp).pipe(map((r) => r.data || []));
  }

  getCategory(id: number): Observable<PageCategoryDto> {
    return this.#api.get<ApiResponse<PageCategoryDto>>('pageCategoryById', { id }).pipe(map((r) => r.data));
  }

  createCategory(req: CreateCategoryRequest): Observable<PageCategoryDto> {
    return this.#api.post<ApiResponse<PageCategoryDto>>('pageCategories', req).pipe(map((r) => r.data));
  }

  updateCategory(req: UpdateCategoryRequest): Observable<PageCategoryDto> {
    return this.#api.put<ApiResponse<PageCategoryDto>>('pageCategories', req).pipe(map((r) => r.data));
  }

  deleteCategory(id: number): Observable<void> {
    return this.#api.delete<ApiResponse<void>>('pageCategoryById', { id }).pipe(map(() => undefined));
  }

  

  // Removed non-CRUD and sections/blocks endpoints per refactor
}

