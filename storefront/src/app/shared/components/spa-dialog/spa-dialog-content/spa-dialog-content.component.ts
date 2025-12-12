import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { DialogContentType } from '../spa-dialog.types';

@Component({
    selector: 'spa-dialog-content',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './spa-dialog-content.component.html',
    styleUrls: ['./spa-dialog-content.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class SpaDialogContentComponent {
    @Input() type: DialogContentType = 'form';
}
