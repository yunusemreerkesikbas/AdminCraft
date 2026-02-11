import { Injectable, inject, signal } from '@angular/core';
import { TenantsService } from '@modules/admin/custom/tenants/tenants.service';
import { Observable, catchError, finalize, map, of, shareReplay, take } from 'rxjs';
import { TenantContextService } from '../tenant/tenant-context.service';
import { environment } from '@environments/environment';

@Injectable({
    providedIn: 'root'
})
export class LanguageContextService {
    readonly #tenantContextService = inject(TenantContextService);
    readonly #tenantsService = inject(TenantsService);
    supportedLanguages = signal<string[]>(environment.supportedLanguages.map(l => l.toUpperCase()));
    #languagesLoaded = false;
    #cachedTenantId: number | null = null;
    #pendingRequest$: Observable<string[]> | null = null;

    constructor() {
        this.#tenantContextService.tenant$.subscribe((tenant) => {
            if (tenant?.id && tenant.id !== this.#cachedTenantId) {
                if (tenant.supportedLanguages?.length) {
                    const normalizedLanguages = tenant.supportedLanguages.map(l => l.code.toUpperCase());
                    this.supportedLanguages.set(normalizedLanguages);
                    this.#languagesLoaded = true;
                    this.#cachedTenantId = tenant.id;
                } else {
                    this.loadTenantLanguages(tenant.id).pipe(take(1)).subscribe();
                }
            }
        });
        
        // Initial check if tenant is already loaded
        const currentTenant = this.#tenantContextService.tenant();
        if (currentTenant?.id && !this.#languagesLoaded) {
             if (currentTenant.supportedLanguages?.length) {
                const normalizedLanguages = currentTenant.supportedLanguages.map(l => l.code.toUpperCase());
                this.supportedLanguages.set(normalizedLanguages);
                this.#languagesLoaded = true;
                this.#cachedTenantId = currentTenant.id;
             } else {
                this.loadTenantLanguages(currentTenant.id).pipe(take(1)).subscribe();
             }
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
                    lang.toString().toUpperCase()
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
        const normalizedLang = language.toUpperCase();
        return this.supportedLanguages().includes(normalizedLang);
    }

    getDefaultLanguage(): string {
        const languages = this.supportedLanguages();
        return languages.length > 0 ? languages[0] : environment.defaultLanguage.toUpperCase();
    }

    reset(): void {
        this.#languagesLoaded = false;
        this.#cachedTenantId = null;
        this.#pendingRequest$ = null;
    }
}
