import { Injectable, inject, signal } from '@angular/core';
import { TenantsService } from '@modules/admin/custom/tenants/tenants.service';
import { Observable, catchError, finalize, map, of, shareReplay, take } from 'rxjs';
import { TenantContextService } from '../tenant/tenant-context.service';

@Injectable({
    providedIn: 'root'
})
export class LanguageContextService {
    readonly #tenantContextService = inject(TenantContextService);
    readonly #tenantsService = inject(TenantsService);
    supportedLanguages = signal<string[]>(['tr', 'en']);
    #languagesLoaded = false;
    #cachedTenantId: number | null = null;
    #pendingRequest$: Observable<string[]> | null = null;

    constructor() {
        this.#tenantContextService.tenant$.subscribe((tenant) => {
            if (tenant?.id && tenant.id !== this.#cachedTenantId) {
                this.loadTenantLanguages(tenant.id).pipe(take(1)).subscribe();
            }
        });
        const tenantId = this.#tenantContextService.getCurrentTenantId();
        if (tenantId && !this.#languagesLoaded) {
            this.loadTenantLanguages(tenantId).pipe(take(1)).subscribe();
        }
    }

    loadTenantLanguages(tenantId: number): Observable<string[]> {
        if (this.#languagesLoaded && this.#cachedTenantId === tenantId) {
            return of(this.supportedLanguages());
        }
        if (this.#pendingRequest$ && this.#cachedTenantId === tenantId) {
            return this.#pendingRequest$;
        }
        this.#cachedTenantId = tenantId;
        this.#pendingRequest$ = this.#tenantsService.getTenantLanguages(tenantId).pipe(
            map((languagesDto) => {
                const languages = languagesDto.supportedLanguages || [];
                const normalizedLanguages = languages.map(lang =>
                    lang.toString().toLowerCase()
                );
                this.supportedLanguages.set(normalizedLanguages);
                this.#languagesLoaded = true;

                return normalizedLanguages;
            }),
            catchError(() => {
                const currentLanguages = this.supportedLanguages();
                return of(currentLanguages);
            }),
            finalize(() => {
                this.#pendingRequest$ = null;
            }),
            shareReplay(1)
        );

        return this.#pendingRequest$;
    }

    refreshLanguages(): void {
        this.#languagesLoaded = false;
        this.#pendingRequest$ = null;
        const tenantId = this.#tenantContextService.getCurrentTenantId();

        if (tenantId) {
            this.loadTenantLanguages(tenantId).pipe(take(1)).subscribe();
        }
    }

    isLanguageSupported(language: string): boolean {
        const normalizedLang = language.toLowerCase();
        return this.supportedLanguages().includes(normalizedLang);
    }

    getDefaultLanguage(): string {
        const languages = this.supportedLanguages();
        return languages.length > 0 ? languages[0] : 'tr';
    }

    reset(): void {
        this.#languagesLoaded = false;
        this.#cachedTenantId = null;
        this.#pendingRequest$ = null;
    }
}
