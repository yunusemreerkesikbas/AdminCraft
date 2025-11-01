import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal, Signal } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { Navigation } from 'app/core/navigation/navigation.types';
import { Observable, take, tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class NavigationService {
    readonly #httpClient = inject(HttpClient);
    #navigationSig = signal<Navigation | null>(null);

    readonly navigation: Signal<Navigation | null> = this.#navigationSig.asReadonly();
    readonly navigation$ = toObservable(this.#navigationSig);

    get(): Observable<Navigation> {
        return this.#httpClient.get<Navigation>('api/common/navigation').pipe(
            tap((navigation) => {
                this.#navigationSig.set(navigation);
            })
        );
    }

    reload(): void {
        this.get().pipe(take(1)).subscribe();
    }
}
