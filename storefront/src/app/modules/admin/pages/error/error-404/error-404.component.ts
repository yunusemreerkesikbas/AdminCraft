import {
    ChangeDetectionStrategy,
    Component,
    inject,
    ViewEncapsulation,
} from '@angular/core';
import { Router } from '@angular/router';
import { UserService } from 'app/core/user/user.service';
import { LanguageService } from 'app/core/language/language.service';

@Component({
    selector: 'error-404',
    templateUrl: './error-404.component.html',
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [],
})
export class Error404Component {
    #router = inject(Router);
    #userService = inject(UserService);
    #languageService = inject(LanguageService);

    protected navigateToDashboard(): void {
        const user = this.#userService.user();
        const lang = this.#languageService.currentLanguage || 'en';

        if (!user) {
            this.#router.navigate(['/sign-in']);
            return;
        }

        if (user.role === 'SUPER_ADMIN') {
            this.#router.navigate([`/${lang}/platform-dashboard`]);
        } else {
            this.#router.navigate([`/${lang}/site`]);
        }
    }
}
