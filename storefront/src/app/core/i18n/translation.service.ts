import { Injectable, inject } from '@angular/core';
import { TranslocoService } from '@jsverse/transloco';
import { Language } from 'app/modules/admin/custom/tenants/tenants.types';
import { BehaviorSubject, Observable, combineLatest, firstValueFrom } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { TenantContextService } from '../tenant/tenant-context.service';
import {
    SupportedLanguage,
    TenantLanguageSettings,
    TranslationConfig,
    TranslationLoadError,
    UserLanguagePreference
} from './translation.types';

@Injectable({
    providedIn: 'root'
})
export class TranslationService {
    private readonly _translocoService = inject(TranslocoService);
    private readonly _tenantContextService = inject(TenantContextService);

    private readonly _config$ = new BehaviorSubject<TranslationConfig>({
        defaultLang: SupportedLanguage.TR,
        fallbackLang: SupportedLanguage.EN,
        supportedLanguages: [SupportedLanguage.TR, SupportedLanguage.EN],
        enableFallback: true,
        logMissingKeys: true
    });

    private readonly _loadErrors$ = new BehaviorSubject<TranslationLoadError[]>([]);
    private readonly _loadingState$ = new BehaviorSubject<boolean>(false);
    private readonly _userPreference$ = new BehaviorSubject<UserLanguagePreference | null>(null);

    get config$(): Observable<TranslationConfig> {
        return this._config$.asObservable();
    }

    get loadErrors$(): Observable<TranslationLoadError[]> {
        return this._loadErrors$.asObservable();
    }

    get isLoading$(): Observable<boolean> {
        return this._loadingState$.asObservable();
    }

    get effectiveLanguage$(): Observable<SupportedLanguage> {
        return combineLatest([
            this._tenantContextService.tenant$,
            this._userPreference$,
            this._config$
        ]).pipe(
            map(([tenant, userPreference, config]) => {
                // Priority: User preference > Tenant default > System default
                if (userPreference?.language && this.isLanguageSupported(userPreference.language, tenant)) {
                    return userPreference.language;
                }
                
                if (tenant?.defaultLanguage) {
                    return this.mapTenantLanguageToSupported(tenant.defaultLanguage);
                }
                
                return config.defaultLang;
            })
        );
    }

    async initializeTranslations(forceReload: boolean = false): Promise<void> {
        this._loadingState$.next(true);
        
        try {
            this._loadErrors$.next([]);
            const tenantSettings = await this.loadTenantLanguageSettings();
            if (tenantSettings) {
                this.updateConfigForTenant(tenantSettings);
            }
            await this.loadUserLanguagePreference();
            await this.loadTranslationModules(forceReload);
            const effectiveLanguage = await firstValueFrom(this.effectiveLanguage$);
            this._translocoService.setActiveLang(effectiveLanguage);

        } catch (error) {
            this.handleTranslationError('initialization', error);
        } finally {
            this._loadingState$.next(false);
        }
    }

    async setUserLanguage(language: SupportedLanguage, persistPreference: boolean = true): Promise<void> {
        try {
            const tenant = this._tenantContextService.tenant();
            if (!this.isLanguageSupported(language, tenant)) {
                throw new Error(`Language ${language} is not supported by current tenant`);
            }
            if (persistPreference) {
                const userPreference: UserLanguagePreference = {
                    userId: this.getCurrentUserId(), // This should come from user context
                    language,
                    fallbackLanguage: this._config$.getValue().fallbackLang
                };
                this._userPreference$.next(userPreference);
                this.persistUserLanguagePreference(userPreference);
            }
            this._translocoService.setActiveLang(language);
            document.documentElement.lang = language;

        } catch (error) {
            this.handleTranslationError('language-change', error);
            throw error;
        }
    }

    getTranslation(key: string, params?: any, language?: SupportedLanguage): Observable<string> {
        const targetLanguage = language || this._translocoService.getActiveLang() as SupportedLanguage;
        return this._translocoService.selectTranslate(key, params, targetLanguage).pipe(
            catchError(error => {
                this.handleTranslationError('translation-get', error, { key, language: targetLanguage });
                return this.getFallbackTranslation(key, params);
            })
        );
    }

    getInstantTranslation(key: string, params?: any, language?: SupportedLanguage): string {
        const targetLanguage = language || this._translocoService.getActiveLang() as SupportedLanguage;
        try {
            return this._translocoService.translate(key, params, targetLanguage);
        } catch (error) {
            this.handleTranslationError('translation-instant', error, { key, language: targetLanguage });
            return this.getInstantFallbackTranslation(key, params);
        }
    }

    hasTranslation(key: string, language?: SupportedLanguage): boolean {
        const targetLanguage = language || this._translocoService.getActiveLang() as SupportedLanguage;
        return this._translocoService.translate(key, {}, targetLanguage) !== key;
    }
    private async loadTranslationModules(forceReload: boolean): Promise<void> {
        try {
            const loadPromises = [
                this.loadAdminTranslations(SupportedLanguage.TR),
                this.loadAdminTranslations(SupportedLanguage.EN)
            ];
            const results = await Promise.allSettled(loadPromises);
            results.forEach((result, index) => {
                if (result.status === 'rejected') {
                    const language = index === 0 ? SupportedLanguage.TR : SupportedLanguage.EN;
                    this.addLoadError(language, 'admin', result.reason);
                }
            });

        } catch (error) {
            this.handleTranslationError('module-loading', error);
            throw error;
        }
    }

    private async loadAdminTranslations(language: SupportedLanguage): Promise<void> {
        try {
            if (language === SupportedLanguage.TR) {
                const module = await import('@modules/admin/i18n/langTR');
                if (module.langTR) {
                    this._translocoService.setTranslation(
                        module.langTR,
                        language,
                        { merge: true }
                    );
                } else {
                    throw new Error('Turkish translation module not found');
                }
            } else if (language === SupportedLanguage.EN) {
                const module = await import('@modules/admin/i18n/langEN');
                if (module.langEN) {
                    this._translocoService.setTranslation(
                        module.langEN,
                        language,
                        { merge: true }
                    );
                } else {
                    throw new Error('English translation module not found');
                }
            } else {
                throw new Error(`Unsupported language: ${language}`);
            }
        } catch (error) {
            this.addLoadError(language, 'admin', error);
            throw error;
        }
    }

    private async loadTenantLanguageSettings(): Promise<TenantLanguageSettings | null> {
        const tenant = this._tenantContextService.tenant();
        if (!tenant) return null;

        return {
            tenantId: tenant.id,
            defaultLanguage: this.mapTenantLanguageToSupported(tenant.defaultLanguage),
            supportedLanguages: tenant.supportedLanguages.map(lang => this.mapTenantLanguageToSupported(lang)),
            enableAutoDetection: true
        };
    }

    private async loadUserLanguagePreference(): Promise<void> {
        const stored = localStorage.getItem('admincraft-user-language-preference');
        if (stored) {
            try {
                const preference = JSON.parse(stored) as UserLanguagePreference;
                this._userPreference$.next(preference);
            } catch (error) {
            }
        }
    }

    private updateConfigForTenant(tenantSettings: TenantLanguageSettings): void {
        const currentConfig = this._config$.getValue();
        const updatedConfig: TranslationConfig = {
            ...currentConfig,
            defaultLang: tenantSettings.defaultLanguage,
            supportedLanguages: tenantSettings.supportedLanguages
        };
        
        this._config$.next(updatedConfig);
    }

    private isLanguageSupported(language: SupportedLanguage, tenant?: any): boolean {
        if (!tenant) {
            return this._config$.getValue().supportedLanguages.includes(language);
        }
        
        return tenant.supportedLanguages?.some((lang: Language | { code: string }) => 
            this.mapTenantLanguageToSupported(lang) === language
        ) ?? false;
    }

    private mapTenantLanguageToSupported(tenantLanguage: Language | { code: string }): SupportedLanguage {
        const code = typeof tenantLanguage === 'object' ? tenantLanguage.code : tenantLanguage;
        
        switch (code) {
            case Language.TR:
            case 'TR':
                return SupportedLanguage.TR;
            case Language.EN:
            case 'EN':
                return SupportedLanguage.EN;
            default:
                return SupportedLanguage.TR;
        }
    }

    private getFallbackTranslation(key: string, params?: any): Observable<string> {
        const config = this._config$.getValue();
        if (!config.enableFallback) {
            return new BehaviorSubject(key).asObservable();
        }

        try {
            const fallback = this._translocoService.translate(key, params, config.fallbackLang);
            return new BehaviorSubject(fallback).asObservable();
        } catch {
            return new BehaviorSubject(key).asObservable();
        }
    }

    private getInstantFallbackTranslation(key: string, params?: any): string {
        const config = this._config$.getValue();
        if (!config.enableFallback) {
            return key;
        }

        try {
            return this._translocoService.translate(key, params, config.fallbackLang);
        } catch {
            return key;
        }
    }

    private persistUserLanguagePreference(preference: UserLanguagePreference): void {
        localStorage.setItem('admincraft-user-language-preference', JSON.stringify(preference));
    }

    private getCurrentUserId(): number {
        return 1;
    }

    private addLoadError(language: string, module: string, error: any): void {
        const currentErrors = this._loadErrors$.getValue();
        const newError: TranslationLoadError = {
            language,
            module,
            error: error instanceof Error ? error : new Error(String(error))
        };
        
        this._loadErrors$.next([...currentErrors, newError]);
    }

    private handleTranslationError(context: string, error: any, details?: any): void {
    }
}