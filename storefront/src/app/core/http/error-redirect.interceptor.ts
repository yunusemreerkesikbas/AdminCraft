import {
    HttpErrorResponse,
    HttpEvent,
    HttpHandlerFn,
    HttpRequest,
} from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { LanguageService } from 'app/core/language/language.service';
import { Observable, catchError, throwError } from 'rxjs';

export const errorRedirectInterceptor = (
    req: HttpRequest<unknown>,
    next: HttpHandlerFn
): Observable<HttpEvent<unknown>> => {
    const router = inject(Router);
    const languageService = inject(LanguageService);

    return next(req).pipe(
        catchError((error) => {
            if (!(error instanceof HttpErrorResponse)) {
                return throwError(() => error);
            }

            const status = error.status;
            const lang = languageService.currentLanguage || 'tr';

            const isAuthEndpoint = req.url.includes('/auth/login') ||
                                  req.url.includes('/auth/signup') ||
                                  req.url.includes('/auth/forgot-password') ||
                                  req.url.includes('/auth/reset-password');

            if (isAuthEndpoint) {
                return throwError(() => error);
            }

            if (status === 401) {
                router.navigate(['/sign-in']);
                return throwError(() => error);
            }

            if (status === 403) {
                router.navigate([`/${lang}/pages/error/403`]);
                return throwError(() => error);
            }

            if (status === 500) {
                router.navigate([`/${lang}/pages/error/500`]);
                return throwError(() => error);
            }

            return throwError(() => error);
        })
    );
};
