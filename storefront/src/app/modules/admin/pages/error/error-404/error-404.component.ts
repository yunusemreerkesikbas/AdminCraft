import {
    ChangeDetectionStrategy,
    Component,
    inject,
    ViewEncapsulation,
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SUPER_ADMIN_ROLE } from '@shared/constants';
import { environment } from '@environments/environment';
import { UserService } from 'app/core/user/user.service';

@Component({
    selector: 'error-404',
    templateUrl: './error-404.component.html',
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [],
})
export class Error404Component {
    #router = inject(Router);
    #route = inject(ActivatedRoute);
    #userService = inject(UserService);

    protected navigateToDashboard(): void {
        const user = this.#userService.user();
        const routeLang = this.#route.snapshot.paramMap.get('lang');
        const lang = routeLang || environment.defaultLanguage;

        if (!user) {
            this.#router.navigate(['/sign-in']);
            return;
        }

        if (user.role === SUPER_ADMIN_ROLE) {
            this.#router.navigate([`/${lang}/platform-dashboard`]);
        } else {
            this.#router.navigate([`/${lang}/site`]);
        }
    }
}
