import {
    afterNextRender,
    computed,
    DestroyRef,
    effect,
    inject,
    Injectable,
    signal,
    Signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { environment } from '@environments/environment';
import { TranslocoService } from '@jsverse/transloco';
import { UserService } from 'app/core/user/user.service';
import { cloneDeep } from 'lodash-es';
import { skip } from 'rxjs';
import { DEFAULT_NAVIGATION_ITEMS } from './navigation-data.constants';
import { NavigationFilterService } from './navigation-filter.service';
import { NavigationItem } from './navigation.types';

@Injectable({ providedIn: 'root' })
export class NavigationDataService {
    #userService = inject(UserService);
    #filterService = inject(NavigationFilterService);
    #translocoService = inject(TranslocoService);
    #destroyRef = inject(DestroyRef);
    #baseNavigationSig = signal<NavigationItem[]>(
        DEFAULT_NAVIGATION_ITEMS
    );
    #currentLanguageSig = signal<string>(this.#detectLanguage());
    #translationsLoadedSig = signal<boolean>(false);
    currentLanguage: Signal<string> = this.#currentLanguageSig.asReadonly();

    navigation = computed(() => {
        const items = cloneDeep(this.#baseNavigationSig());
        const user = this.#userService.user();
        const modules = this.#userService.tenantModules();
        const lang = this.#currentLanguageSig();

        return this.#filterService.filterAndUpdateLinks(
            items,
            user,
            modules,
            lang
        );
    });

    constructor() {
        effect(
            () => {
                const user = this.#userService.user();
                const modules = this.#userService.tenantModules();
            },
            { allowSignalWrites: false }
        );

        this.#translocoService.events$
            .pipe(takeUntilDestroyed(this.#destroyRef))
            .subscribe(() => {
                this.#checkAndUpdateTranslations();
            });

        this.#translocoService.langChanges$
            .pipe(
                skip(1),
                takeUntilDestroyed(this.#destroyRef)
            )
            .subscribe(() => {
                this.#checkAndUpdateTranslations();
            });

        afterNextRender(() => {
            this.#checkAndUpdateTranslations();
        });
    }

    #checkAndUpdateTranslations(): void {
        const activeLang = this.#translocoService.getActiveLang();
        if (activeLang) {
            const translation = this.#translocoService.getTranslation(activeLang);
            if (translation && Object.keys(translation).length > 0) {
                queueMicrotask(() => {
                    this.#translationsLoadedSig.set(true);
                    this.reload();
                });
            }
        }
    }

    updateNavigationItems(items: NavigationItem[]): void {
        this.#baseNavigationSig.set(items);
    }

    setLanguage(language: string): void {
        this.#currentLanguageSig.set(language);
    }

    reload(): void {
        this.#baseNavigationSig.set([...this.#baseNavigationSig()]);
    }

    #detectLanguage(): string {
        return environment.defaultLanguage;
    }
}
