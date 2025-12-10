import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { SpaInputComponent, SpaSelectComponent, SpaSelectOption, SpaToggleComponent } from '@shared/components/custom-ui';
import { SLOT_POSITION_OPTIONS, SlotPosition } from '@shared/types/common.types';
import { CreatePageSlotRequest, PageSlotResponse, UpdatePageSlotRequest } from '../page-slot.types';

export interface PageSlotFormDialogData {
    pageId: number;
    slot?: PageSlotResponse; // For edit mode - now uses full response type
}

export interface PageSlotFormDialogResult {
    isEdit: boolean;
    data: CreatePageSlotRequest | UpdatePageSlotRequest;
}

@Component({
    selector: 'spa-page-slot-form-dialog',
    templateUrl: './page-slot-form-dialog.component.html',
    styleUrls: ['./page-slot-form-dialog.component.scss'],
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatDialogModule,
        MatButtonModule,
        MatIconModule,
        TranslocoModule,
        SpaInputComponent,
        SpaSelectComponent,
        SpaToggleComponent
    ]
})
export class PageSlotFormDialogComponent {
    form: FormGroup;
    positionOptions: SpaSelectOption<SlotPosition>[];
    isEditMode: boolean;

    constructor(
        private fb: FormBuilder,
        private dialogRef: MatDialogRef<PageSlotFormDialogComponent>,
        private translocoService: TranslocoService,
        @Inject(MAT_DIALOG_DATA) public data: PageSlotFormDialogData
    ) {
        this.isEditMode = !!data.slot?.id;

        // Convert SLOT_POSITION_OPTIONS to SpaSelectOption format
        this.positionOptions = SLOT_POSITION_OPTIONS.map(opt => ({
            value: opt.value,
            label: opt.label
        }));

        this.form = this.fb.group({
            uid: [data.slot?.uid || '', [Validators.maxLength(50)]],
            slotName: [data.slot?.slotName || '', [Validators.required, Validators.maxLength(50)]],
            position: [data.slot?.position || SlotPosition.CENTER, Validators.required],
            isActive: [data.slot?.isActive ?? true],
            isShared: [data.slot?.isShared ?? false]
        });
    }

    get dialogTitle(): string {
        return this.isEditMode
            ? this.translocoService.translate('admin.pageSlots.editTitle')
            : this.translocoService.translate('admin.pageSlots.createTitle');
    }

    get submitButtonText(): string {
        return this.isEditMode
            ? this.translocoService.translate('admin.common.actions.update')
            : this.translocoService.translate('admin.common.actions.create');
    }

    close(): void {
        this.dialogRef.close();
    }

    submit(): void {
        if (this.form.valid) {
            const formValue = this.form.value;

            if (this.isEditMode) {
                const result: PageSlotFormDialogResult = {
                    isEdit: true,
                    data: {
                        uid: formValue.uid?.trim() || undefined,
                        slotName: formValue.slotName.trim(),
                        position: formValue.position,
                        isActive: formValue.isActive,
                        isShared: formValue.isShared
                    } as UpdatePageSlotRequest
                };
                this.dialogRef.close(result);
            } else {
                const result: PageSlotFormDialogResult = {
                    isEdit: false,
                    data: {
                        uid: formValue.uid?.trim() || undefined,
                        slotName: formValue.slotName.trim(),
                        position: formValue.position,
                        isActive: formValue.isActive,
                        isShared: formValue.isShared
                    } as CreatePageSlotRequest
                };
                this.dialogRef.close(result);
            }
        }
    }
}
