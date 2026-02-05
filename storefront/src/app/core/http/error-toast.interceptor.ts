import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandlerFn,
  HttpRequest,
} from '@angular/common/http';
import { inject } from '@angular/core';
import { DeduplicationService } from 'app/shared/notifications/deduplication.service';
import { NotificationService } from 'app/shared/notifications/notification.service';
import { NotificationOptions } from 'app/shared/notifications/notification.types';
import { Observable, catchError, throwError } from 'rxjs';

export const errorToastInterceptor = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
): Observable<HttpEvent<unknown>> => {
  const notify = inject(NotificationService);
  const deduplication = inject(DeduplicationService);

  // İstek bazında bastırma mekanizması kaldırıldı (HttpContextToken)

  return next(req).pipe(
    catchError((error) => {
      if (!(error instanceof HttpErrorResponse)) {
        return throwError(() => error);
      }

      const status = error.status;
      const url = req.url || '';

      // 401, 403, 500: Redirect interceptor tarafından ele alınır - toast yok
      if (status === 401 || status === 403 || status === 500) {
        return throwError(() => error);
      }

      // 400/422: Form doğrulama gövdesi varsa toast gösterme
      if ((status === 400 || status === 422) && hasValidationBody(error)) {
        return throwError(() => error);
      }

      // 5xx (500 hariç) veya ağ hatası: error toast
      if (status > 500 || status === 0) {
        emitDedup(
          notify,
          deduplication,
          'admin.common.errors.server',
          'alert',
          url
        );
        return throwError(() => error);
      }

      // Diğer durumlar: bilgi/uyarı
      emitDedup(
        notify,
        deduplication,
        'admin.common.errors.unexpected',
        'warning',
        url
      );
      return throwError(() => error);
    })
  );
};

function hasValidationBody(error: HttpErrorResponse): boolean {
  const body = error.error as any;
  if (!body) return false;
  // Check for validation errors OR custom error message from backend
  // Backend returns: { result: "ERROR", message: "...", code: 400 }
  return Boolean(body.errors || body.fieldErrors || body.validationErrors || body.message);
}

function sanitizeUrl(url: string): string {
  try {
    const urlObj = new URL(url, window.location.origin);
    return urlObj.pathname; // Remove query params and sensitive data
  } catch {
    return '[invalid-url]';
  }
}

function emitDedup(
  notify: NotificationService,
  deduplication: DeduplicationService,
  messageKey: string,
  type: 'alert' | 'warning',
  source: string
): void {
  const key = `${type}-${messageKey}`;
  
  if (!deduplication.canEmit(key)) {
    return; // Deduplicated within 2 seconds
  }

  // Sanitize source for security (remove sensitive URL params)
  const sanitizedSource = sanitizeUrl(source);
  
  const opts: NotificationOptions = { 
    params: undefined, 
    preventDuplicates: true,
    // Source is only for internal logging, not exposed to user
    ...(sanitizedSource !== '[invalid-url]' && { source: sanitizedSource })
  };
  
  if (type === 'alert') {
    notify.alert(messageKey, opts);
  } else {
    notify.warning(messageKey, opts);
  }
}


