import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { DatePipe, NgClass } from '@angular/common';
import { TranslocoModule } from '@jsverse/transloco';
import { TenantModule } from 'app/core/tenant/tenant.types';

@Component({
    selector: 'spa-tenant-modules',
    templateUrl: './tenant-modules.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: true,
    imports: [TranslocoModule, DatePipe, NgClass],
})
export class SpaTenantModulesComponent {
    @Input() modules: TenantModule[] = [];
}
