import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '@environments/environment';
import { firstValueFrom } from 'rxjs';

interface ApiResponse<T> {
    result: string;
    data?: T;
    message?: string;
}

@Injectable({ providedIn: 'root' })
export class ConfigFlagsService {
    readonly #http = inject(HttpClient);
    readonly #flags = new Map<string, string>();

    async load(subdomain: string | null): Promise<void> {
        // /config panel and admin host are platform-scoped, not tenant-scoped.
        if (!subdomain || subdomain === 'admin') {
            return;
        }
        try {
            const headers = new HttpHeaders({ 'X-Tenant-Subdomain': subdomain });
            const response = await firstValueFrom(
                this.#http.get<ApiResponse<Record<string, string>>>(
                    `${environment.apiBaseUrl}/cms/config`,
                    { headers }
                )
            );
            if (response.result === 'SUCCESS' && response.data) {
                this.#flags.clear();
                Object.entries(response.data).forEach(([k, v]) => this.#flags.set(k, v));
            }
        } catch {
            // Non-critical — storefront continues without flags
        }
    }

    flag(key: string, defaultValue: string): string;
    flag(key: string, defaultValue: boolean): boolean;
    flag(key: string, defaultValue: string | boolean): string | boolean {
        const raw = this.#flags.get(key);
        if (raw === undefined) {
            return defaultValue;
        }
        if (typeof defaultValue === 'boolean') {
            return raw === 'true' || raw === '1' || raw === 'yes';
        }
        return raw;
    }

    hasFlag(key: string): boolean {
        return this.#flags.has(key);
    }
}
