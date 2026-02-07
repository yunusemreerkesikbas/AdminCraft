import { inject, Injectable } from '@angular/core';
import { ApiClientService } from '@core/api/api-client.service';
import { ApiResponse } from '@core/crud';
import { Observable, of, tap, map, catchError } from 'rxjs';
import { PublicTenantConfig } from './public-tenant-config.types';

@Injectable({ providedIn: 'root' })
export class PublicTenantConfigService {
    readonly #apiClient = inject(ApiClientService);
    readonly #configCache = new Map<string, PublicTenantConfig>();
    readonly #STORAGE_PREFIX = 'public_tenant_config_';

    loadConfig(subdomain: string): Observable<PublicTenantConfig> {
        if (!subdomain) {
            return of(this.#getDefaultConfig());
        }

        const cached = this.#getCachedConfig(subdomain);
        if (cached) {
            return of(cached);
        }

        return this.#apiClient
            .custom<ApiResponse<PublicTenantConfig>>('GET', 'publicTenantConfig', {
                customHeaders: { 'X-Tenant-Subdomain': subdomain }
            })
            .pipe(
                map(response => response.data),
                tap(config => {
                    if (config) {
                        this.#setCachedConfig(subdomain, config);
                    }
                }),
                catchError(() => {
                    const defaultConfig = this.#getDefaultConfig();
                    this.#setCachedConfig(subdomain, defaultConfig);
                    return of(defaultConfig);
                })
            );
    }

    clearCache(subdomain?: string): void {
        if (subdomain) {
            this.#configCache.delete(subdomain);
            sessionStorage.removeItem(this.#STORAGE_PREFIX + subdomain);
        } else {
            this.#configCache.clear();
            Object.keys(sessionStorage)
                .filter(key => key.startsWith(this.#STORAGE_PREFIX))
                .forEach(key => sessionStorage.removeItem(key));
        }
    }

    #getCachedConfig(subdomain: string): PublicTenantConfig | null {
        const memoryCache = this.#configCache.get(subdomain);
        if (memoryCache) {
            return memoryCache;
        }

        const storageCache = sessionStorage.getItem(this.#STORAGE_PREFIX + subdomain);
        if (storageCache) {
            try {
                const config = JSON.parse(storageCache) as PublicTenantConfig;
                this.#configCache.set(subdomain, config);
                return config;
            } catch {
                return null;
            }
        }

        return null;
    }

    #setCachedConfig(subdomain: string, config: PublicTenantConfig): void {
        this.#configCache.set(subdomain, config);
        sessionStorage.setItem(this.#STORAGE_PREFIX + subdomain, JSON.stringify(config));
    }

    #getDefaultConfig(): PublicTenantConfig {
        return {
            recaptcha: {
                enabled: false,
                siteKey: null,
                threshold: 0.5
            }
        };
    }
}
