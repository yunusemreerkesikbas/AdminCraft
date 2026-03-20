import { SpaReorderListComponent } from '@/app/shared/components/custom-ui';
import { CommonModule } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    inject,
    OnInit,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { LanguageContextService } from '@core/services/language-context.service';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { SpaDialogComponent } from '@shared/components/spa-dialog';
import { SpaDialogBase } from '@shared/components/spa-dialog-base';
import { SpaDialogData } from '@shared/components/spa-dialog-base/spa-dialog-base.types';
import { NotificationService } from '@shared/notifications/notification.service';
import { ConfirmationService } from '@shared/services/confirmation.service';
import {
    BehaviorSubject,
    catchError,
    forkJoin,
    of,
    take,
    takeUntil,
    tap,
} from 'rxjs';
import { ComponentEditDialogComponent } from '../../../components/component-edit-dialog/component-edit-dialog.component';
import { ComponentPickerDialogComponent } from '../../../components/picker/component-picker-dialog.component';
import { ComponentLibraryService } from '../../../components/services/component-library.service';
import {
    PageSlotFormDialogComponent,
    PageSlotFormDialogResult,
} from '../form/page-slot-form-dialog.component';
import { PageSlotService } from '../page-slot.service';
import {
    PageSlotResponse,
    SlotComponentResponse,
    UpdatePageSlotRequest,
} from '../page-slot.types';

export interface PageSlotDialogData extends SpaDialogData {
    pageId: number;
    pageUid?: string;
    templateId?: number | null;
}

@Component({
    selector: 'spa-page-slot-dialog',
    templateUrl: './page-slot-dialog.component.html',
    styleUrls: ['./page-slot-dialog.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: true,
    imports: [
        CommonModule,
        MatButtonModule,
        MatIconModule,
        MatCardModule,
        MatChipsModule,
        MatProgressSpinnerModule,
        MatDialogModule,
        MatTooltipModule,
        TranslocoModule,
        SpaDialogComponent,
        SpaReorderListComponent,
    ],
})
export class PageSlotDialogComponent
    extends SpaDialogBase<void, PageSlotDialogData>
    implements OnInit
{
    #pageSlotService = inject(PageSlotService);
    #notificationService = inject(NotificationService);
    #dialog = inject(MatDialog);
    #translocoService = inject(TranslocoService);
    #confirmationService = inject(ConfirmationService);
    #componentService = inject(ComponentLibraryService);
    #languageContextService = inject(LanguageContextService);

    protected pageId: number;
    protected pageUid: string;
    protected templateId: number | null;

    slots$ = new BehaviorSubject<PageSlotResponse[]>([]);
    isLoading$ = new BehaviorSubject<boolean>(false);

    constructor() {
        super();
        this.pageId = this.data?.pageId ?? 0;
        this.pageUid = this.data?.pageUid ?? '';
        this.templateId = this.data?.templateId ?? null;
    }

    ngOnInit(): void {
        this.#loadSlots();
    }

    #loadSlots(): void {
        if (!this.pageId) return;
        this.isLoading$.next(true);
        this.#pageSlotService
            .getSlots(this.pageId)
            .pipe(
                take(1),
                tap((slots) => {
                    this.slots$.next(slots);
                    this.isLoading$.next(false);
                }),
                catchError(() => {
                    this.isLoading$.next(false);
                    this.#notificationService.alert(
                        'admin.pageBuilder.errors.loadSlotsFailed'
                    );
                    return of([]);
                })
            )
            .subscribe();
    }

    editSlot(slot: PageSlotResponse): void {
        const dialogRef = this.#dialog.open(PageSlotFormDialogComponent, {
            data: { pageId: this.pageId, slot },
            width: '500px',
            panelClass: 'spa-dialog-panel',
        });

        dialogRef
            .afterClosed()
            .pipe(take(1), takeUntil(this.destroy$))
            .subscribe((result: PageSlotFormDialogResult | undefined) => {
                if (!result || !result.isEdit) return;

                this.#pageSlotService
                    .updateSlot(
                        this.pageId,
                        slot.slotName,
                        result.data as UpdatePageSlotRequest
                    )
                    .pipe(take(1))
                    .subscribe({
                        next: () => {
                            this.#notificationService.success(
                                this.#translocoService.translate(
                                    'admin.common.messages.updateSuccess',
                                    { item: 'Slot' }
                                )
                            );
                            this.#loadSlots();
                        },
                        error: () =>
                            this.#notificationService.alert(
                                this.#translocoService.translate(
                                    'admin.common.messages.operationError'
                                )
                            ),
                    });
            });
    }

    deleteSlot(slot: PageSlotResponse): void {
        if (slot.isShared) {
            this.#notificationService.alert(
                this.#translocoService.translate(
                    'admin.pageSlots.sharedSlotDeleteError'
                )
            );
            return;
        }

        this.#confirmationService
            .confirm(
                'admin.pageSlots.deleteTitle',
                'admin.pageSlots.deleteMessage',
                'admin.common.delete'
            )
            .pipe(take(1), takeUntil(this.destroy$))
            .subscribe((confirmed) => {
                if (!confirmed) return;

                this.#pageSlotService
                    .deleteSlot(this.pageId, slot.slotName)
                    .pipe(take(1))
                    .subscribe({
                        next: () => {
                            this.#notificationService.success(
                                this.#translocoService.translate(
                                    'admin.pageSlots.deleteSuccess'
                                )
                            );
                            this.#loadSlots();
                        },
                        error: () =>
                            this.#notificationService.alert(
                                this.#translocoService.translate(
                                    'admin.pageSlots.deleteError'
                                )
                            ),
                    });
            });
    }

    addComponent(slot: PageSlotResponse): void {
        const dialogRef = this.#dialog.open(ComponentPickerDialogComponent, {
            width: '900px',
            maxWidth: '95vw',
            height: '80vh',
            panelClass: 'spa-dialog-panel',
        });

        dialogRef
            .afterClosed()
            .pipe(take(1), takeUntil(this.destroy$))
            .subscribe((component) => {
                if (!component) return;

                this.#pageSlotService
                    .addComponent(this.pageId, slot.slotName, {
                        componentId: component.id,
                    })
                    .pipe(take(1))
                    .subscribe({
                        next: () => {
                            this.#notificationService.success(
                                this.#translocoService.translate(
                                    'admin.pageSlots.addComponentSuccess'
                                )
                            );
                            this.#loadSlots();
                        },
                        error: (err) => {
                            const message =
                                err?.error?.message ||
                                this.#translocoService.translate(
                                    'admin.pageSlots.addComponentError'
                                );
                            this.#notificationService.alert(message);
                        },
                    });
            });
    }

    protected editComponent(comp: SlotComponentResponse): void {
        this.#componentService.getComponentDetail(comp.componentId)
            .pipe(take(1), takeUntil(this.destroy$))
            .subscribe({
                next: (detail) => {
                    const dialogRef = this.#dialog.open(
                        ComponentEditDialogComponent,
                        {
                            width: '900px',
                            height: '90vh',
                            maxHeight: '90vh',
                            panelClass: 'spa-compact-dialog',
                            disableClose: true,
                            data: {
                                mode: 'edit',
                                component: detail,
                                languages:
                                    this.#languageContextService.supportedLanguages(),
                            },
                        }
                    );
                    dialogRef
                        .afterClosed()
                        .pipe(take(1))
                        .subscribe((result) => {
                            if (result) {
                                this.#loadSlots();
                            }
                        });
                },
                error: () =>
                    this.#notificationService.alert(
                        'admin.components.errors.loadDetailFailed'
                    ),
            });
    }

    removeComponent(slot: PageSlotResponse, componentId: number): void {
        this.#confirmationService
            .confirm(
                'admin.pageSlots.removeComponentTitle',
                'admin.pageSlots.removeComponentMessage',
                'admin.common.delete'
            )
            .pipe(take(1), takeUntil(this.destroy$))
            .subscribe((confirmed) => {
                if (!confirmed) return;

                this.#pageSlotService
                    .removeComponent(this.pageId, slot.slotName, componentId)
                    .pipe(take(1))
                    .subscribe({
                        next: () => {
                            this.#notificationService.success(
                                this.#translocoService.translate(
                                    'admin.pageSlots.removeComponentSuccess'
                                )
                            );
                            this.#loadSlots();
                        },
                        error: () =>
                            this.#notificationService.alert(
                                this.#translocoService.translate(
                                    'admin.pageSlots.removeComponentError'
                                )
                            ),
                    });
            });
    }

    onComponentsReorder(
        reorderedComponents: any[],
        slot: PageSlotResponse
    ): void {
        const updatedSlots = this.slots$.value.map((s) => {
            if (s.id === slot.id) {
                return { ...s, components: reorderedComponents };
            }
            return s;
        });
        this.slots$.next(updatedSlots);

        const componentIds = reorderedComponents.map((c) => c.componentId);
        this.#pageSlotService
            .reorderComponents(this.pageId, slot.slotName, componentIds)
            .pipe(take(1))
            .subscribe({
                error: () => {
                    this.#notificationService.alert(
                        this.#translocoService.translate(
                            'admin.pageSlots.reorderError'
                        )
                    );
                    this.#loadSlots();
                },
            });
    }
}
