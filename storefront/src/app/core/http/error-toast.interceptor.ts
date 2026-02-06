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

  return next(req).pipe(
    catchError((error) => {
      if (!(error instanceof HttpErrorResponse)) {
        return throwError(() => error);
      }

      const status = error.status;
      const url = req.url || '';

      if (status === 401 || status === 403 || status === 500) {
        return throwError(() => error);
      }

      if ((status === 400 || status === 422) && hasValidationBody(error)) {
        return throwError(() => error);
      }

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
    return;
  }

  const sanitizedSource = sanitizeUrl(source);
  
  const opts: NotificationOptions = { 
    params: undefined, 
    preventDuplicates: true,
    ...(sanitizedSource !== '[invalid-url]' && { source: sanitizedSource })
  };
  
  if (type === 'alert') {
    notify.alert(messageKey, opts);
  } else {
    notify.warning(messageKey, opts);
  }
}


