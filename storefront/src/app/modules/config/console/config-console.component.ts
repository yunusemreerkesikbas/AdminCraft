import {
    ChangeDetectionStrategy,
    Component,
    DestroyRef,
    OnInit,
    inject,
    signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ConfigAuthComponent } from '../auth/config-auth.component';
import { ConfigDashboardComponent } from '../dashboard/config-dashboard.component';
import {
    ConfigAuthChallengeResponse,
    ConfigTokenState,
} from './config-console.types';
import {
    CONFIG_CONSOLE_STORAGE_KEY,
    ConfigSessionService,
} from './config-session.service';

@Component({
    selector: 'spa-config-console',
    standalone: true,
    templateUrl: './config-console.component.html',
    styleUrls: ['./config-console.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [ConfigAuthComponent, ConfigDashboardComponent],
})
export class ConfigConsoleComponent implements OnInit {
    protected readonly storageKey = CONFIG_CONSOLE_STORAGE_KEY;

    #session = inject(ConfigSessionService);
    #destroyRef = inject(DestroyRef);

    protected stageSig = signal<'login' | 'otp' | 'panel'>('login');
    protected tokenSig = signal<ConfigTokenState | null>(null);
    protected challengeSig = signal<ConfigAuthChallengeResponse | null>(null);

    ngOnInit(): void {
        this.#restoreSession();
        this.#session.onInvalidSession
            .pipe(takeUntilDestroyed(this.#destroyRef))
            .subscribe(() => this.#resetToLogin());
    }

    protected onChallengeReceived(
        challenge: ConfigAuthChallengeResponse
    ): void {
        this.challengeSig.set(challenge);
        this.stageSig.set('otp');
    }

    protected onAuthenticated(token: ConfigTokenState): void {
        this.tokenSig.set(token);
        localStorage.setItem(this.storageKey, JSON.stringify(token));
        this.stageSig.set('panel');
    }

    protected onCancelOtp(): void {
        this.challengeSig.set(null);
        this.stageSig.set('login');
    }

    protected logout(): void {
        this.tokenSig.set(null);
        this.challengeSig.set(null);
        this.stageSig.set('login');
        this.#session.clearStoredSession();
    }

    #resetToLogin(): void {
        this.tokenSig.set(null);
        this.challengeSig.set(null);
        this.stageSig.set('login');
    }

    #restoreSession(): void {
        const raw = localStorage.getItem(this.storageKey);
        if (!raw) {
            return;
        }
        try {
            const parsed = JSON.parse(raw) as ConfigTokenState;
            if (!parsed?.accessToken || parsed.issuedAt == null) {
                this.#session.clearStoredSession();
                return;
            }
            if (Date.now() > parsed.issuedAt + parsed.expiresIn * 1000) {
                this.#session.clearStoredSession();
                return;
            }
            this.tokenSig.set(parsed);
            this.stageSig.set('panel');
        } catch {
            this.#session.clearStoredSession();
        }
    }
}
