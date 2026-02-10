import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { DatePipe, NgClass } from '@angular/common';
import { TranslocoModule } from '@jsverse/transloco';
import { ProvisioningJobResponse } from '../../tenants.types';

@Component({
    selector: 'spa-tenant-jobs',
    templateUrl: './tenant-jobs.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: true,
    imports: [TranslocoModule, DatePipe, NgClass],
})
export class SpaTenantJobsComponent {
    jobs = input<ProvisioningJobResponse[]>([]);
}
