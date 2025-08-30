import { Injectable, inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router } from '@angular/router';
import { TenantContextService } from '@core/tenant/tenant-context.service';
import { NotificationService } from 'app/shared/notifications/notification.service';
import { Observable, of } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PageBuilderGuard implements CanActivate {

  constructor(
    private _tenantCtx: TenantContextService,
    private _router: Router
  ) {}

  canActivate(route: ActivatedRouteSnapshot): Observable<boolean> {
    const notify = inject(NotificationService);
    const tenantId = this._tenantCtx.getCurrentTenantId();
    
    if (!tenantId) {
      notify.warning('admin.pageBuilder.errors.noTenant');
      this._router.navigate(['/admin']);
      return of(false);
    }

    // Check if user has permission to access page builder
    // For now, we just check tenant context existence
    // TODO: Add role-based permission checking
    const pageId = route.params['id'];
    if (pageId && !this.isValidPageId(pageId)) {
      notify.warning('admin.pageBuilder.errors.invalidPageId');
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