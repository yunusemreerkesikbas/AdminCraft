import { PageRequest, QueryParams, SearchRequest } from './api.types';

export class QueryUtil {
  static buildPageQuery(pageRequest: PageRequest): QueryParams {
    const params: QueryParams = {};
    
    if (pageRequest.page !== undefined && pageRequest.page !== null) {
      params['page'] = pageRequest.page;
    }
    
    if (pageRequest.size !== undefined && pageRequest.size !== null) {
      params['size'] = pageRequest.size;
    }
    
    if (pageRequest.sort) {
      params['sort'] = pageRequest.sort;
    }
    
    return params;
  }

  static buildSearchQuery(searchRequest: SearchRequest): QueryParams {
    const params = this.buildPageQuery(searchRequest);
    
    if (searchRequest.search) {
      params['search'] = searchRequest.search;
    }
    
    return params;
  }

  static toHttpParams(params: QueryParams): Record<string, any> {
    const httpParams: Record<string, any> = {};
    
    Object.keys(params).forEach(key => {
      const value = params[key];
      if (value !== undefined && value !== null) {
        httpParams[key] = value;
      }
    });
    
    return httpParams;
  }

  static merge(base: QueryParams, override: QueryParams): QueryParams {
    return { ...base, ...override };
  }
}

