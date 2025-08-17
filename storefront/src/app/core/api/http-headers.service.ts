import { Injectable, inject } from '@angular/core';
import { HttpHeaders } from '@angular/common/http';
import { TranslocoService } from '@jsverse/transloco';
import { environment } from '@environments/environment';

/**
 * HTTP Headers Service
 * Centralized service for managing HTTP headers with i18n, authentication and tenant context
 * Following Clean Architecture principles for infrastructure layer
 */
@Injectable({
    providedIn: 'root'
})
export class HttpHeadersService {
    private readonly _translocoService = inject(TranslocoService);

    /**
     * Get standard headers with authentication, localization and tenant context
     */
    getStandardHeaders(): HttpHeaders {
        let headers = new HttpHeaders({
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            'Accept-Language': this.getCurrentLanguage(),
            'X-Client-Version': environment.version,
        });

        // Add authentication header if available
        const accessToken = this.getAccessToken();
        if (accessToken) {
            headers = headers.set('Authorization', `Bearer ${accessToken}`);
        }

        // Add tenant context headers
        headers = this.addTenantHeaders(headers);
        
        // Add user context headers
        headers = this.addUserHeaders(headers);

        return headers;
    }

    /**
     * Get headers for file upload
     */
    getUploadHeaders(): HttpHeaders {
        let headers = new HttpHeaders({
            'Accept': 'application/json',
            'Accept-Language': this.getCurrentLanguage(),
            'X-Client-Version': environment.version,
        });
        // Note: Don't set Content-Type for file uploads, let browser set it

        const accessToken = this.getAccessToken();
        if (accessToken) {
            headers = headers.set('Authorization', `Bearer ${accessToken}`);
        }

        // Add tenant and user context headers
        headers = this.addTenantHeaders(headers);
        headers = this.addUserHeaders(headers);

        return headers;
    }

    /**
     * Get headers for external API calls (without authentication)
     */
    getPublicHeaders(): HttpHeaders {
        return new HttpHeaders({
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            'Accept-Language': this.getCurrentLanguage(),
            'X-Client-Version': environment.version,
        });
    }

    /**
     * Get custom headers with additional options
     */
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

        if (includeTenantContext) {
            headers = this.addTenantHeaders(headers);
        }
        
        if (includeUserContext) {
            headers = this.addUserHeaders(headers);
        }

        return headers;
    }

    /**
     * Get current user language for headers
     */
    private getCurrentLanguage(): string {
        const currentLang = this._translocoService.getActiveLang();
        
        // Validate against supported languages
        if (environment.supportedLanguages.includes(currentLang)) {
            return currentLang;
        }
        
        // Fallback to default language
        return environment.defaultLanguage;
    }

    /**
     * Get language-specific headers for specific language
     * Useful for content creation/editing in different languages
     */
    getLanguageSpecificHeaders(language: string): HttpHeaders {
        // Validate language
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

        // Add tenant and user context headers
        headers = this.addTenantHeaders(headers);
        headers = this.addUserHeaders(headers);

        return headers;
    }

    /**
     * Helper method to safely get access token
     */
    private getAccessToken(): string | null {
        try {
            return localStorage.getItem('accessToken');
        } catch {
            return null;
        }
    }

    /**
     * Add tenant context headers based on SecurityConfig allowed headers
     */
    private addTenantHeaders(headers: HttpHeaders): HttpHeaders {
        try {
            // X-Tenant-ID header
            const tenantId = localStorage.getItem('tenantId');
            if (tenantId) {
                headers = headers.set('X-Tenant-ID', tenantId);
            }

            // X-Tenant-Subdomain header
            const tenantSubdomain = localStorage.getItem('tenantSubdomain');
            if (tenantSubdomain) {
                headers = headers.set('X-Tenant-Subdomain', tenantSubdomain);
            }
        } catch {
            // Fail silently if localStorage is not available
        }
        
        return headers;
    }

    /**
     * Add user context headers based on SecurityConfig allowed headers
     */
    private addUserHeaders(headers: HttpHeaders): HttpHeaders {
        try {
            // X-User-ID header
            const userId = localStorage.getItem('userId');
            if (userId) {
                headers = headers.set('X-User-ID', userId);
            }
        } catch {
            // Fail silently if localStorage is not available
        }
        
        return headers;
    }
}