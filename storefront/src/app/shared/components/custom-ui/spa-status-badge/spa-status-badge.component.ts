import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, Input, ViewEncapsulation } from '@angular/core';
import { TranslocoModule } from '@jsverse/transloco';

export type StatusBadgeType = 'ACTIVE' | 'INACTIVE' | 'DRAFT' | 'PENDING' | 'ERROR' | 'WARNING' | 'INFO' | string;

@Component({
    selector: 'spa-status-badge',
    standalone: true,
    imports: [CommonModule, TranslocoModule],
    templateUrl: './spa-status-badge.component.html',
    styleUrls: ['./spa-status-badge.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SpaStatusBadgeComponent {
    @Input() status: StatusBadgeType = 'DRAFT';
    @Input() label?: string;
    @Input() size: 'sm' | 'md' | 'lg' = 'md';
    @Input() variant: 'filled' | 'outline' | 'soft' = 'soft';

    get statusClass(): string {
        return `status-${this.status.toLowerCase()}`;
    }
}
