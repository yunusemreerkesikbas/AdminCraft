import { HttpHandlerFn, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { TranslocoService } from '@jsverse/transloco';
import { environment } from 'environments/environment';

/**
 * CMS Delivery API endpoints - Accept-Language header will not be added
 * Content language is managed via lang query parameter
 */
const CMS_ENDPOINTS: readonly string[] = ['/cms/'] as const;

/**
 * HTTP Interceptor that adds Accept-Language header to API requests
 * for proper backend i18n support.
 *
 * Excludes CMS Delivery API endpoints since they use lang query parameter
 * for content language management.
 */
export const languageInterceptor: HttpInterceptorFn = (
    req: HttpRequest<unknown>,
    next: HttpHandlerFn
) => {
    // Exclude CMS endpoints - content language is managed separately
    const isCmsEndpoint = CMS_ENDPOINTS.some(endpoint =>
        req.url.includes(endpoint)
    );
    if (isCmsEndpoint) {
        return next(req);
    }

    const translocoService = inject(TranslocoService);

    const currentLang = translocoService.getActiveLang();
    const language = environment.supportedLanguages.includes(currentLang)
        ? currentLang
        : environment.defaultLanguage;

    const clonedRequest = req.clone({
        setHeaders: {
            'Accept-Language': language,
        },
    });

    return next(clonedRequest);
};
