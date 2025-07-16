import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../../features/auth/services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const authHeaders = authService.getAuthHeaders();
  
  if (Object.keys(authHeaders).length > 0) {
    req = req.clone({
      setHeaders: authHeaders
    });
  }
  
  return next(req);
}; 