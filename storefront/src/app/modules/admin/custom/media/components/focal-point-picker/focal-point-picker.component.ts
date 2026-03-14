import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TranslocoModule } from '@jsverse/transloco';
import { NotificationService } from '@shared/notifications/notification.service';
import { finalize } from 'rxjs/operators';
import { MediaService } from '../../media.service';
import { FocalPointRequest, Media } from '../../media.types';

@Component({
    selector: 'app-focal-point-picker',
    standalone: true,
    imports: [
        CommonModule,
        MatButtonModule,
        MatIconModule,
        MatTooltipModule,
        MatProgressSpinnerModule,
        TranslocoModule
    ],
    templateUrl: './focal-point-picker.component.html',
    styleUrl: './focal-point-picker.component.scss'
})
export class FocalPointPickerComponent implements OnChanges {
    @Input() media!: Media;
    @Output() updated = new EventEmitter<void>();

    protected focalX = 0.5;
    protected focalY = 0.5;
    protected isUpdating = false;

    #mediaService = inject(MediaService);
    #notificationService = inject(NotificationService);

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['media'] && this.media) {
            this.focalX = this.media.focalPointX ?? 0.5;
            this.focalY = this.media.focalPointY ?? 0.5;
        }
    }

    onImageClick(event: MouseEvent, img: HTMLImageElement): void {
        if (this.isUpdating) return;
        
        const rect = img.getBoundingClientRect();
        const x = event.clientX - rect.left;
        const y = event.clientY - rect.top;

        this.focalX = Math.max(0, Math.min(1, x / rect.width));
        this.focalY = Math.max(0, Math.min(1, y / rect.height));
    }

    saveFocalPoint(): void {
        this.isUpdating = true;
        const request: FocalPointRequest = {
            x: this.focalX,
            y: this.focalY
        };

        this.#mediaService.updateFocalPoint(this.media.id, request)
            .pipe(finalize(() => this.isUpdating = false))
            .subscribe({
                next: (response) => {
                    this.#notificationService.success(response.message!);
                    this.updated.emit();
                },
                error: (error) => {
                    this.#notificationService.alert(error.error.message);
                },
            });
    }

    reset(): void {
        this.focalX = 0.5;
        this.focalY = 0.5;
    }
}
