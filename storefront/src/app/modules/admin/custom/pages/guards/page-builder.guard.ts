import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, Router } from '@angular/router';
import { TenantContextService } from '@core/tenant/tenant-context.service';
import { Observable, map, of } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PageBuilderGuard implements CanActivate {

  constructor(
    private _tenantCtx: TenantContextService,
    private _router: Router
  ) {}

  canActivate(route: ActivatedRouteSnapshot): Observable<boolean> {
    const tenantId = this._tenantCtx.getCurrentTenantId();
    
    if (!tenantId) {
      console.warn('PageBuilderGuard: No tenant context available');
      this._router.navigate(['/admin']);
      return of(false);
    }

    // Check if user has permission to access page builder
    // For now, we just check tenant context existence
    // TODO: Add role-based permission checking
    const pageId = route.params['id'];
    if (pageId && !this.isValidPageId(pageId)) {
      console.warn('PageBuilderGuard: Invalid page ID format:', pageId);
      this._router.navigate(['/admin/pages']);
      return of(false);
    }

    return of(true);
  }

  private isValidPageId(pageId: string): boolean {
    const id = Number(pageId);
    return !isNaN(id) && id > 0;
  }
}