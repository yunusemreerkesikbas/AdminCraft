import { inject, Injectable } from '@angular/core';
import { ApiClientService } from '@core/api/api-client.service';
import { map, Observable, Subject } from 'rxjs';
import {
  ApiResponse,
  CreateCategoryRequest,
  CreatePageRequest,
  Language,
  MoveCategoryRequest,
  PageBlockDto,
  PageCategoryDto,
  PageCategoryTreeNode,
  PageDto,
  PageI18nDto,
  PageI18nRequest,
  PageSectionDto,
  PageWithI18nDto,
  PublishPageI18nRequest,
  ReorderCategoryRequest,
  UpdateCategoryRequest,
  UpdatePageRequest,
} from './page-builder.types';

@Injectable({ providedIn: 'root' })
export class PageBuilderService {
  #api = inject(ApiClientService);
  #createRequested = new Subject<void>();
  readonly createRequested$ = this.#createRequested.asObservable();

  listPages(): Observable<PageDto[]> {
    return this.#api.get<ApiResponse<PageDto[]>>('pages').pipe(
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

  getPageWithI18n(id: number): Observable<PageWithI18nDto> {
    return this.#api.get<ApiResponse<PageWithI18nDto>>('pageWithI18n', { id }).pipe(map((r) => r.data));
  }

  createPage(req: CreatePageRequest): Observable<PageDto> {
    return this.#api.post<ApiResponse<PageDto>>('pages', req).pipe(map((r) => r.data));
  }

  updatePage(id: number, req: UpdatePageRequest): Observable<PageDto> {
    return this.#api.put<ApiResponse<PageDto>>('pageById', req, { id }).pipe(map((r) => r.data));
  }

  setPageAsHome(id: number): Observable<PageDto> {
    return this.#api.put<ApiResponse<PageDto>>('pageSetHome', {}, { id }).pipe(map((r) => r.data));
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

  getCategoryTree(rootId?: number | null, depth?: number): Observable<PageCategoryTreeNode[]> {
    const qp: Record<string, any> = {
      ...(rootId !== undefined && { rootId }),
      ...(depth !== undefined && { depth }),
    };
    return this.#api
      .custom<ApiResponse<PageCategoryTreeNode[]>>('GET', 'pageCategoryTree', { queryParams: qp, includeAuth: true })
      .pipe(map((r) => r.data || []));
  }

  getCategoryChildren(parentId: number | null): Observable<PageCategoryDto[]> {
    const qp: Record<string, any> = { parentId };
    return this.#api.get<ApiResponse<PageCategoryDto[]>>('pageCategoryChildren', undefined, qp).pipe(map((r) => r.data || []));
  }

  moveCategory(req: MoveCategoryRequest): Observable<void> {
    return this.#api
      .put<ApiResponse<void>>('pageCategoryMove', { newParentId: req.newParentId }, { id: req.id })
      .pipe(map(() => undefined));
  }

  reorderCategories(req: ReorderCategoryRequest): Observable<void> {
    return this.#api
      .put<ApiResponse<void>>('pageCategoryReorder', { parentId: req.parentId, orderedIds: req.orderedIds })
      .pipe(map(() => undefined));
  }

  listSections(pageId: number): Observable<PageSectionDto[]> {
    return this.#api
      .get<ApiResponse<PageSectionDto[]>>('pageBuilderSections', undefined, { pageId })
      .pipe(map((r) => r.data || []));
  }

  addSection(pageId: number, type?: string, displayOrder?: number, data?: string): Observable<PageSectionDto> {
    return this.#api
      .post<ApiResponse<PageSectionDto>>('pageBuilderSections', { pageId, type, displayOrder, data })
      .pipe(map((r) => r.data));
  }

  updateSection(id: number, payload: Partial<PageSectionDto>): Observable<PageSectionDto> {
    return this.#api
      .put<ApiResponse<PageSectionDto>>('pageBuilderSections', { id, ...payload })
      .pipe(map((r) => r.data));
  }

  deleteSection(id: number): Observable<void> {
    return this.#api
      .delete<ApiResponse<void>>('pageBuilderSectionById', { id })
      .pipe(map(() => undefined));
  }

  listBlocks(sectionId: number): Observable<PageBlockDto[]> {
    return this.#api
      .get<ApiResponse<PageBlockDto[]>>('pageBuilderBlocks', undefined, { sectionId })
      .pipe(map((r) => r.data || []));
  }

  addBlock(sectionId: number, type?: string, displayOrder?: number, data?: string): Observable<PageBlockDto> {
    return this.#api
      .post<ApiResponse<PageBlockDto>>('pageBuilderBlocks', { sectionId, type, displayOrder, data })
      .pipe(map((r) => r.data));
  }

  updateBlock(id: number, payload: Partial<PageBlockDto>): Observable<PageBlockDto> {
    return this.#api
      .put<ApiResponse<PageBlockDto>>('pageBuilderBlocks', { id, ...payload })
      .pipe(map((r) => r.data));
  }

  deleteBlock(id: number): Observable<void> {
    return this.#api
      .delete<ApiResponse<void>>('pageBuilderBlockById', { id })
      .pipe(map(() => undefined));
  }
}

