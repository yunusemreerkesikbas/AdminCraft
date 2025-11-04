import { computed, inject, Injectable, Signal } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { Navigation } from 'app/core/navigation/navigation.types';
import { NavigationDataService } from 'app/shared/navigation';

@Injectable({ providedIn: 'root' })
export class NavigationService {
    readonly #navigationDataService = inject(NavigationDataService);

    readonly navigation: Signal<Navigation> = computed(() => {
        const filteredItems = this.#navigationDataService.navigation();
        return {
            default: filteredItems,
        };
    });

    readonly navigation$ = toObservable(this.navigation);

    reload(): void {
        this.#navigationDataService.reload();
    }

    setLanguage(language: string): void {
        this.#navigationDataService.setLanguage(language);
    }
}
