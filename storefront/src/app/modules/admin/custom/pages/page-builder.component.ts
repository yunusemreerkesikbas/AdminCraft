import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { Router, RouterModule } from '@angular/router';
import { TenantContextService } from '@core/tenant/tenant-context.service';
import { TranslocoPipe } from '@jsverse/transloco';
import { SpaSearchInputComponent } from '@shared/components/custom-ui/spa-search-input/spa-search-input.component';
import { PageBuilderService } from './page-builder.service';

@Component({
  selector: 'spa-page-builder',
  templateUrl: './page-builder.component.html',
  styleUrls: ['./page-builder.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatProgressBarModule,
    MatButtonModule,
    MatIconModule,
    SpaSearchInputComponent,
    TranslocoPipe,
  ],
})
export class PageBuilderComponent {
  isLoading: boolean = false;
  constructor(
    private _svc: PageBuilderService,
    private _tenantCtx: TenantContextService,
    private _router: Router
  ) {}

  create(): void {
    // Follow inventory pattern: notify list to create and open details
    this._svc.requestCreate();
  }
}


