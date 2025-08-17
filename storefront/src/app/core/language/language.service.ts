import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { TranslationService } from '../i18n/translation.service';
import { SupportedLanguage } from '../i18n/translation.types';

export interface LanguageDefinition {
    id: SupportedLanguage;
    title: string;
    flag: string;
}

/**
 * Language Service - Simplified facade for UI components
 * Delegates complex translation logic to TranslationService
 * Following Clean Architecture: This is the Interface Adapter layer
 */
@Injectable({
    providedIn: 'root'
})
export class LanguageService {
    private readonly _translationService = inject(TranslationService);
    
    private _currentLanguage: BehaviorSubject<SupportedLanguage> = new BehaviorSubject(SupportedLanguage.TR);
    private _availableLanguages: LanguageDefinition[] = [
        {
            id: SupportedLanguage.TR,
            title: 'Türkçe',
            flag: 'TR'
        },
        {
            id: SupportedLanguage.EN,
            title: 'English',
            flag: 'US'
        }
    ];

    /**
     * Constructor
     */
    constructor() {
        // Subscribe to effective language from TranslationService
        this._translationService.effectiveLanguage$.subscribe(language => {
            this._currentLanguage.next(language);
        });
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Accessors
    // -----------------------------------------------------------------------------------------------------

    /**
     * Getter for current language
     */
    get currentLanguage$(): Observable<SupportedLanguage> {
        return this._currentLanguage.asObservable();
    }

    /**
     * Getter for current language value
     */
    get currentLanguage(): SupportedLanguage {
        return this._currentLanguage.value;
    }

    /**
     * Getter for available languages
     */
    get availableLanguages(): LanguageDefinition[] {
        return this._availableLanguages;
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Public methods
    // -----------------------------------------------------------------------------------------------------

    /**
     * Set the current language
     * Delegates to TranslationService for proper tenant-aware handling
     *
     * @param language
     */
    async setCurrentLanguage(language: SupportedLanguage): Promise<void> {
        try {
            await this._translationService.setUserLanguage(language, true);
            // The current language will be updated automatically via subscription
        } catch (error) {
            console.error('Failed to set language:', error);
            // Could emit error to user or show notification
        }
    }

    /**
     * Get language definition by id
     *
     * @param id
     */
    getLanguageById(id: SupportedLanguage): LanguageDefinition | undefined {
        return this._availableLanguages.find(language => language.id === id);
    }

    /**
     * Get localized text based on current language
     * Delegates to TranslationService for proper i18n handling
     *
     * @param key
     * @param params
     */
    getLocalizedText(key: string, params?: any): string {
        return this._translationService.getInstantTranslation(key, params);
    }

    /**
     * Get localized text as observable
     * Useful for reactive templates
     *
     * @param key
     * @param params
     */
    getLocalizedText$(key: string, params?: any): Observable<string> {
        return this._translationService.getTranslation(key, params);
    }

    /**
     * Check if a translation key exists
     *
     * @param key
     */
    hasTranslation(key: string): boolean {
        return this._translationService.hasTranslation(key);
    }
}