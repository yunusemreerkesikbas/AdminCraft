import { Injectable, inject } from '@angular/core';
import { TenantsService } from '@modules/admin/custom/tenants/tenants.service';
import { BehaviorSubject, Observable, catchError, map, of, take } from 'rxjs';
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

    constructor() {
        this.#tenantContextService.tenant$.subscribe((tenant) => {
            if (tenant?.id && tenant.id !== this.#cachedTenantId) {
                this.#cachedTenantId = tenant.id;
                this.#languagesLoaded = false;
                this.loadTenantLanguages(tenant.id).pipe(take(1)).subscribe();
            }
        });
    }

    get supportedLanguages$(): Observable<string[]> {
        return this.#supportedLanguages$.asObservable();
    }

    get currentLanguages(): string[] {
        return this.#supportedLanguages$.getValue();
    }

    loadTenantLanguages(tenantId: number): Observable<string[]> {
        if (this.#languagesLoaded && this.#cachedTenantId === tenantId) {
            return of(this.currentLanguages);
        }

        return this.#tenantsService.getTenantLanguages(tenantId).pipe(
            map((languagesDto) => {
                const languages = languagesDto.supportedLanguages || [];
                const normalizedLanguages = languages.map(lang =>
                    lang.toString().toLowerCase()
                );
                this.#supportedLanguages$.next(normalizedLanguages);
                this.#languagesLoaded = true;
                this.#cachedTenantId = tenantId;

                return normalizedLanguages;
            }),
            catchError((error) => {
                const fallbackLanguages = ['tr', 'en'];
                this.#supportedLanguages$.next(fallbackLanguages);
                this.#languagesLoaded = true;

                return of(fallbackLanguages);
            }),
            take(1)
        );
    }

    refreshLanguages(): void {
        this.#languagesLoaded = false;
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
            this.#supportedLanguages$.next(['tr', 'en']);
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
        this.#supportedLanguages$.next(['tr', 'en']);
    }
}
