import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  OnDestroy,
  OnInit,
  inject,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { TenantContextService } from '@core/tenant/tenant-context.service';
import { TranslocoPipe } from '@jsverse/transloco';
import { ListHeaderComponent } from '@shared/components/custom-ui/list-header/list-header.component';
import { NotificationService } from '@shared/notifications/notification.service';
import { BehaviorSubject, Subject, take } from 'rxjs';
import { ComponentsService } from '../../components.service';
import { ComponentResponse } from '../../components.types';

@Component({
  selector: 'spa-navbar-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatPaginatorModule,
    MatSlideToggleModule,
    MatMenuModule,
    MatSelectModule,
    MatFormFieldModule,
    MatInputModule,
    ListHeaderComponent,
    TranslocoPipe,
  ],
  styles: [
    /* language=SCSS */
    `
        .inventory-grid {
            grid-template-columns: 48px auto 40px;

            @screen sm {
                grid-template-columns: 48px auto 112px 72px;
            }

            @screen md {
                grid-template-columns: 48px 112px auto 112px 72px;
            }

            @screen lg {
                grid-template-columns: 48px 112px auto 112px 96px 96px 72px;
            }
        }
    `,
  ],
  templateUrl: './navbar-list.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NavbarListComponent implements OnInit, OnDestroy {
  #svc = inject(ComponentsService);
  #tenant = inject(TenantContextService);
  #notify = inject(NotificationService);
  #router = inject(Router);
  #route = inject(ActivatedRoute);
  #destroy$ = new Subject<void>();

  isLoading$ = new BehaviorSubject<boolean>(true);
  items$ = new BehaviorSubject<ComponentResponse[]>([]);
  statusFilter$ = new BehaviorSubject<'ACTIVE' | 'INACTIVE' | 'ALL'>('ALL');
  get statusFilter(): 'ACTIVE' | 'INACTIVE' | 'ALL' {
    return this.statusFilter$.value;
  }
  set statusFilter(v: 'ACTIVE' | 'INACTIVE' | 'ALL') {
    this.statusFilter$.next(v);
  }

  ngOnInit(): void {
    this.load();
  }

  ngOnDestroy(): void {
    this.#destroy$.next();
    this.#destroy$.complete();
  }

  load(): void {
    const tenantId = this.#tenant.getCurrentTenantId();
    const status = this.statusFilter$.value;
    this.isLoading$.next(true);
    if (!tenantId) {
      this.isLoading$.next(false);
      this.#notify.alert('admin.common.messages.error');
      return;
    }
    this.#svc
      .listByType('NAVBAR', tenantId, status === 'ALL' ? undefined : status)
      .pipe(take(1))
      .subscribe({
        next: (rows) => {
          this.items$.next(rows);
          this.isLoading$.next(false);
        },
        error: () => {
          this.isLoading$.next(false);
          this.#notify.alert('admin.common.messages.error');
        },
      });
  }

  create(): void {
    this.#router.navigate(['new'], { relativeTo: this.#route });
  }

  edit(id: number): void {
    this.#router.navigate([id], { relativeTo: this.#route });
  }
}


