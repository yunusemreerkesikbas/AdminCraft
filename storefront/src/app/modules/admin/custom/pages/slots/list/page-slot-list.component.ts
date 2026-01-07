import { CdkDragDrop, DragDropModule, moveItemInArray } from '@angular/cdk/drag-drop';
import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, Input, OnInit, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { AdminPageHeaderComponent } from '@shared/components/admin-page-header/admin-page-header.component';
import { SpaGenericModalComponent } from '@shared/components/spa-generic-modal';
import { ModalConfig } from '@shared/components/spa-generic-modal/spa-generic-modal.types';
import { NotificationService } from '@shared/notifications/notification.service';
import { BehaviorSubject, catchError, of, take, tap } from 'rxjs';
import { ComponentPickerDialogComponent } from '../../../components/picker/component-picker-dialog.component';
import { PageSlotFormDialogComponent, PageSlotFormDialogResult } from '../form/page-slot-form-dialog.component';
import { PageSlotService } from '../page-slot.service';
import { CreatePageSlotRequest, PageSlotResponse, UpdatePageSlotRequest } from '../page-slot.types';

@Component({
    selector: 'spa-page-slot-list',
    templateUrl: './page-slot-list.component.html',
    styleUrls: ['./page-slot-list.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: true,
    imports: [
        CommonModule,
        DragDropModule,
        MatButtonModule,
        MatIconModule,
        MatCardModule,
        MatChipsModule,
        MatProgressSpinnerModule,
        TranslocoModule,
        MatDialogModule,
        AdminPageHeaderComponent
    ]
})
export class PageSlotListComponent implements OnInit {
    @Input() pageId!: number;

    #pageSlotService = inject(PageSlotService);
    #notificationService = inject(NotificationService);
    #dialog = inject(MatDialog);
    #translocoService = inject(TranslocoService);

    slots$ = new BehaviorSubject<PageSlotResponse[]>([]);
    isLoading$ = new BehaviorSubject<boolean>(false);

    ngOnInit(): void {
        this.loadSlots();
    }

    loadSlots(): void {
        if (!this.pageId) return;
        this.isLoading$.next(true);
        this.#pageSlotService.getSlots(this.pageId).pipe(
            take(1),
            tap(slots => {
                // Sort slots by position or name if needed, backend sends them sorting order mostly
                this.slots$.next(slots);
                this.isLoading$.next(false);
            }),
            catchError(() => {
                this.isLoading$.next(false);
                this.#notificationService.alert('admin.pageBuilder.errors.loadSlotsFailed');
                return of([]);
            })
        ).subscribe();
    }

    addSlot(): void {
        const dialogRef = this.#dialog.open(PageSlotFormDialogComponent, {
            data: { pageId: this.pageId },
            width: '500px',
            panelClass: 'spa-dialog-panel'
        });

        dialogRef.afterClosed().pipe(take(1)).subscribe((result: PageSlotFormDialogResult | undefined) => {
            if (!result) return;

            this.#pageSlotService.createSlot(this.pageId, result.data as CreatePageSlotRequest)
                .pipe(take(1))
                .subscribe({
                    next: () => {
                        this.#notificationService.success(this.#translocoService.translate('admin.pageSlots.createSuccess'));
                        this.loadSlots();
                    },
                    error: () => this.#notificationService.alert(this.#translocoService.translate('admin.pageSlots.createError'))
                });
        });
    }

    editSlot(slot: PageSlotResponse): void {
        const dialogRef = this.#dialog.open(PageSlotFormDialogComponent, {
            data: { pageId: this.pageId, slot },
            width: '500px',
            panelClass: 'spa-dialog-panel'
        });

        dialogRef.afterClosed().pipe(take(1)).subscribe((result: PageSlotFormDialogResult | undefined) => {
            if (!result || !result.isEdit) return;

            this.#pageSlotService.updateSlot(this.pageId, slot.slotName, result.data as UpdatePageSlotRequest)
                .pipe(take(1))
                .subscribe({
                    next: () => {
                        this.#notificationService.success(this.#translocoService.translate('admin.common.messages.updateSuccess', { item: 'Slot' }));
                        this.loadSlots();
                    },
                    error: () => this.#notificationService.alert(this.#translocoService.translate('admin.common.messages.operationError'))
                });
        });
    }

    deleteSlot(slot: PageSlotResponse): void {
        if (slot.isShared) {
            this.#notificationService.alert(this.#translocoService.translate('admin.pageSlots.sharedSlotDeleteError'));
            return;
        }

        const modalConfig: ModalConfig = {
            type: 'warning',
            variant: 'confirmation',
            title: this.#translocoService.translate('admin.pageSlots.deleteTitle'),
            icon: 'heroicons_outline:exclamation-triangle',
            data: null,
            sections: [
                {
                    type: 'alert-box',
                    alertType: 'warning',
                    content: this.#translocoService.translate('admin.pageSlots.deleteMessage')
                }
            ],
            actions: [
                { label: this.#translocoService.translate('admin.common.cancel'), handler: () => {} },
                {
                    label: this.#translocoService.translate('admin.common.delete'),
                    color: 'warn',
                    handler: () => {
                        this.#pageSlotService.deleteSlot(this.pageId, slot.slotName).pipe(take(1)).subscribe({
                            next: () => {
                                this.#notificationService.success(this.#translocoService.translate('admin.pageSlots.deleteSuccess'));
                                this.loadSlots();
                            },
                            error: () => this.#notificationService.alert(this.#translocoService.translate('admin.pageSlots.deleteError'))
                        });
                    }
                }
            ]
        };

        this.#dialog.open(SpaGenericModalComponent, { data: modalConfig });
    }

    addComponent(slot: PageSlotResponse): void {
        const dialogRef = this.#dialog.open(ComponentPickerDialogComponent, {
            width: '1000px',
            maxWidth: '95vw',
            height: '80vh',
            panelClass: 'spa-dialog-panel'
        });

        dialogRef.afterClosed().pipe(take(1)).subscribe((component) => {
            if (!component) return;

            this.#pageSlotService.addComponent(this.pageId, slot.slotName, { componentId: component.id })
                .pipe(take(1))
                .subscribe({
                    next: () => {
                        this.#notificationService.success(this.#translocoService.translate('admin.pageSlots.addComponentSuccess'));
                        this.loadSlots();
                    },
                    error: (err) => {
                        const message = err?.error?.message || this.#translocoService.translate('admin.pageSlots.addComponentError');
                        this.#notificationService.alert(message);
                    }
                });
        });
    }

    removeComponent(slot: PageSlotResponse, componentId: number): void {
        this.#pageSlotService.removeComponent(this.pageId, slot.slotName, componentId).pipe(take(1)).subscribe({
            next: () => {
                this.#notificationService.success(this.#translocoService.translate('admin.pageSlots.removeComponentSuccess'));
                this.loadSlots();
            },
            error: () => this.#notificationService.alert(this.#translocoService.translate('admin.pageSlots.removeComponentError'))
        });
    }

    drop(event: CdkDragDrop<any[]>, slot: PageSlotResponse): void {
        if (event.previousIndex === event.currentIndex) return;

        const components = [...slot.components];
        moveItemInArray(components, event.previousIndex, event.currentIndex);

        // Optimistic UI update
        const updatedSlots = this.slots$.value.map(s => {
            if (s.id === slot.id) {
                return { ...s, components };
            }
            return s;
        });
        this.slots$.next(updatedSlots);

        const componentIds = components.map(c => c.componentId);
        this.#pageSlotService.reorderComponents(this.pageId, slot.slotName, componentIds).pipe(take(1)).subscribe({
            error: () => {
                this.#notificationService.alert(this.#translocoService.translate('admin.pageSlots.reorderError'));
                this.loadSlots(); // Revert
            }
        });
    }
}
