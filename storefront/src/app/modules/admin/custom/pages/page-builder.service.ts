import { Injectable } from '@angular/core';
import { CrudEndpoints, CrudHttpService } from '@core/crud';
import { Observable, Subject } from 'rxjs';
import {
    CreateCategoryRequest,
    CreatePageRequest,
    Language,
    PageCategoryDetailDto,
    PageCategoryI18nDto,
    PageCategoryListDto,
    PageDetailDto,
    PageDto,
    PageI18nDto,
    PageI18nRequest,
    PageListDto,
    PublishPageI18nRequest,
    UpdateCategoryRequest,
    UpdatePageRequest,
    UpsertCategoryI18nRequest
} from './page-builder.types';

@Injectable({ providedIn: 'root' })
export class PageBuilderService extends CrudHttpService<PageDto, CreatePageRequest, UpdatePageRequest> {
  protected endpoints: CrudEndpoints = {
    list: 'pages',
    getById: 'pageById',
    create: 'pages',
    update: 'pageById',
    delete: 'pageById'
  };

  #createRequested = new Subject<void>();
  readonly createRequested$ = this.#createRequested.asObservable();

  listPages(): Observable<PageListDto[]> {
    return this.customGet<PageListDto[]>('pages');
  }

  requestCreate(): void {
    this.#createRequested.next();
  }

  getPageById(id: number): Observable<PageDto> {
    return this.getById(id);
  }

  getPageDetail(id: number): Observable<PageDetailDto> {
    return this.customGet<PageDetailDto>('pageById', { id }, { include: 'translations' });
  }

  createPage(req: CreatePageRequest): Observable<PageDto> {
    return this.create(req);
  }

  updatePage(id: number, req: UpdatePageRequest): Observable<PageDto> {
    return this.update(id, req);
  }

  deletePage(id: number): Observable<void> {
    return this.delete(id);
  }

  getPageI18n(pageId: number, language: Language): Observable<PageI18nDto> {
    return this.customGet<PageI18nDto>('pageI18n', { pageId, language });
  }

  updatePageI18n(pageId: number, language: Language, req: PageI18nRequest): Observable<PageI18nDto> {
    return this.customPut<PageI18nDto>('pageI18n', req, { pageId, language });
  }

  publishPageI18n(pageId: number, language: Language, req?: PublishPageI18nRequest): Observable<PageI18nDto> {
    return this.customPost<PageI18nDto>('pageI18nPublish', req || {}, { pageId, language });
  }

  listCategories(): Observable<PageCategoryListDto[]> {
    return this.customGet<PageCategoryListDto[]>('pageCategories');
  }

  getCategoryDetail(id: number): Observable<PageCategoryDetailDto> {
    return this.customGet<PageCategoryDetailDto>('pageCategoryWithTranslations', { id });
  }

  createCategory(req: CreateCategoryRequest): Observable<PageCategoryDetailDto> {
    return this.customPost<PageCategoryDetailDto>('pageCategories', req);
  }

  updateCategory(id: number, req: UpdateCategoryRequest): Observable<PageCategoryDetailDto> {
    return this.customPut<PageCategoryDetailDto>('pageCategoryById', req, { id });
  }

  deleteCategory(id: number): Observable<void> {
    return this.customDelete<void>('pageCategoryById', { id });
  }

  getCategoryI18n(categoryId: number, language: Language): Observable<PageCategoryI18nDto> {
    return this.customGet<PageCategoryI18nDto>('pageCategoryI18n', { categoryId, language });
  }

  upsertCategoryI18n(categoryId: number, language: Language, req: UpsertCategoryI18nRequest): Observable<PageCategoryI18nDto> {
    return this.customPut<PageCategoryI18nDto>('pageCategoryI18n', req, { categoryId, language });
  }
}

