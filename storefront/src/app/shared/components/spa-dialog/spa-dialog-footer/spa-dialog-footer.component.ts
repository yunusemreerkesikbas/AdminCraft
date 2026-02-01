import {
    ChangeDetectionStrategy,
    Component,
    EventEmitter,
    Input,
    Output,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslocoModule } from '@jsverse/transloco';

@Component({
    selector: 'spa-dialog-footer',
    standalone: true,
    imports: [MatButtonModule, MatProgressSpinnerModule, TranslocoModule],
    templateUrl: './spa-dialog-footer.component.html',
    styleUrls: ['./spa-dialog-footer.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SpaDialogFooterComponent {
    @Input() showCancel = true;
    @Input() showSubmit = true;
    @Input() cancelLabel = 'admin.common.cancel';
    @Input() submitLabel = 'admin.common.save';
    @Input() submitDisabled = false;
    @Input() isSubmitting = false;
    @Input() submitColor: 'primary' | 'accent' | 'warn' = 'primary';
    @Output() cancelClicked = new EventEmitter<void>();
    @Output() submitClicked = new EventEmitter<void>();
}
