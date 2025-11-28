import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '@environments/environment';
import { EndpointKey, resolveEndpoint } from '@modules/admin/api-endpoints';
import { Observable, retry, throwError, timer } from 'rxjs';
import { catchError, timeout } from 'rxjs/operators';
import { HttpHeadersService } from './http-headers.service';

@Injectable({
    providedIn: 'root'
})
export class ApiClientService {
    private readonly _http = inject(HttpClient);
    private readonly _httpHeaders = inject(HttpHeadersService);
    private readonly _baseUrl = environment.apiBaseUrl;

    get<T>(
        endpointKey: EndpointKey,
        params?: Record<string, string | number>,
        queryParams?: Record<string, any>
    ): Observable<T> {
        const url = this.buildUrl(endpointKey, params);
        const httpParams = this.buildHttpParams(queryParams);

        return this._http.get<T>(url, {
            headers: this._httpHeaders.getStandardHeaders(),
            params: httpParams
        }).pipe(
            timeout(environment.apiTimeout),
            retry({
                count: environment.maxRetryAttempts,
                delay: (error, retryIndex) => this.getRetryDelay(error, retryIndex)
            }),
            catchError((error) => this.handleError(error, 'GET', url))
        );
    }

    post<T>(
        endpointKey: EndpointKey,
        body: any,
        params?: Record<string, string | number>,
        queryParams?: Record<string, any>
    ): Observable<T> {
        const url = this.buildUrl(endpointKey, params);
        const httpParams = this.buildHttpParams(queryParams);

        return this._http.post<T>(url, body, {
            headers: this._httpHeaders.getStandardHeaders(),
            params: httpParams
        }).pipe(
            timeout(environment.apiTimeout),
            retry({
                count: environment.maxRetryAttempts,
                delay: (error, retryIndex) => this.getRetryDelay(error, retryIndex)
            }),
            catchError((error) => this.handleError(error, 'POST', url))
        );
    }

    put<T>(
        endpointKey: EndpointKey,
        body: any,
        params?: Record<string, string | number>,
        queryParams?: Record<string, any>
    ): Observable<T> {
        const url = this.buildUrl(endpointKey, params);
        const httpParams = this.buildHttpParams(queryParams);

        return this._http.put<T>(url, body, {
            headers: this._httpHeaders.getStandardHeaders(),
            params: httpParams
        }).pipe(
            timeout(environment.apiTimeout),
            retry({
                count: environment.maxRetryAttempts,
                delay: (error, retryIndex) => this.getRetryDelay(error, retryIndex)
            }),
            catchError((error) => this.handleError(error, 'PUT', url))
        );
    }

    patch<T>(
        endpointKey: EndpointKey,
        body: any,
        params?: Record<string, string | number>,
        queryParams?: Record<string, any>
    ): Observable<T> {
        const url = this.buildUrl(endpointKey, params);
        const httpParams = this.buildHttpParams(queryParams);
        return this._http.patch<T>(url, body, {
            headers: this._httpHeaders.getStandardHeaders(),
            params: httpParams
        }).pipe(
            timeout(environment.apiTimeout),
            retry({
                count: environment.maxRetryAttempts,
                delay: (error, retryIndex) => this.getRetryDelay(error, retryIndex)
            }),
            catchError((error) => this.handleError(error, 'PATCH', url))
        );
    }

    delete<T>(
        endpointKey: EndpointKey,
        params?: Record<string, string | number>,
        queryParams?: Record<string, any>
    ): Observable<T> {
        const url = this.buildUrl(endpointKey, params);
        const httpParams = this.buildHttpParams(queryParams);

        return this._http.delete<T>(url, {
            headers: this._httpHeaders.getStandardHeaders(),
            params: httpParams
        }).pipe(
            timeout(environment.apiTimeout),
            retry({
                count: environment.maxRetryAttempts,
                delay: (error, retryIndex) => this.getRetryDelay(error, retryIndex)
            }),
            catchError((error) => this.handleError(error, 'DELETE', url))
        );
    }

    upload<T>(
        endpointKey: EndpointKey,
        formData: FormData,
        params?: Record<string, string | number>
    ): Observable<T> {
        const url = this.buildUrl(endpointKey, params);

        return this._http.post<T>(url, formData, {
            headers: this._httpHeaders.getUploadHeaders()
        }).pipe(
            timeout(environment.apiTimeout * 3), // Longer timeout for uploads
            catchError((error) => this.handleError(error, 'UPLOAD', url))
        );
    }

    getPublic<T>(
        endpointKey: EndpointKey,
        params?: Record<string, string | number>,
        queryParams?: Record<string, any>
    ): Observable<T> {
        const url = this.buildUrl(endpointKey, params);
        const httpParams = this.buildHttpParams(queryParams);

        return this._http.get<T>(url, {
            headers: this._httpHeaders.getPublicHeaders(),
            params: httpParams
        }).pipe(
            timeout(environment.apiTimeout),
            catchError((error) => this.handleError(error, 'GET_PUBLIC', url))
        );
    }

    custom<T>(
        method: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE',
        endpointKey: EndpointKey,
        options: {
            body?: any;
            params?: Record<string, string | number>;
            queryParams?: Record<string, any>;
            customHeaders?: Record<string, string>;
            includeAuth?: boolean;
            language?: string;
        } = {}
    ): Observable<T> {
        const url = this.buildUrl(endpointKey, options.params);
        const httpParams = this.buildHttpParams(options.queryParams);
        
        const headers = options.language 
            ? this._httpHeaders.getLanguageSpecificHeaders(options.language)
            : this._httpHeaders.getCustomHeaders({
                includeAuth: options.includeAuth,
                customHeaders: options.customHeaders
            });

        const httpOptions = {
            headers,
            params: httpParams,
            ...(options.body && { body: options.body })
        };

        let request$: Observable<T>;
        
        switch (method) {
            case 'GET':
                request$ = this._http.get<T>(url, httpOptions);
                break;
            case 'POST':
                request$ = this._http.post<T>(url, options.body, httpOptions);
                break;
            case 'PUT':
                request$ = this._http.put<T>(url, options.body, httpOptions);
                break;
            case 'PATCH':
                request$ = this._http.patch<T>(url, options.body, httpOptions);
                break;
            case 'DELETE':
                request$ = this._http.delete<T>(url, httpOptions);
                break;
            default:
                return throwError(() => new Error(`Unsupported HTTP method: ${method}`));
        }

        return request$.pipe(
            timeout(environment.apiTimeout),
            catchError((error) => this.handleError(error, method, url))
        );
    }
    private buildUrl(endpointKey: EndpointKey, params?: Record<string, string | number>): string {
        const endpoint = resolveEndpoint(endpointKey, params);
        return `${this._baseUrl}/${endpoint}`;
    }

    private buildHttpParams(queryParams?: Record<string, any>): HttpParams {
        if (!queryParams) {
            return new HttpParams();
        }

        let params = new HttpParams();
        Object.keys(queryParams).forEach(key => {
            const value = queryParams[key];
            if (value !== null && value !== undefined) {
                params = params.set(key, String(value));
            }
        });

        return params;
    }

    private getRetryDelay(error: any, retryIndex: number): Observable<number> {
        if (error instanceof HttpErrorResponse) {
            const status = error.status;
            if (status >= 400 && status < 500 && status !== 408) {
                return throwError(() => error);
            }
        }
        const delay = Math.pow(2, retryIndex) * 1000;
        return timer(delay);
    }

    private handleError(error: any, method: string, url: string): Observable<never> {
        if (error instanceof HttpErrorResponse) {
            switch (error.status) {
                case 401:
                    break;
                case 403:
                    break;
                case 404:
                    break;
                case 429:
                    break;
                case 500:
                case 502:
                case 503:
                case 504:
                    break;
            }
        }

        return throwError(() => error);
    }
}