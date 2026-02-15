import {
    HttpErrorResponse,
    HttpEvent,
    HttpHandlerFn,
    HttpRequest,
} from '@angular/common/http';
import { inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { LanguageService } from 'app/core/language/language.service';
import { environment } from '@environments/environment';
import { Observable, catchError, throwError } from 'rxjs';

export const errorRedirectInterceptor = (
    req: HttpRequest<unknown>,
    next: HttpHandlerFn
): Observable<HttpEvent<unknown>> => {
    const router = inject(Router);
    const languageService = inject(LanguageService);
    const dialog = inject(MatDialog);

    return next(req).pipe(
        catchError((error) => {
            if (!(error instanceof HttpErrorResponse)) {
                return throwError(() => error);
            }

            const status = error.status;
            const lang = languageService.currentLanguage || environment.defaultLanguage;

            const authPaths = [
                '/auth/login',
                '/auth/signup',
                '/auth/forgot-password',
                '/auth/reset-password',
                '/auth/verify-otp',
                '/auth/resend-otp',
            ];
            const isAuthEndpoint = authPaths.some((path) =>
                req.url.includes(path)
            );

            if (isAuthEndpoint) {
                return throwError(() => error);
            }

            if (status === 401) {
                router.navigate(['/sign-in']);
                return throwError(() => error);
            }

            if (status === 403) {
                dialog.closeAll();
                router.navigate([`/${lang}/pages/error/403`]);
                return throwError(() => error);
            }

            if (status === 500) {
                dialog.closeAll();
                router.navigate([`/${lang}/pages/error/500`]);
                return throwError(() => error);
            }

            return throwError(() => error);
        })
    );
};
