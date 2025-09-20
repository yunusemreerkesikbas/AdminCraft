import { inject, Injectable } from '@angular/core';
import { ApiClientService } from '@core/api/api-client.service';
import { map, Observable } from 'rxjs';
import {
    ApiResponse,
    ComponentRequest,
    ComponentResponse,
    ComponentType,
    SiteComponentResponse,
} from './components.types';

@Injectable({ providedIn: 'root' })
export class ComponentsService {
  #api = inject(ApiClientService);

  listByType(
    type: ComponentType,
    tenantId: number,
    status?: 'ACTIVE' | 'INACTIVE'
  ): Observable<ComponentResponse[]> {
    const qp: Record<string, any> = { tenantId, ...(status && { status }) };
    return this.#api
      .get<ApiResponse<ComponentResponse[]>>('componentsByType', { type }, qp)
      .pipe(map((r) => r.data || []));
  }

  getByType(
    type: ComponentType,
    id: number
  ): Observable<ComponentResponse> {
    return this.#api
      .get<ApiResponse<ComponentResponse>>('componentByTypeAndId', {
        type,
        id,
      })
      .pipe(map((r) => r.data));
  }

  createByType(req: ComponentRequest): Observable<ComponentResponse> {
    return this.#api
      .post<ApiResponse<ComponentResponse>>('componentsByType', req, {
        type: req.type,
      })
      .pipe(map((r) => r.data));
  }

  updateByType(
    id: number,
    req: ComponentRequest
  ): Observable<ComponentResponse> {
    return this.#api
      .put<ApiResponse<ComponentResponse>>(
        'componentByTypeAndId',
        req,
        { type: req.type, id }
      )
      .pipe(map((r) => r.data));
  }

  deleteByType(type: ComponentType, id: number): Observable<boolean> {
    return this.#api
      .delete<ApiResponse<unknown>>('componentByTypeAndId', { type, id })
      .pipe(map((r) => r.result === 'SUCCESS'));
  }

  listForSite(
    type: ComponentType,
    tenantId: number,
    lang: 'tr' | 'en'
  ): Observable<SiteComponentResponse[]> {
    return this.#api
      .get<ApiResponse<SiteComponentResponse[]>>(
        'componentsSiteList',
        { type },
        { tenantId, lang }
      )
      .pipe(map((r) => r.data || []));
  }
}


