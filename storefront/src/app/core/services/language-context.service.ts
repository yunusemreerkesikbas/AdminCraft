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
                this.#supportedLanguages$.next(normalizedLanguages);
                this.#languagesLoaded = true;

                return normalizedLanguages;
            }),
            catchError(() => {
                const currentLanguages = this.#supportedLanguages$.getValue();
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
        return this.currentLanguages.includes(normalizedLang);
    }

    getDefaultLanguage(): string {
        const languages = this.currentLanguages;
        return languages.length > 0 ? languages[0] : 'tr';
    }

    reset(): void {
        this.#languagesLoaded = false;
        this.#cachedTenantId = null;
        this.#pendingRequest$ = null;
    }
}
