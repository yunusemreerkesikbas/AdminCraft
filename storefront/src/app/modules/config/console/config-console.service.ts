import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { ApiResponse } from '@core/crud';
import { environment } from '@environments/environment';
import { Observable } from 'rxjs';
import {
    ConfigAuditItem,
    ConfigAuthChallengeResponse,
    ConfigAuthResponse,
    ConfigProperty,
    ConfigRecaptchaState,
    PatchRecaptchaPayload,
    UpsertPropertyPayload,
} from './config-console.types';

@Injectable({ providedIn: 'root' })
export class ConfigConsoleService {
    readonly #http = inject(HttpClient);
    readonly #baseUrl = environment.apiBaseUrl;

    login(
        email: string,
        password: string,
        subdomain: string | null,
        tenantId?: number | null
    ): Observable<ApiResponse<ConfigAuthChallengeResponse>> {
        const headers = this.#tenantHeaders(subdomain, tenantId);
        return this.#http.post<ApiResponse<ConfigAuthChallengeResponse>>(
            `${this.#baseUrl}/config/auth/login`,
            { email, password, subdomain, tenantId },
            { headers }
        );
    }

    verifyOtp(
        pendingToken: string,
        otpCode: string,
        subdomain: string | null,
        tenantId?: number | null
    ): Observable<ApiResponse<ConfigAuthResponse>> {
        return this.#http.post<ApiResponse<ConfigAuthResponse>>(
            `${this.#baseUrl}/config/auth/verify-otp`,
            { pendingToken, otpCode, subdomain, tenantId }
        );
    }

    getRecaptcha(
        accessToken: string
    ): Observable<ApiResponse<ConfigRecaptchaState>> {
        return this.#http.get<ApiResponse<ConfigRecaptchaState>>(
            `${this.#baseUrl}/config/admin/security/recaptcha`,
            { headers: this.#authHeaders(accessToken) }
        );
    }

    patchRecaptcha(
        accessToken: string,
        payload: PatchRecaptchaPayload
    ): Observable<ApiResponse<ConfigRecaptchaState>> {
        return this.#http.patch<ApiResponse<ConfigRecaptchaState>>(
            `${this.#baseUrl}/config/admin/security/recaptcha`,
            payload,
            { headers: this.#authHeaders(accessToken) }
        );
    }

    getAudit(
        accessToken: string,
        limit: number = 20
    ): Observable<ApiResponse<ConfigAuditItem[]>> {
        const params = new HttpParams().set('limit', limit);
        return this.#http.get<ApiResponse<ConfigAuditItem[]>>(
            `${this.#baseUrl}/config/admin/security/recaptcha/audit`,
            { headers: this.#authHeaders(accessToken), params }
        );
    }

    listProperties(accessToken: string): Observable<ApiResponse<ConfigProperty[]>> {
        return this.#http.get<ApiResponse<ConfigProperty[]>>(
            `${this.#baseUrl}/config/admin/properties`,
            { headers: this.#authHeaders(accessToken) }
        );
    }

    upsertProperty(
        accessToken: string,
        key: string,
        payload: UpsertPropertyPayload
    ): Observable<ApiResponse<ConfigProperty>> {
        return this.#http.put<ApiResponse<ConfigProperty>>(
            `${this.#baseUrl}/config/admin/properties/${encodeURIComponent(key)}`,
            payload,
            { headers: this.#authHeaders(accessToken) }
        );
    }

    deleteProperty(
        accessToken: string,
        key: string,
        reason: string
    ): Observable<ApiResponse<void>> {
        const params = new HttpParams().set('reason', reason);
        return this.#http.delete<ApiResponse<void>>(
            `${this.#baseUrl}/config/admin/properties/${encodeURIComponent(key)}`,
            { headers: this.#authHeaders(accessToken), params }
        );
    }

    #authHeaders(accessToken: string): HttpHeaders {
        return new HttpHeaders({
            Authorization: `Bearer ${accessToken}`,
            'Content-Type': 'application/json',
        });
    }

    #tenantHeaders(subdomain: string | null, tenantId?: number | null): HttpHeaders {
        let headers = new HttpHeaders({ 'Content-Type': 'application/json' });
        if (subdomain) {
            headers = headers.set('X-Tenant-Subdomain', subdomain);
        }
        if (tenantId) {
            headers = headers.set('X-Tenant-ID', String(tenantId));
        }
        return headers;
    }
}
