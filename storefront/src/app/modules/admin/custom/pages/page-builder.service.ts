import { Injectable } from '@angular/core';
import { CrudEndpoints, CrudHttpService } from '@core/crud';
import { Observable, Subject } from 'rxjs';
import {
    CreatePageCompositeRequest,
    CreatePageRequest,
    Language,
    PageDetailDto,
    PageI18nDto,
    PageI18nRequest,
    PageListDto,
    PublishPageI18nRequest,
    UpdatePageCompositeRequest,
    UpdatePageRequest
} from './page-builder.types';

@Injectable({ providedIn: 'root' })
export class PageBuilderService extends CrudHttpService<PageListDto, CreatePageRequest, UpdatePageRequest> {
  protected endpoints: CrudEndpoints = {
    list: 'pages',
    getById: 'pageById',
    create: 'pages',
    update: 'pageById',
    delete: 'pageById'
  };

  #createRequested = new Subject<void>();
  readonly createRequested$ = this.#createRequested.asObservable();

  override list(): Observable<PageListDto[]> {
    return this.customGet<PageListDto[]>('pages');
  }

  listPages(): Observable<PageListDto[]> {
    return this.list();
  }

  requestCreate(): void {
    this.#createRequested.next();
  }

  getPageById(id: number): Observable<PageListDto> {
    return this.getById(id);
  }

  getPageDetail(id: number): Observable<PageDetailDto> {
    return this.customGet<PageDetailDto>('pageById', { id }, { include: 'translations' });
  }

  createPage(req: CreatePageRequest): Observable<PageListDto> {
    return this.create(req);
  }

  updatePage(id: number, req: UpdatePageRequest): Observable<PageListDto> {
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

  // Composite operations (Sprint 34 pattern)
  getComposite(id: number): Observable<PageDetailDto> {
    return this.customGet<PageDetailDto>('pageComposite', { id });
  }

  createComposite(req: CreatePageCompositeRequest): Observable<PageDetailDto> {
    return this.customPost<PageDetailDto>('pagesComposite', req);
  }

  updateComposite(id: number, req: UpdatePageCompositeRequest): Observable<PageDetailDto> {
    return this.customPut<PageDetailDto>('pageComposite', req, { id });
  }
}
