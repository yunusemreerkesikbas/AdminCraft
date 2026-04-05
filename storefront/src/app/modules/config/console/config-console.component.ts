import {
    ChangeDetectionStrategy,
    Component,
    DestroyRef,
    OnInit,
    inject,
    signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { TenantContextService } from 'app/core/tenant/tenant-context.service';
import { ConfigAuthComponent } from '../auth/config-auth.component';
import { ConfigDashboardComponent } from '../dashboard/config-dashboard.component';
import {
    ConfigAuthChallengeResponse,
    ConfigTokenState,
} from './config-console.types';
import {
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
    #activatedRoute = inject(ActivatedRoute);
    #session = inject(ConfigSessionService);
    #tenantContext = inject(TenantContextService);
    #destroyRef = inject(DestroyRef);

    protected stageSig = signal<'login' | 'otp' | 'panel'>('login');
    protected tokenSig = signal<ConfigTokenState | null>(null);
    protected challengeSig = signal<ConfigAuthChallengeResponse | null>(null);

    ngOnInit(): void {
        const routeSubdomain =
            this.#activatedRoute.snapshot.queryParamMap.get('subdomain');
        if (routeSubdomain) {
            this.#tenantContext.setSubdomain(routeSubdomain);
        } else {
            const hostSubdomain = this.#tenantContext.extractSubdomainFromHost();
            if (hostSubdomain) {
                this.#tenantContext.setSubdomain(hostSubdomain);
            }
        }

        this.#restoreSession();
        this.#session.onSessionChanged
            .pipe(takeUntilDestroyed(this.#destroyRef))
            .subscribe((session) => {
                this.tokenSig.set(session);
                if (session) {
                    this.stageSig.set('panel');
                    return;
                }

                if (this.stageSig() === 'panel') {
                    this.#resetToLogin();
                }
            });
    }

    protected onChallengeReceived(
        challenge: ConfigAuthChallengeResponse
    ): void {
        this.challengeSig.set(challenge);
        this.stageSig.set('otp');
    }

    protected onAuthenticated(token: ConfigTokenState): void {
        this.#session.setStoredSession(token);
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
        const storedSession = this.#session.getStoredSession();
        if (!storedSession) {
            return;
        }

        if (!this.#session.isAccessTokenExpired(storedSession)) {
            this.#session.setStoredSession(storedSession);
            return;
        }

        if (!storedSession.refreshToken) {
            this.#session.clearStoredSession();
        }
    }
}
