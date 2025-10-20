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
import { appRoutes } from 'app/app.routes';
import { authInterceptor } from 'app/core/auth/auth.interceptor';
import { provideAuth } from 'app/core/auth/auth.provider';
import { errorToastInterceptor } from 'app/core/http/error-toast.interceptor';
import { SupportedLanguage } from 'app/core/i18n/translation.types';
import { provideIcons } from 'app/core/icons/icons.provider';
import { TenantContextService } from 'app/core/tenant/tenant-context.service';
import { tenantInterceptor } from 'app/core/tenant/tenant.interceptor';
import { MockApiService } from 'app/mock-api';
import { provideToastr } from 'ngx-toastr';

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
            authInterceptor,
            tenantInterceptor,
            errorToastInterceptor
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
                availableLangs: [
                    {
                        id: SupportedLanguage.TR,
                        label: 'Türkçe',
                    },
                    {
                        id: SupportedLanguage.EN,
                        label: 'English',
                    },
                ],
                defaultLang: SupportedLanguage.TR,
                fallbackLang: SupportedLanguage.EN, // Always fallback to English
                reRenderOnLangChange: true,
                prodMode: !isDevMode(),
                missingHandler: {
                    useFallbackTranslation: true, // Enable fallback to prevent blank UI
                    allowEmpty: false, // Don't allow empty translations
                    logMissingKey: !isDevMode(), // Log only in development
                },
                interpolation: ['{{', '}}'], // Consistent with backend i18n
            },
        }),
        provideAppInitializer(() => {
            const translocoService = inject(TranslocoService);

            return (async () => {
                try {
                    const [adminTR, adminEN] = await Promise.all([
                        import('@modules/admin/i18n/langTR'),
                        import('@modules/admin/i18n/langEN')
                    ]);
                    if (adminTR?.langTR) {
                        translocoService.setTranslation(
                            adminTR.langTR,
                            SupportedLanguage.TR,
                            { merge: true }
                        );
                    }

                    if (adminEN?.langEN) {
                        translocoService.setTranslation(
                            adminEN.langEN,
                            SupportedLanguage.EN,
                            { merge: true }
                        );
                    }

                    translocoService.setActiveLang(SupportedLanguage.TR);
                    
                } catch (error) {
                    try {
                        const fallbackTranslations = {
                            'admin.common.messages.error': 'An error occurred',
                            'admin.common.messages.loading': 'Loading...',
                            'admin.tenants.title': 'Tenant Management',
                            'admin.users.title': 'User Management',
                            'admin.content.title': 'Content Management',
                            'admin.media.title': 'Media Management',
                            'admin.sites.title': 'Site Management'
                        };
                        
                        translocoService.setTranslation(
                            fallbackTranslations,
                            SupportedLanguage.EN,
                            { merge: true }
                        );
                        
                        translocoService.setActiveLang(SupportedLanguage.EN);
                    } catch (fallbackError) {
                    }
                }
            })();
        }),

        provideAppInitializer(() => {
            const tenantContext = inject(TenantContextService);
            tenantContext.initializeFromHostname();
        }),

        // Fuse
        provideAuth(),
        provideIcons(),
        provideFuse({
            mockApi: {
                delay: 0,
                service: MockApiService,
            },
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
