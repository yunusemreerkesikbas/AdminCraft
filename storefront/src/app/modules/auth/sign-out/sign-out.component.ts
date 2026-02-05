import { I18nPluralPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnDestroy, OnInit, ViewEncapsulation, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { AuthService } from 'app/core/auth/auth.service';
import { Subject, finalize, takeUntil, takeWhile, tap, timer } from 'rxjs';

@Component({
    selector: 'spa-sign-out',
    standalone: true,
    templateUrl: './sign-out.component.html',
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [RouterLink, I18nPluralPipe, TranslocoModule],
})
export class AuthSignOutComponent implements OnInit, OnDestroy {
    #authService = inject(AuthService);
    #router = inject(Router);
    #translocoService = inject(TranslocoService);
    #destroy$ = new Subject<void>();

    protected countdownSig = signal(5);
    protected countdownMapping: any = {
        '=1': this.#translocoService.translate('auth.signOut.countdown.one'),
        other: this.#translocoService.translate('auth.signOut.countdown.other'),
    };

    ngOnInit(): void {
        this.#authService.signOut();

        timer(1000, 1000)
            .pipe(
                finalize(() => {
                    this.#router.navigate(['sign-in']);
                }),
                takeWhile(() => this.countdownSig() > 0),
                takeUntil(this.#destroy$),
                tap(() => this.countdownSig.update(v => v - 1))
            )
            .subscribe();
    }

    ngOnDestroy(): void {
        this.#destroy$.next();
        this.#destroy$.complete();
    }
}
