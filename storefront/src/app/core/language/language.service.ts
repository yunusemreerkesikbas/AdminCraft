import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { TranslationService } from '../i18n/translation.service';
import { SupportedLanguage } from '../i18n/translation.types';

export interface LanguageDefinition {
    id: SupportedLanguage;
    title: string;
    flag: string;
}

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

    constructor() {
        this._translationService.effectiveLanguage$.subscribe(language => {
            this._currentLanguage.next(language);
        });
    }

    get currentLanguage$(): Observable<SupportedLanguage> {
        return this._currentLanguage.asObservable();
    }

    get currentLanguage(): SupportedLanguage {
        return this._currentLanguage.value;
    }

    get availableLanguages(): LanguageDefinition[] {
        return this._availableLanguages;
    }

    async setCurrentLanguage(language: SupportedLanguage): Promise<void> {
        try {
            await this._translationService.setUserLanguage(language, true);
        } catch (error) {
        }
    }

    getLanguageById(id: SupportedLanguage): LanguageDefinition | undefined {
        return this._availableLanguages.find(language => language.id === id);
    }

    getLocalizedText(key: string, params?: any): string {
        return this._translationService.getInstantTranslation(key, params);
    }

    getLocalizedText$(key: string, params?: any): Observable<string> {
        return this._translationService.getTranslation(key, params);
    }

    hasTranslation(key: string): boolean {
        return this._translationService.hasTranslation(key);
    }
}