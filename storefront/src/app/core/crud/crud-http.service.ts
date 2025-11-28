import { inject } from '@angular/core';
import { ApiClientService } from '@core/api/api-client.service';
import { EndpointKey } from '@modules/admin/api-endpoints';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ApiResponse, CrudEntity, Page, PageRequest, SearchRequest } from './api.types';
import { QueryUtil } from './query.util';

export interface CrudEndpoints {
  list: EndpointKey;
  getById: EndpointKey;
  create: EndpointKey;
  update: EndpointKey;
  delete: EndpointKey;
}

export abstract class CrudHttpService<
  T extends CrudEntity,
  CreateDto = Partial<T>,
  UpdateDto = Partial<T>
> {
  protected api = inject(ApiClientService);
  
  protected abstract endpoints: CrudEndpoints;

  list(pageRequest?: PageRequest): Observable<T[]> {
    const queryParams = pageRequest ? QueryUtil.toHttpParams(QueryUtil.buildPageQuery(pageRequest)) : undefined;
    
    return this.api.get<ApiResponse<T[]>>(
      this.endpoints.list,
      undefined,
      queryParams
    ).pipe(
      map((response) => response.data || [])
    );
  }

  listPaged(pageRequest: PageRequest): Observable<Page<T>> {
    const queryParams = QueryUtil.toHttpParams(QueryUtil.buildPageQuery(pageRequest));
    
    return this.api.get<ApiResponse<Page<T>>>(
      this.endpoints.list,
      undefined,
      queryParams
    ).pipe(
      map((response) => response.data)
    );
  }

  search(searchRequest: SearchRequest): Observable<T[]> {
    const queryParams = QueryUtil.toHttpParams(QueryUtil.buildSearchQuery(searchRequest));
    
    return this.api.get<ApiResponse<T[]>>(
      this.endpoints.list,
      undefined,
      queryParams
    ).pipe(
      map((response) => response.data || [])
    );
  }

  searchPaged(searchRequest: SearchRequest): Observable<Page<T>> {
    const queryParams = QueryUtil.toHttpParams(QueryUtil.buildSearchQuery(searchRequest));
    
    return this.api.get<ApiResponse<Page<T>>>(
      this.endpoints.list,
      undefined,
      queryParams
    ).pipe(
      map((response) => response.data)
    );
  }

  getById(id: number, params?: Record<string, string | number>): Observable<T> {
    return this.api.get<ApiResponse<T>>(
      this.endpoints.getById,
      { id, ...params }
    ).pipe(
      map((response) => response.data)
    );
  }

  create(dto: CreateDto): Observable<T> {
    return this.api.post<ApiResponse<T>>(
      this.endpoints.create,
      dto
    ).pipe(
      map((response) => response.data)
    );
  }

  update(id: number, dto: UpdateDto): Observable<T> {
    return this.api.put<ApiResponse<T>>(
      this.endpoints.update,
      dto,
      { id }
    ).pipe(
      map((response) => response.data)
    );
  }

  delete(id: number): Observable<void> {
    return this.api.delete<ApiResponse<void>>(
      this.endpoints.delete,
      { id }
    ).pipe(
      map(() => undefined)
    );
  }

  protected customGet<R>(
    endpoint: EndpointKey,
    params?: Record<string, string | number>,
    queryParams?: Record<string, any>
  ): Observable<R> {
    return this.api.get<ApiResponse<R>>(endpoint, params, queryParams).pipe(
      map((response) => response.data)
    );
  }

  protected customPost<R>(
    endpoint: EndpointKey,
    body: any,
    params?: Record<string, string | number>
  ): Observable<R> {
    return this.api.post<ApiResponse<R>>(endpoint, body, params).pipe(
      map((response) => response.data)
    );
  }

  protected customPut<R>(
    endpoint: EndpointKey,
    body: any,
    params?: Record<string, string | number>
  ): Observable<R> {
    return this.api.put<ApiResponse<R>>(endpoint, body, params).pipe(
      map((response) => response.data)
    );
  }

  protected customDelete<R>(
    endpoint: EndpointKey,
    params?: Record<string, string | number>
  ): Observable<R> {
    return this.api.delete<ApiResponse<R>>(endpoint, params).pipe(
      map((response) => response.data)
    );
  }
}

