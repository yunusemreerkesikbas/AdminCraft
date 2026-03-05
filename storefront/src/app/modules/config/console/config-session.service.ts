import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

export const CONFIG_CONSOLE_STORAGE_KEY = 'config_console_auth';

@Injectable({ providedIn: 'root' })
export class ConfigSessionService {
    private readonly invalidSession$ = new Subject<void>();

    readonly onInvalidSession = this.invalidSession$.asObservable();

    notifyInvalidSession(): void {
        this.invalidSession$.next();
    }

    clearStoredSession(): void {
        try {
            localStorage.removeItem(CONFIG_CONSOLE_STORAGE_KEY);
        } catch {
            // ignore
        }
    }
}
