import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { SpaDialogContentComponent } from './spa-dialog-content/spa-dialog-content.component';
import { SpaDialogFooterComponent } from './spa-dialog-footer/spa-dialog-footer.component';
import { SpaDialogHeaderComponent } from './spa-dialog-header/spa-dialog-header.component';
import { DialogContentType } from './spa-dialog.types';

@Component({
    selector: 'spa-dialog',
    standalone: true,
    imports: [
        CommonModule,
        SpaDialogHeaderComponent,
        SpaDialogContentComponent,
        SpaDialogFooterComponent
    ],
    templateUrl: './spa-dialog.component.html',
    styleUrls: ['./spa-dialog.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class SpaDialogComponent {
    @Input({ required: true }) title!: string;
    @Input() subtitle?: string;
    @Input() showCloseButton = true;

    @Input() contentType: DialogContentType = 'form';

    @Input() showFooter = true;
    @Input() showCancel = true;
    @Input() showSubmit = true;
    @Input() cancelLabel = 'admin.common.cancel';
    @Input() submitLabel = 'admin.common.save';
    @Input() submitDisabled = false;
    @Input() isSubmitting = false;

    @Input() size: 'sm' | 'md' | 'lg' | 'xl' = 'md';

    @Output() closed = new EventEmitter<void>();
    @Output() cancelled = new EventEmitter<void>();
    @Output() submitted = new EventEmitter<void>();
}
