import { provideHttpClient, withInterceptors } from '@angular/common/http';
import {
    ApplicationConfig,
    inject,
    isDevMode,
    provideAppInitializer,
} from '@angular/core';
import { LuxonDateAdapter } from '@angular/material-luxon-adapter';
import { DateAdapter, MAT_DATE_FORMATS } from '@angular/material/core';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideRouter, withInMemoryScrolling } from '@angular/router';
import { provideFuse } from '@fuse';
import { TranslocoService, provideTransloco } from '@jsverse/transloco';
import { environment } from '@environments/environment';
import { appRoutes } from 'app/app.routes';
import { authInterceptor } from 'app/core/auth/auth.interceptor';
import { languageInterceptor } from 'app/core/i18n/language.interceptor';
import { provideAuth } from 'app/core/auth/auth.provider';
import { AuthService } from 'app/core/auth/auth.service';
import { errorRedirectInterceptor } from 'app/core/http/error-redirect.interceptor';
import { errorToastInterceptor } from 'app/core/http/error-toast.interceptor';
import { provideIcons } from 'app/core/icons/icons.provider';
import { ConfigFlagsService } from 'app/core/config/config-flags.service';
import { TenantContextService } from 'app/core/tenant/tenant-context.service';
import { tenantInterceptor } from 'app/core/tenant/tenant.interceptor';

import { provideToastr } from 'ngx-toastr';
import { firstValueFrom } from 'rxjs';

const LANGUAGE_CONFIG: Record<string, {
    label: string;
    key: string;
    loader: () => Promise<{ [key: string]: unknown }>;
}> = {
    tr: {
        label: 'Türkçe',
        key: 'langTR',
        loader: () => import('@modules/admin/i18n/langTR'),
    },
    en: {
        label: 'English',
        key: 'langEN',
        loader: () => import('@modules/admin/i18n/langEN'),
    },
};

export const appConfig: ApplicationConfig = {
    providers: [
        provideAnimations(),
        provideToastr({
            positionClass: 'toast-top-right',
            timeOut: 4000,
            closeButton: true,
            progressBar: true,
            maxOpened: 3,
            autoDismiss: true,
            newestOnTop: true,
            tapToDismiss: true,
        }),
        provideHttpClient(withInterceptors([
            languageInterceptor,
            authInterceptor,
            tenantInterceptor,
            errorToastInterceptor,
            errorRedirectInterceptor
        ])),
        provideRouter(
            appRoutes,
            withInMemoryScrolling({ scrollPositionRestoration: 'enabled' })
        ),

        // Material Date Adapter
        {
            provide: DateAdapter,
            useClass: LuxonDateAdapter,
        },
        {
            provide: MAT_DATE_FORMATS,
            useValue: {
                parse: {
                    dateInput: 'D',
                },
                display: {
                    dateInput: 'DDD',
                    monthYearLabel: 'LLL yyyy',
                    dateA11yLabel: 'DD',
                    monthYearA11yLabel: 'LLLL yyyy',
                },
            },
        },

        provideTransloco({
            config: {
                availableLangs: environment.supportedLanguages.map((lang) => ({
                    id: lang,
                    label: LANGUAGE_CONFIG[lang]?.label || lang,
                })),
                defaultLang: environment.defaultLanguage,
                fallbackLang: environment.defaultLanguage,
                reRenderOnLangChange: true,
                prodMode: !isDevMode(),
                missingHandler: {
                    useFallbackTranslation: true,
                    allowEmpty: false,
                    logMissingKey: !isDevMode(),
                },
                interpolation: ['{{', '}}'],
            },
        }),
        provideAppInitializer(() => {
            const translocoService = inject(TranslocoService);

            return (async () => {
                try {
                    const loadPromises = environment.supportedLanguages
                        .filter((lang) => LANGUAGE_CONFIG[lang])
                        .map(async (lang) => {
                            const config = LANGUAGE_CONFIG[lang];
                            const module = await config.loader();
                            const translations = module[config.key];
                            if (translations) {
                                translocoService.setTranslation(translations, lang, { merge: true });
                            }
                        });

                    await Promise.all(loadPromises);
                    await new Promise(resolve => setTimeout(resolve, 100));
                    translocoService.setActiveLang(environment.defaultLanguage);
                    await new Promise(resolve => setTimeout(resolve, 100));
                } catch (error: unknown) {
                    console.error('[i18n] Failed to load translations:', error);
                }
            })();
        }),

        provideAppInitializer(() => {
            const tenantContext = inject(TenantContextService);
            tenantContext.initializeFromHostname();
            return Promise.resolve();
        }),

        provideAppInitializer(() => {
            const tenantContext = inject(TenantContextService);
            const configFlags = inject(ConfigFlagsService);
            const path = window.location.pathname;
            if (path === '/config' || path.startsWith('/config/')) {
                return Promise.resolve();
            }
            const subdomain = tenantContext.subdomain();
            return configFlags.load(subdomain ?? null);
        }),

        provideAppInitializer(() => {
            const auth = inject(AuthService);
            return firstValueFrom(auth.check()).catch(() => true);
        }),

        // Fuse
        provideAuth(),
        provideIcons(),
        provideFuse({
            fuse: {
                layout: 'classy',
                scheme: 'light',
                screens: {
                    sm: '600px',
                    md: '960px',
                    lg: '1280px',
                    xl: '1440px',
                },
                theme: 'theme-default',
                themes: [
                    {
                        id: 'theme-default',
                        name: 'Default',
                    },
                    {
                        id: 'theme-brand',
                        name: 'Brand',
                    },
                    {
                        id: 'theme-teal',
                        name: 'Teal',
                    },
                    {
                        id: 'theme-rose',
                        name: 'Rose',
                    },
                    {
                        id: 'theme-purple',
                        name: 'Purple',
                    },
                    {
                        id: 'theme-amber',
                        name: 'Amber',
                    },
                ],
            },
        }),
    ],
};
