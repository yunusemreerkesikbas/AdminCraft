import { Injectable, inject } from '@angular/core';
import { TenantsService } from '@modules/admin/custom/tenants/tenants.service';
import { BehaviorSubject, Observable, catchError, finalize, map, of, shareReplay, take } from 'rxjs';
import { TenantContextService } from '../tenant/tenant-context.service';

@Injectable({
    providedIn: 'root'
})
export class LanguageContextService {
    readonly #tenantContextService = inject(TenantContextService);
    readonly #tenantsService = inject(TenantsService);
    readonly #supportedLanguages$ = new BehaviorSubject<string[]>(['tr', 'en']);
    #languagesLoaded = false;
    #cachedTenantId: number | null = null;
    #pendingRequest$: Observable<string[]> | null = null;

    constructor() {
        // Try to load languages from current tenant context
        this.#tenantContextService.tenant$.subscribe((tenant) => {
            if (tenant?.id && tenant.id !== this.#cachedTenantId) {
                // Update cached tenant ID but don't invalidate cache
                // Let loadTenantLanguages() method handle cache logic
                this.loadTenantLanguages(tenant.id).pipe(take(1)).subscribe();
            }
        });

        // If no tenant in context, try to load from localStorage tenantId
        const tenantId = this.#tenantContextService.getCurrentTenantId();
        if (tenantId && !this.#languagesLoaded) {
            this.loadTenantLanguages(tenantId).pipe(take(1)).subscribe();
        }
    }

    get supportedLanguages$(): Observable<string[]> {
        return this.#supportedLanguages$.asObservable();
    }

    get currentLanguages(): string[] {
        return this.#supportedLanguages$.getValue();
    }

    loadTenantLanguages(tenantId: number): Observable<string[]> {
        // Return cached data if already loaded for this tenant
        if (this.#languagesLoaded && this.#cachedTenantId === tenantId) {
            return of(this.currentLanguages);
        }

        // Return pending request if one exists for the same tenant
        if (this.#pendingRequest$ && this.#cachedTenantId === tenantId) {
            return this.#pendingRequest$;
        }

        // Update cached tenant ID before making the request
        this.#cachedTenantId = tenantId;

        // Create new request with in-flight tracking
        this.#pendingRequest$ = this.#tenantsService.getTenantLanguages(tenantId).pipe(
            map((languagesDto) => {
                const languages = languagesDto.supportedLanguages || [];
                const normalizedLanguages = languages.map(lang =>
                    lang.toString().toLowerCase()
                );
                this.#supportedLanguages$.next(normalizedLanguages);
                this.#languagesLoaded = true;

                return normalizedLanguages;
            }),
            catchError((error) => {
                console.error('Failed to load tenant languages:', error);
                // Keep current languages if API call fails
                const currentLanguages = this.#supportedLanguages$.getValue();
                return of(currentLanguages);
            }),
            finalize(() => {
                // Clear pending request when complete (success or error)
                this.#pendingRequest$ = null;
            }),
            shareReplay(1) // Share single HTTP request among multiple subscribers
        );

        return this.#pendingRequest$;
    }

    refreshLanguages(): void {
        this.#languagesLoaded = false;
        this.#pendingRequest$ = null; // Clear any pending request
        const tenantId = this.#tenantContextService.getCurrentTenantId();

        if (tenantId) {
            this.loadTenantLanguages(tenantId).pipe(take(1)).subscribe({
                next: (languages) => {
                    console.log('Languages refreshed:', languages);
                },
                error: (error) => {
                    console.error('Failed to refresh languages:', error);
                }
            });
        } else {
            console.warn('Cannot refresh languages: No tenant context available');
        }
    }

    isLanguageSupported(language: string): boolean {
        const normalizedLang = language.toLowerCase();
        return this.currentLanguages.includes(normalizedLang);
    }

    getDefaultLanguage(): string {
        const languages = this.currentLanguages;
        return languages.length > 0 ? languages[0] : 'tr';
    }

    reset(): void {
        this.#languagesLoaded = false;
        this.#cachedTenantId = null;
        this.#pendingRequest$ = null; // Clear any pending request
        // Don't reset to hardcoded values, keep current languages
    }
}
