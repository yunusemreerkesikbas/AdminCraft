import { HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '@environments/environment';
import { TranslocoService } from '@jsverse/transloco';

@Injectable({
    providedIn: 'root'
})
export class HttpHeadersService {
    private readonly _translocoService = inject(TranslocoService);

    getStandardHeaders(): HttpHeaders {
        let headers = new HttpHeaders({
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            'Accept-Language': this.getCurrentLanguage(),
            'X-Client-Version': environment.version,
        });
        const accessToken = this.getAccessToken();
        if (accessToken) {
            headers = headers.set('Authorization', `Bearer ${accessToken}`);
        }
        headers = this.addUserHeaders(headers);
        return headers;
    }

    getUploadHeaders(): HttpHeaders {
        let headers = new HttpHeaders({
            'Accept': 'application/json',
            'Accept-Language': this.getCurrentLanguage(),
            'X-Client-Version': environment.version,
        });
        const accessToken = this.getAccessToken();
        if (accessToken) {
            headers = headers.set('Authorization', `Bearer ${accessToken}`);
        }

        // Add user context headers
        headers = this.addUserHeaders(headers);

        return headers;
    }

    getPublicHeaders(): HttpHeaders {
        return new HttpHeaders({
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            'Accept-Language': this.getCurrentLanguage(),
            'X-Client-Version': environment.version,
        });
    }

    getCustomHeaders(options: {
        includeAuth?: boolean;
        contentType?: string;
        customHeaders?: Record<string, string>;
        includeTenantContext?: boolean;
        includeUserContext?: boolean;
    } = {}): HttpHeaders {
        const {
            includeAuth = true,
            contentType = 'application/json',
            customHeaders = {},
            includeTenantContext = true,
            includeUserContext = true
        } = options;

        let headers = new HttpHeaders({
            'Content-Type': contentType,
            'Accept': 'application/json',
            'Accept-Language': this.getCurrentLanguage(),
            'X-Client-Version': environment.version,
            ...customHeaders
        });

        if (includeAuth) {
            const accessToken = this.getAccessToken();
            if (accessToken) {
                headers = headers.set('Authorization', `Bearer ${accessToken}`);
            }
        }

        
        if (includeUserContext) {
            headers = this.addUserHeaders(headers);
        }

        return headers;
    }

    private getCurrentLanguage(): string {
        const currentLang = this._translocoService.getActiveLang();
        if (environment.supportedLanguages.includes(currentLang)) {
            return currentLang;
        }
        return environment.defaultLanguage;
    }

    getLanguageSpecificHeaders(language: string): HttpHeaders {
        if (!environment.supportedLanguages.includes(language)) {
            throw new Error(`Unsupported language: ${language}`);
        }
        let headers = new HttpHeaders({
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            'Accept-Language': language,
            'X-Client-Version': environment.version,
        });
        const accessToken = this.getAccessToken();
        if (accessToken) {
            headers = headers.set('Authorization', `Bearer ${accessToken}`);
        }
        headers = this.addUserHeaders(headers);
        return headers;
    }

    private getAccessToken(): string | null {
        try {
            return localStorage.getItem('accessToken');
        } catch {
            return null;
        }
    }

    private addTenantHeaders(headers: HttpHeaders): HttpHeaders {
        // Deprecated: tenant headers are handled by HTTP interceptor to respect platform endpoints.
        return headers;
    }

    private addUserHeaders(headers: HttpHeaders): HttpHeaders {
        try {
            const userId = localStorage.getItem('userId');
            if (userId) {
                headers = headers.set('X-User-ID', userId);
            }
        } catch {
        }
        
        return headers;
    }
}