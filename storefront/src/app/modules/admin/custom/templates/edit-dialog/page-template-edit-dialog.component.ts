import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit, signal } from '@angular/core';
import { FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

import { TranslocoModule } from '@jsverse/transloco';
import { SpaInputComponent } from '@shared/components/custom-ui/spa-input/spa-input.component';
import { SpaReorderListComponent } from '@shared/components/custom-ui/spa-reorder-list/spa-reorder-list.component';
import { SpaSelectComponent, SpaSelectOption } from '@shared/components/custom-ui/spa-select/spa-select.component';
import { SpaTextareaComponent } from '@shared/components/custom-ui/spa-textarea/spa-textarea.component';
import { SpaToggleComponent } from '@shared/components/custom-ui/spa-toggle/spa-toggle.component';
import { SpaDialogContentComponent, SpaDialogFooterComponent, SpaDialogHeaderComponent } from '@shared/components/spa-dialog';
import { SpaFormDialog } from '@shared/components/spa-form-dialog';
import { VALIDATION_LIMITS, VALIDATION_PATTERNS } from '@shared/constants/validation.constants';
import { forkJoin, switchMap, take } from 'rxjs';
import { PageSlotService } from '../../pages/slots/page-slot.service';
import { PageTemplateService } from '../page-template.service';
import {
    CreatePageTemplateDto,
    CreateTemplateSlotDto,
    PageTemplate,
    SLOT_POSITIONS,
    UpdatePageTemplateDto
} from '../page-template.types';

export interface PageTemplateEditDialogData {
    mode: 'create' | 'edit';
    template?: PageTemplate;
}

@Component({
    selector: 'spa-page-template-edit-dialog',
    templateUrl: './page-template-edit-dialog.component.html',
    styleUrls: ['./page-template-edit-dialog.component.scss'],
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        MatButtonModule,

        MatIconModule,
        TranslocoModule,
        SpaInputComponent,
        SpaSelectComponent,
        SpaTextareaComponent,
        SpaToggleComponent,
        SpaDialogHeaderComponent,
        SpaDialogContentComponent,
        SpaDialogFooterComponent,
        SpaReorderListComponent
    ],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class PageTemplateEditDialogComponent extends SpaFormDialog<boolean, PageTemplateEditDialogData> implements OnInit {
    #templateService: PageTemplateService;
    #pageSlotService: PageSlotService;

    slotPositionOptions: SpaSelectOption<string>[] = SLOT_POSITIONS.map(p => ({
        value: p.value,
        label: p.label
    }));


    slotsSig = signal<CreateTemplateSlotDto[]>([]);
    availableSlotsSig = signal<SpaSelectOption<string>[]>([]);

    protected form: FormGroup = this.fb.group({
        name: ['', [
            Validators.required,
            Validators.maxLength(VALIDATION_LIMITS.PAGE_TEMPLATE_NAME_MAX)
        ]],
        uid: ['', [
            Validators.required,
            Validators.maxLength(VALIDATION_LIMITS.UID_TEMPLATE_MAX),
            Validators.pattern(VALIDATION_PATTERNS.UID)
        ]],
        description: ['', Validators.maxLength(VALIDATION_LIMITS.PAGE_TEMPLATE_DESCRIPTION_MAX)],
        isActive: [true]
    });

    constructor(templateService: PageTemplateService, pageSlotService: PageSlotService) {
        super();
        this.#templateService = templateService;
        this.#pageSlotService = pageSlotService;
    }

    override ngOnInit(): void {
        super.ngOnInit();
        this.#initializeFormData();
        this.#loadAvailableSlots();
    }

    #loadAvailableSlots(): void {
        this.#pageSlotService.getSharedSlots().pipe(take(1)).subscribe({
            next: (slots) => {
                this.availableSlotsSig.set(slots.map(s => ({
                    value: s.slotName,
                    label: s.slotName
                })));
            },
            error: () => console.error('Failed to load slots')
        });
    }

    #initializeFormData(): void {
        if (this.isEditMode() && this.data?.template) {
            const t = this.data.template;
            this.form.patchValue({
                name: t.name,
                uid: t.uid,
                description: t.description,
                isActive: t.isActive
            });
            this.slotsSig.set(t.slots?.map(s => ({
                slotName: s.slotName,
                position: s.position,
                sortOrder: s.sortOrder,
                isRequired: s.isRequired
            })) ?? []);
        }
    }

    protected addSlot(): void {
        const currentSlots = this.slotsSig();
        this.slotsSig.set([...currentSlots, {
            slotName: '',
            position: 'CENTER',
            sortOrder: currentSlots.length
        }]);
    }

    protected onSlotsReorder(reorderedSlots: CreateTemplateSlotDto[]): void {
        const updatedSlots = reorderedSlots.map((slot, index) => ({
            ...slot,
            sortOrder: index
        }));
        this.slotsSig.set(updatedSlots);
    }

    protected removeSlot(index: number): void {
        const currentSlots = [...this.slotsSig()];
        currentSlots.splice(index, 1);
        this.slotsSig.set(currentSlots);
    }

    save(): void {
        if (!this.form.valid) return;

        const formValue = this.form.value;

        if (this.isCreateMode()) {
            this.#createTemplate(formValue);
        } else {
            this.#updateTemplate(formValue);
        }
    }

    #createTemplate(formValue: Record<string, unknown>): void {
        const dto: CreatePageTemplateDto = {
            name: formValue['name'] as string,
            uid: formValue['uid'] as string,
            description: formValue['description'] as string | undefined,
            isActive: formValue['isActive'] as boolean
        };

        this.setSubmitting(true);

        this.#templateService.create(dto).pipe(take(1)).subscribe({
            next: (created: PageTemplate) => {
                const slotsToAdd = this.slotsSig();
                if (slotsToAdd.length > 0) {
                    this.#addSlotsToTemplate(created.id, slotsToAdd);
                } else {
                    this.#onCreateSuccess();
                }
            },
            error: () => {
                this.notify.alert('admin.pageTemplates.messages.createFailed');
                this.setSubmitting(false);
            }
        });
    }

    #addSlotsToTemplate(templateId: number, slots: CreateTemplateSlotDto[]): void {
        const slotRequests = slots.map(slot =>
            this.#templateService.addSlot(templateId, slot)
        );

        forkJoin(slotRequests).pipe(take(1)).subscribe({
            next: () => this.#onCreateSuccess(),
            error: () => {
                this.notify.success('admin.pageTemplates.messages.createPartial');
                this.setSubmitting(false);
                this.close(true);
            }
        });
    }

    #onCreateSuccess(): void {
        this.notify.success('admin.pageTemplates.messages.createSuccess');
        this.setSubmitting(false);
        this.close(true);
    }

    #updateTemplate(formValue: Record<string, unknown>): void {
        const dto: UpdatePageTemplateDto = {
            name: formValue['name'] as string,
            description: formValue['description'] as string | undefined,
            isActive: formValue['isActive'] as boolean
        };

        this.setSubmitting(true);

        this.#templateService.update(this.data!.template!.id, dto)
            .pipe(
                switchMap(() => {
                    const slotNames = this.slotsSig().map(s => s.slotName);
                    return this.#templateService.reorderSlots(this.data!.template!.id, slotNames);
                }),
                take(1)
            )
            .subscribe({
                next: () => {
                    this.notify.success('admin.pageTemplates.messages.updateSuccess');
                    this.setSubmitting(false);
                    this.close(true);
                },
                error: (err) => {
                    this.notify.alert('admin.pageTemplates.messages.updateFailed');
                    this.setSubmitting(false);
                }
            });
    }
}
