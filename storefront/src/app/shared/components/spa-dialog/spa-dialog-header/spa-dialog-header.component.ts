import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
    selector: 'spa-dialog-header',
    standalone: true,
    imports: [MatButtonModule, MatIconModule],
    templateUrl: './spa-dialog-header.component.html',
    styleUrls: ['./spa-dialog-header.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class SpaDialogHeaderComponent {
    @Input({ required: true }) title!: string;
    @Input() subtitle?: string;
    @Input() showCloseButton = true;
    @Output() closeClicked = new EventEmitter<void>();

    onClose(event: MouseEvent): void {
        event.stopPropagation();
        this.closeClicked.emit();
    }
}
