import { inject, Injectable } from '@angular/core';
import { TranslocoService } from '@jsverse/transloco';
import { ActiveToast, IndividualConfig, ToastrService } from 'ngx-toastr';
import { Subject } from 'rxjs';
import {
  NotificationEvent,
  NotificationOptions,
  NotificationType,
  ToastPosition
} from './notification.types';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  #toastr = inject(ToastrService);
  #i18n = inject(TranslocoService);

  #events$ = new Subject<NotificationEvent>();
  public readonly events$ = this.#events$.asObservable();

  public show(message: string, options?: NotificationOptions): ActiveToast<any> {
    return this.#open('info', message, options);
  }

  public success(message: string, options?: NotificationOptions): ActiveToast<any> {
    return this.#open('success', message, options);
  }

  public warning(message: string, options?: NotificationOptions): ActiveToast<any> {
    return this.#open('warning', message, options);
  }

  public alert(message: string, options?: NotificationOptions): ActiveToast<any> {
    return this.#open('alert', message, options);
  }

  public info(message: string, options?: NotificationOptions): ActiveToast<any> {
    return this.#open('info', message, options);
  }

  public dismiss(id?: number): void {
    if (typeof id === 'number') {
      this.#toastr.clear(id);
      return;
    }
    this.#toastr.clear();
  }

  #open(type: NotificationType, message: string, options?: NotificationOptions) {
    const config = this.#buildConfig(type, options);
    const title = options?.title
      ? this.#translate(options.title, options?.params)
      : undefined;
    const body = this.#translate(message, options?.params);

    const toast = this.#dispatch(type, body, title, config);

    this.#emitEvent(toast.toastId, type, config, title, body);
    return toast;
  }

  #dispatch(
    type: NotificationType,
    body: string,
    title: string | undefined,
    config: Partial<IndividualConfig>
  ) {
    switch (type) {
      case 'success':
        return this.#toastr.success(body, title, config);
      case 'warning':
        return this.#toastr.warning(body, title, config);
      case 'alert':
        return this.#toastr.error(body, title, config);
      case 'info':
      default:
        return this.#toastr.info(body, title, config);
    }
  }

  #buildConfig(
    type: NotificationType,
    options?: NotificationOptions
  ): Partial<IndividualConfig> {
    const global = this.#toastr.toastrConfig;
    const positionClass = options?.position
      ? this.#mapPosition(options.position)
      : (global.positionClass as string);
    const timeOut = options?.durationMs ?? (global.timeOut as number);

    const base: Partial<IndividualConfig> = {
      positionClass,
      timeOut,
      closeButton: options?.closeButton ?? (global.closeButton as boolean),
      progressBar: options?.progressBar ?? (global.progressBar as boolean),
      tapToDismiss: options?.tapToDismiss ?? (global.tapToDismiss as boolean),
      disableTimeOut: timeOut === 0,
    };

    return base;
  }

  #mapPosition(position: ToastPosition): string {
    switch (position) {
      case 'top-left':
        return 'toast-top-left';
      case 'bottom-right':
        return 'toast-bottom-right';
      case 'bottom-left':
        return 'toast-bottom-left';
      case 'top-center':
        return 'toast-top-center';
      case 'bottom-center':
        return 'toast-bottom-center';
      case 'top-right':
      default:
        return 'toast-top-right';
    }
  }

  #translate(keyOrText: string, params?: Record<string, unknown>): string {
    if (!keyOrText) {
      return '';
    }
    // Try i18n key first; if not found, return as-is
    const has = this.#i18n.translate(keyOrText, params ?? {}, '');
    return has || keyOrText;
  }

  #emitEvent(
    id: number | string,
    type: NotificationType,
    config: Partial<IndividualConfig>,
    title: string | undefined,
    message: string
  ): void {
    this.#events$.next({
      id,
      type,
      position: this.#unmapPosition(
        config.positionClass ??
          (this.#toastr.toastrConfig.positionClass as string)
      ),
      durationMs:
        config.timeOut ?? (this.#toastr.toastrConfig.timeOut as number),
      title,
      message,
      createdAt: Date.now(),
    });
  }

  #unmapPosition(positionClass: string): ToastPosition {
    switch (positionClass) {
      case 'toast-top-left':
        return 'top-left';
      case 'toast-bottom-right':
        return 'bottom-right';
      case 'toast-bottom-left':
        return 'bottom-left';
      case 'toast-top-center':
        return 'top-center';
      case 'toast-bottom-center':
        return 'bottom-center';
      case 'toast-top-right':
      default:
        return 'top-right';
    }
  }
}
