import { inject, Injectable } from '@angular/core';
import { ApiClientService } from '@core/api/api-client.service';
import { map, Observable, Subject } from 'rxjs';
import {
  ApiResponse,
  CreateCategoryRequest,
  CreatePageRequest,
  MoveCategoryRequest,
  PageBlockDto,
  PageCategoryDto,
  PageCategoryTreeNode,
  PageDto,
  PageSectionDto,
  ReorderCategoryRequest,
  UpdateCategoryRequest,
  UpdatePageRequest,
} from './page-builder.types';

@Injectable({ providedIn: 'root' })
export class PageBuilderService {
  #api = inject(ApiClientService);
  // UI bus for header actions
  #createRequested = new Subject<void>();
  readonly createRequested$ = this.#createRequested.asObservable();

  // Pages
  listPages(tenantId: number, language?: 'TR' | 'EN'): Observable<PageDto[]> {
    const qp: Record<string, any> = { tenantId, ...(language && { language }) };
    return this.#api.get<ApiResponse<PageDto[]>>('pages', undefined, qp).pipe(
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
    return this.#api.get<any>('pageById', { id }).pipe(map((r) => r.data));
  }

  getPageBySlug(tenantId: number, language: 'TR' | 'EN', slug: string): Observable<PageDto> {
    return this.#api
      .get<any>('pageBySlug', { language, slug }, { tenantId })
      .pipe(map((r) => r.data));
  }

  createPage(req: CreatePageRequest): Observable<PageDto> {
    return this.#api.post<any>('pages', req).pipe(map((r) => r.data));
  }

  updatePage(req: UpdatePageRequest): Observable<PageDto> {
    return this.#api.put<any>('pages', req).pipe(map((r) => r.data));
  }

  publishPage(id: number): Observable<PageDto> {
    return this.#api.put<any>('pagePublish', {}, { id }).pipe(map((r) => r.data));
  }

  unpublishPage(id: number): Observable<PageDto> {
    return this.#api.put<any>('pageUnpublish', {}, { id }).pipe(map((r) => r.data));
  }

  schedulePage(id: number, when: string): Observable<PageDto> {
    return this.#api.put<any>('pageSchedule', {}, { id }, { when }).pipe(map((r) => r.data));
  }

  // Categories
  listCategories(tenantId: number, parentId?: number): Observable<PageCategoryDto[]> {
    const qp: Record<string, any> = { tenantId, ...(parentId !== undefined && { parentId }) };
    return this.#api.get<any>('pageCategories', undefined, qp).pipe(map((r) => r.data || []));
  }

  getCategory(id: number): Observable<PageCategoryDto> {
    return this.#api.get<any>('pageCategoryById', { id }).pipe(map((r) => r.data));
  }

  createCategory(req: CreateCategoryRequest): Observable<PageCategoryDto> {
    return this.#api.post<any>('pageCategories', req).pipe(map((r) => r.data));
  }

  updateCategory(req: UpdateCategoryRequest): Observable<PageCategoryDto> {
    return this.#api.put<any>('pageCategories', req).pipe(map((r) => r.data));
  }

  deleteCategory(id: number): Observable<boolean> {
    return this.#api.delete<any>('pageCategoryById', { id }).pipe(map((r) => r.result === 'SUCCESS'));
  }

  getCategoryTree(
    tenantId: number,
    language?: 'TR' | 'EN',
    rootId?: number | null,
    depth?: number
  ): Observable<PageCategoryTreeNode[]> {
    const qp: Record<string, any> = {
      tenantId,
      ...(language && { language }),
      ...(rootId !== undefined && { rootId }),
      ...(depth !== undefined && { depth }),
    };
    // Use custom GET without retry to avoid 4x calls on 5xx responses
    return this.#api
      .custom<any>('GET', 'pageCategoryTree', { queryParams: qp, includeAuth: true })
      .pipe(map((r) => r.data || []));
  }

  getCategoryChildren(
    tenantId: number,
    parentId: number | null,
    language?: 'TR' | 'EN'
  ): Observable<PageCategoryDto[]> {
    const qp: Record<string, any> = { tenantId, parentId, ...(language && { language }) };
    return this.#api.get<any>('pageCategoryChildren', undefined, qp).pipe(map((r) => r.data || []));
  }

  moveCategory(req: MoveCategoryRequest): Observable<boolean> {
    return this.#api
      .put<any>('pageCategoryMove', { newParentId: req.newParentId }, { id: req.id })
      .pipe(map((r) => r.result === 'SUCCESS'));
  }

  reorderCategories(req: ReorderCategoryRequest): Observable<boolean> {
    return this.#api
      .put<any>('pageCategoryReorder', { parentId: req.parentId, orderedIds: req.orderedIds })
      .pipe(map((r) => r.result === 'SUCCESS'));
  }

  // Sections
  listSections(pageId: number): Observable<PageSectionDto[]> {
    return this.#api
      .get<any>('pageBuilderSections', undefined, { pageId })
      .pipe(map((r) => r.data || []));
  }

  addSection(pageId: number, type?: string, displayOrder?: number, data?: string): Observable<PageSectionDto> {
    return this.#api
      .post<any>('pageBuilderSections', { pageId, type, displayOrder, data })
      .pipe(map((r) => r.data));
  }

  updateSection(id: number, payload: Partial<PageSectionDto>): Observable<PageSectionDto> {
    return this.#api
      .put<any>('pageBuilderSections', { id, ...payload })
      .pipe(map((r) => r.data));
  }

  deleteSection(id: number): Observable<boolean> {
    return this.#api
      .delete<any>('pageBuilderSectionById', { id })
      .pipe(map((r) => r.result === 'SUCCESS'));
  }

  // Blocks
  listBlocks(sectionId: number): Observable<PageBlockDto[]> {
    return this.#api
      .get<any>('pageBuilderBlocks', undefined, { sectionId })
      .pipe(map((r) => r.data || []));
  }

  addBlock(sectionId: number, type?: string, displayOrder?: number, data?: string): Observable<PageBlockDto> {
    return this.#api
      .post<any>('pageBuilderBlocks', { sectionId, type, displayOrder, data })
      .pipe(map((r) => r.data));
  }

  updateBlock(id: number, payload: Partial<PageBlockDto>): Observable<PageBlockDto> {
    return this.#api
      .put<any>('pageBuilderBlocks', { id, ...payload })
      .pipe(map((r) => r.data));
  }

  deleteBlock(id: number): Observable<boolean> {
    return this.#api
      .delete<any>('pageBuilderBlockById', { id })
      .pipe(map((r) => r.result === 'SUCCESS'));
  }
}


