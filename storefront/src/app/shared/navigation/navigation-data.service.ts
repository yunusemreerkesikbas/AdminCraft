import {
    computed,
    effect,
    inject,
    Injectable,
    signal,
    Signal,
} from '@angular/core';
import { UserService } from 'app/core/user/user.service';
import { cloneDeep } from 'lodash-es';
import { DEFAULT_NAVIGATION_ITEMS } from './navigation-data.constants';
import { NavigationFilterService } from './navigation-filter.service';
import { AdminCraftNavigationItem } from './navigation.types';

@Injectable({ providedIn: 'root' })
export class NavigationDataService {
    #userService = inject(UserService);
    #filterService = inject(NavigationFilterService);
    #baseNavigationSig = signal<AdminCraftNavigationItem[]>(
        DEFAULT_NAVIGATION_ITEMS
    );
    #currentLanguageSig = signal<string>(this.#detectLanguage());
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
    }

    updateNavigationItems(items: AdminCraftNavigationItem[]): void {
        this.#baseNavigationSig.set(items);
    }

    setLanguage(language: string): void {
        this.#currentLanguageSig.set(language);
    }

    reload(): void {
        this.#baseNavigationSig.set([...this.#baseNavigationSig()]);
    }

    #detectLanguage(): string {
        return navigator.language?.startsWith('en') ? 'en' : 'tr';
    }
}
