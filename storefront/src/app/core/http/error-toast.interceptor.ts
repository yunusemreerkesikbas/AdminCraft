import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandlerFn,
  HttpRequest,
} from '@angular/common/http';
import { inject } from '@angular/core';
import { NotificationService } from 'app/shared/notifications/notification.service';
import { Observable, catchError, throwError } from 'rxjs';

// Basit gürültü azaltma: 2 sn içinde aynı mesajı tekrar gösterme
let lastToastKey = '';
let lastToastAt = 0;

export const errorToastInterceptor = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
): Observable<HttpEvent<unknown>> => {
  const notify = inject(NotificationService);

  // İstek bazında bastırma mekanizması kaldırıldı (HttpContextToken)

  return next(req).pipe(
    catchError((error) => {
      if (!(error instanceof HttpErrorResponse)) {
        return throwError(() => error);
      }

      const status = error.status;
      const url = req.url || '';

      // 401: auth akışı - toast yok
      if (status === 401) {
        return throwError(() => error);
      }

      // 400/422: Form doğrulama gövdesi varsa toast gösterme
      if ((status === 400 || status === 422) && hasValidationBody(error)) {
        return throwError(() => error);
      }

      // 403: yetki uyarısı
      if (status === 403) {
        emitDedup(
          notify,
          'admin.common.errors.forbidden',
          'alert',
          url
        );
        return throwError(() => error);
      }

      // 5xx veya ağ hatası: error toast
      if (status >= 500 || status === 0) {
        emitDedup(
          notify,
          'admin.common.errors.server',
          'alert',
          url
        );
        return throwError(() => error);
      }

      // Diğer durumlar: bilgi/uyarı
      emitDedup(
        notify,
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
  return Boolean(body.errors || body.fieldErrors || body.validationErrors);
}

function emitDedup(
  notify: NotificationService,
  messageKey: string,
  type: 'alert' | 'warning',
  source: string
): void {
  const now = Date.now();
  const key = `${type}-${messageKey}`;
  if (key === lastToastKey && now - lastToastAt < 2000) {
    return;
  }
  lastToastKey = key;
  lastToastAt = now;

  const opts = { params: undefined, preventDuplicates: true } as const;
  if (type === 'alert') {
    notify.alert(messageKey, opts as any);
  } else {
    notify.warning(messageKey, opts as any);
  }
}


