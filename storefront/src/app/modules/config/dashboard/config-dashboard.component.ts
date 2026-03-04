import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ConfigPropertiesComponent } from '../properties/config-properties.component';
import { ConfigTokenState } from '../console/config-console.types';

@Component({
    selector: 'spa-config-dashboard',
    standalone: true,
    templateUrl: './config-dashboard.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [ConfigPropertiesComponent, MatButtonModule, MatIconModule],
    host: { class: 'flex flex-auto flex-col overflow-hidden' },
})
export class ConfigDashboardComponent {
    token = input.required<ConfigTokenState>();
    logout = output<void>();
}
