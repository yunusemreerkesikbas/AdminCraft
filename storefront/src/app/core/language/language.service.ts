import { DestroyRef, Injectable, inject } from '@angular/core';
import { TranslocoService } from '@jsverse/transloco';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { BehaviorSubject, Observable } from 'rxjs';
import { TranslationService } from '../i18n/translation.service';
import { SupportedLanguage } from '../i18n/translation.types';
import { NavigationService } from '../navigation/navigation.service';
import { environment } from '@environments/environment';

export interface LanguageDefinition {
    id: SupportedLanguage;
    title: string;
    flag: string;
}

const AVAILABLE_LANGUAGES: readonly LanguageDefinition[] = [
    {
        id: SupportedLanguage.TR,
        title: 'Türkçe',
        flag: 'TR',
    },
    {
        id: SupportedLanguage.EN,
        title: 'English',
        flag: 'US',
    },
] as const;

const DEFAULT_LANGUAGE: SupportedLanguage = Object.values(SupportedLanguage).includes(
    environment.defaultLanguage as SupportedLanguage
)
    ? (environment.defaultLanguage as SupportedLanguage)
    : SupportedLanguage.EN;

@Injectable({ providedIn: 'root' })
export class LanguageService {
    readonly #translationService = inject(TranslationService);
    readonly #navigationService = inject(NavigationService);
    readonly #destroyRef = inject(DestroyRef);
    readonly #translocoService = inject(TranslocoService);

    readonly #currentLanguage = new BehaviorSubject<SupportedLanguage>(
        DEFAULT_LANGUAGE
    );

    constructor() {
        this.#translationService.effectiveLanguage$
            .pipe(takeUntilDestroyed(this.#destroyRef))
            .subscribe(language => {
                this.#currentLanguage.next(language);
            });
    }

    get currentLanguage$(): Observable<SupportedLanguage> {
        return this.#currentLanguage.asObservable();
    }

    get currentLanguage(): SupportedLanguage {
        return this.#currentLanguage.value;
    }

    get availableLanguages(): readonly LanguageDefinition[] {
        return AVAILABLE_LANGUAGES;
    }

    async setCurrentLanguage(language: SupportedLanguage): Promise<void> {
        try {
            const activeLang = this.#translocoService.getActiveLang();
            const normalizedActiveLang = activeLang ? activeLang.toLowerCase() : '';

            if (normalizedActiveLang === language) {
                this.#navigationService.setLanguage(language);
                return;
            }
            await this.#translationService.setUserLanguage(language, true);
            this.#navigationService.setLanguage(language);
        } catch (error) {
            console.error('Failed to set language:', error);
        }
    }

    getLanguageById(id: SupportedLanguage): LanguageDefinition | undefined {
        return AVAILABLE_LANGUAGES.find(language => language.id === id);
    }

    getLocalizedText(key: string, params?: Record<string, unknown>): string {
        return this.#translationService.getInstantTranslation(key, params);
    }

    getLocalizedText$(
        key: string,
        params?: Record<string, unknown>
    ): Observable<string> {
        return this.#translationService.getTranslation(key, params);
    }

    hasTranslation(key: string): boolean {
        return this.#translationService.hasTranslation(key);
    }
}
