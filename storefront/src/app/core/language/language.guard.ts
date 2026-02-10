import { Injectable, inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, UrlTree } from '@angular/router';
import { TranslocoService } from '@jsverse/transloco';
import { environment } from '@environments/environment';
import { SupportedLanguage } from '../i18n/translation.types';
import { LanguageService } from './language.service';

@Injectable({
    providedIn: 'root'
})
export class LanguageGuard implements CanActivate {
    readonly #router = inject(Router);
    readonly #languageService = inject(LanguageService);
    readonly #translocoService = inject(TranslocoService);
    readonly #defaultLanguage = environment.defaultLanguage.toLowerCase();

    canActivate(route: ActivatedRouteSnapshot): boolean | UrlTree {
        const lang = route.paramMap.get('lang');

        if (!lang) {
            return this.#router.createUrlTree([`/${this.#defaultLanguage}`]);
        }

        const normalizedLang = lang.toLowerCase();

        if (!this.#isValidLanguage(normalizedLang)) {
            const currentPath = route.url.slice(1).map(segment => segment.path).join('/');
            return this.#router.createUrlTree([
                `/${this.#defaultLanguage}`,
                ...currentPath.split('/')
            ]);
        }

        const supportedLang = normalizedLang as SupportedLanguage;

        const activeLang: string = this.#translocoService.getActiveLang();
        const normalizedActiveLang: string = activeLang
            ? activeLang.toLowerCase()
            : '';

        if (
            normalizedActiveLang !== supportedLang ||
            this.#languageService.currentLanguage !== supportedLang
        ) {
            this.#languageService.setCurrentLanguage(supportedLang);
        }

        return true;
    }

    #isValidLanguage(lang: string): boolean {
        return Object.values(SupportedLanguage).includes(lang as SupportedLanguage);
    }
}
