import { ChangeDetectionStrategy, Component, OnInit, signal } from '@angular/core';
import { ConfigAuthComponent } from '../auth/config-auth.component';
import { ConfigDashboardComponent } from '../dashboard/config-dashboard.component';
import { ConfigAuthChallengeResponse, ConfigTokenState } from './config-console.types';

@Component({
    selector: 'config-console',
    standalone: true,
    templateUrl: './config-console.component.html',
    styleUrls: ['./config-console.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [ConfigAuthComponent, ConfigDashboardComponent],
})
export class ConfigConsoleComponent implements OnInit {
    protected readonly storageKey = 'config_console_auth';

    protected stageSig = signal<'login' | 'otp' | 'panel'>('login');
    protected tokenSig = signal<ConfigTokenState | null>(null);
    protected challengeSig = signal<ConfigAuthChallengeResponse | null>(null);

    ngOnInit(): void {
        this.#restoreSession();
    }

    protected onChallengeReceived(challenge: ConfigAuthChallengeResponse): void {
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
        localStorage.removeItem(this.storageKey);
    }

    #restoreSession(): void {
        const raw = localStorage.getItem(this.storageKey);
        if (!raw) {
            return;
        }
        try {
            const parsed = JSON.parse(raw) as ConfigTokenState;
            if (!parsed?.accessToken || !parsed?.issuedAt) {
                localStorage.removeItem(this.storageKey);
                return;
            }
            if (Date.now() > parsed.issuedAt + parsed.expiresIn * 1000) {
                localStorage.removeItem(this.storageKey);
                return;
            }
            this.tokenSig.set(parsed);
            this.stageSig.set('panel');
        } catch {
            localStorage.removeItem(this.storageKey);
        }
    }
}
