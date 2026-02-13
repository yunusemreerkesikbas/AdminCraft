import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { TranslocoModule, TranslocoService } from '@jsverse/transloco';
import { SpaInputComponent, SpaSelectComponent, SpaSelectOption, SpaToggleComponent } from '@shared/components/custom-ui';
import { SpaDialogComponent } from '@shared/components/spa-dialog';
import { SpaFormDialogData } from '@shared/components/spa-dialog-base/spa-dialog-base.types';
import { SpaFormDialog } from '@shared/components/spa-form-dialog';
import { VALIDATION_LIMITS, VALIDATION_PATTERNS } from '@shared/constants/validation.constants';
import { SLOT_POSITION_OPTIONS, SlotPosition } from '@shared/types/common.types';
import { CreatePageSlotRequest, PageSlotResponse, UpdatePageSlotRequest } from '../page-slot.types';

export interface PageSlotFormDialogData extends SpaFormDialogData<PageSlotResponse> {
    pageId: number;
    slot?: PageSlotResponse;
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
        TranslocoModule,
        SpaInputComponent,
        SpaSelectComponent,
        SpaToggleComponent,
        SpaDialogComponent
    ]
})
export class PageSlotFormDialogComponent extends SpaFormDialog<PageSlotFormDialogResult, PageSlotFormDialogData> {
    readonly #translocoService = inject(TranslocoService);
    
    protected form: FormGroup;
    readonly positionOptions: SpaSelectOption<SlotPosition>[];

    readonly dialogTitle = computed(() => {
        return this.isEditMode()
            ? this.#translocoService.translate('admin.pageSlots.editTitle')
            : this.#translocoService.translate('admin.pageSlots.createTitle');
    });

    readonly submitButtonLabel = computed(() => {
        return this.isEditMode()
            ? this.#translocoService.translate('admin.common.actions.update')
            : this.#translocoService.translate('admin.common.actions.create');
    });

    constructor() {
        super();
        
        this.positionOptions = SLOT_POSITION_OPTIONS.map(opt => ({
            value: opt.value,
            label: opt.label
        }));

        const slot = this.data?.slot;
        this.form = this.fb.group({
            uid: [slot?.uid || '', [
                Validators.maxLength(VALIDATION_LIMITS.UID_MAX),
                Validators.pattern(VALIDATION_PATTERNS.UID)
            ]],
            slotName: [slot?.slotName || '', [
                Validators.required,
                Validators.maxLength(VALIDATION_LIMITS.SLOT_NAME_MAX),
                Validators.pattern(VALIDATION_PATTERNS.SLOT_NAME)
            ]],
            position: [slot?.position || SlotPosition.CENTER, Validators.required],
            isActive: [slot?.isActive ?? true],
            isShared: [slot?.isShared ?? false]
        });
    }

    override ngOnInit(): void {
        if (this.data?.slot) {
            this.form.patchValue(this.data.slot);
        }
    }

    save(): void {
        if (this.form.invalid) return;

        const formValue = this.form.value;
        const payload = {
            uid: formValue.uid?.trim() || undefined,
            slotName: formValue.slotName.trim(),
            position: formValue.position,
            isActive: formValue.isActive,
            isShared: formValue.isShared
        };

        const result: PageSlotFormDialogResult = {
            isEdit: this.isEditMode(),
            data: payload
        };

        this.close(result);
    }
}
