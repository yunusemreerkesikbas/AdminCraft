import { CommonModule } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    ViewEncapsulation,
    inject,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { NotificationService } from '@shared/notifications/notification.service';
import { ModalConfig } from './spa-generic-modal.types';

@Component({
    selector: 'spa-generic-modal',
    standalone: true,
    imports: [
        CommonModule,
        MatDialogModule,
        MatButtonModule,
        MatIconModule,
        MatDividerModule,
        MatTooltipModule
    ],
    templateUrl: './spa-generic-modal.component.html',
    styleUrls: ['./spa-generic-modal.component.scss'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class SpaGenericModalComponent<T = any> {
    #dialogRef = inject(MatDialogRef<SpaGenericModalComponent<T>>);
    #notification = inject(NotificationService);

    readonly config = inject<ModalConfig<T>>(MAT_DIALOG_DATA);

    get modalClass(): string {
        return `modal-${this.config.type}`;
    }

    protected copyToClipboard(value: string, label: string): void {
        navigator.clipboard.writeText(value).then(
            () => {
                this.#notification.success(`${label} copied to clipboard`);
            },
            () => {
                this.#notification.alert('Failed to copy to clipboard');
            }
        );
    }

    protected executeAction(action?: { handler?: () => void; value?: any }): void {
        if (action?.handler) {
            action.handler();
        }
        this.#dialogRef.close(action?.value);
    }

    protected close(): void {
        this.#dialogRef.close();
    }
}
