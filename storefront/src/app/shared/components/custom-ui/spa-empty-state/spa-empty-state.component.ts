import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, Input, ViewEncapsulation } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { TranslocoModule } from '@jsverse/transloco';

@Component({
    selector: 'spa-empty-state',
    standalone: true,
    imports: [CommonModule, MatIconModule, TranslocoModule],
    templateUrl: './spa-empty-state.component.html',
    styleUrls: ['./spa-empty-state.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SpaEmptyStateComponent {
    @Input() icon: string = 'inbox';
    @Input() title: string = '';
    @Input() description?: string;
    @Input() iconSize: 'sm' | 'md' | 'lg' = 'md';
}
