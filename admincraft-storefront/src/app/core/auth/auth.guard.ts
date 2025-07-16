import { inject } from '@angular/core';
import { Router, type CanActivateFn } from '@angular/router';
import { AuthService } from '../../features/auth/services/auth.service';
import { map, take } from 'rxjs/operators';

export const authGuard: CanActivateFn = (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);
    
    return authService.isAuthenticated().pipe(
        take(1),
        map(isAuth => {
            if (isAuth) {
                console.log('User is authenticated');
                return true;
            }
            router.navigate(['/auth/login']);
            return false;
        })
    );
}; 