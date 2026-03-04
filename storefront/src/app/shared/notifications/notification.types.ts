export type NotificationType =
  | 'success'
  | 'warning'
  | 'alert'
  | 'info';

export type ToastPosition =
  | 'top-right'
  | 'top-left'
  | 'bottom-right'
  | 'bottom-left'
  | 'top-center'
  | 'bottom-center';

export interface NotificationAction {
  label: string;
  callback: () => void;
}

export interface NotificationOptions {
  type?: NotificationType;
  title?: string;
  params?: Record<string, unknown>;
  durationMs?: number;
  position?: ToastPosition;
  icon?: string;
  action?: NotificationAction;
  closeButton?: boolean;
  tapToDismiss?: boolean;
  progressBar?: boolean;
  preventDuplicates?: boolean;
  source?: string; // For internal logging/debugging
}

export interface NotificationConfig {
  maxOpened: number;
  newestOnTop: boolean;
  defaultPosition: ToastPosition;
  defaultDurationMs: number;
  preventDuplicates: boolean;
}

export interface NotificationEvent {
  id: number | string;
  type: NotificationType;
  position: ToastPosition;
  durationMs: number;
  title?: string;
  message: string;
  createdAt: number;
  source?: string;
  actionInvoked?: boolean;
}
