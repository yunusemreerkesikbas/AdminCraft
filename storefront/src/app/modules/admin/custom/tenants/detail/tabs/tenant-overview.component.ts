import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { DatePipe, NgClass } from '@angular/common';
import { TranslocoModule } from '@jsverse/transloco';
import { TenantDetailResponse } from '../../tenants.types';

@Component({
    selector: 'spa-tenant-overview',
    templateUrl: './tenant-overview.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: true,
    imports: [TranslocoModule, DatePipe, NgClass],
})
export class SpaTenantOverviewComponent {
    tenant = input<TenantDetailResponse | null>(null);
}
