import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { TranslocoModule } from '@jsverse/transloco';
import { SpaFormDialogData } from '@shared/components/spa-dialog-base';
import { SpaFormDialog } from '@shared/components/spa-form-dialog/spa-form-dialog.directive';

// Shared Components
import { SpaInputComponent, SpaSelectComponent, SpaToggleComponent } from '@shared/components/custom-ui';
import { SpaDialogContentComponent, SpaDialogFooterComponent, SpaDialogHeaderComponent } from '@shared/components/spa-dialog';

import { NavigationNodeService } from '../../navigation-node.service';
import {
    CreateEntryRequest,
    NAVIGATION_ITEM_TYPE_OPTIONS,
    NavigationEntry,
    NavigationItemType,
    UpdateEntryRequest
} from '../../navigation-node.types';

export interface EntryDialogData extends SpaFormDialogData<NavigationEntry> {
    mode: 'create' | 'edit';
    entry?: NavigationEntry;
    nodeId?: number;
}

@Component({
    selector: 'app-entry-dialog',
    templateUrl: './entry-dialog.component.html',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatButtonModule,
        TranslocoModule,
        SpaDialogHeaderComponent,
        SpaDialogContentComponent,
        SpaDialogFooterComponent,
        SpaInputComponent,
        SpaSelectComponent,
        SpaToggleComponent
    ],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class NavigationEntryDialogComponent extends SpaFormDialog<NavigationEntry, EntryDialogData> {
    #service = inject(NavigationNodeService);
    #fb = inject(FormBuilder);

    itemTypeOptions = NAVIGATION_ITEM_TYPE_OPTIONS;

    targetOptions = [
        { value: '_self', label: 'Same Tab' },
        { value: '_blank', label: 'New Tab' }
    ];
    selectedItemTypeSig = signal<NavigationItemType>(NavigationItemType.URL);

    showUrlFieldSig = computed(() => this.selectedItemTypeSig() === NavigationItemType.URL);
    showItemIdFieldSig = computed(() =>
        this.selectedItemTypeSig() === NavigationItemType.PAGE ||
        this.selectedItemTypeSig() === NavigationItemType.COMPONENT
    );

    form: FormGroup = this.#fb.group({
        uid: ['', [Validators.required, Validators.pattern('^[a-z0-9_-]+$')]],
        itemType: [NavigationItemType.URL, [Validators.required]],
        linkName: ['', [Validators.required]],
        url: [''],
        itemId: [''],
        linkColor: [''],
        target: ['_self'],
        isExternal: [false],
        isVisible: [true]
    });

    protected override initializeForm(): void {
        if (this.data?.entry) {
            this.form.patchValue(this.data.entry);
            this.selectedItemTypeSig.set(this.data.entry.itemType);
        }

        this.form.get('itemType')?.valueChanges.subscribe(value => {
            this.selectedItemTypeSig.set(value);
            if (value === NavigationItemType.URL) {
                this.form.patchValue({ itemId: '' });
            } else {
                this.form.patchValue({ url: '' });
            }
        });
    }

    override save(): void {
        const formData = this.form.value;

        if (this.isCreateMode()) {
            const request: CreateEntryRequest = {
                nodeId: this.data!.nodeId!,
                uid: formData.uid,
                itemType: formData.itemType,
                linkName: formData.linkName,
                url: formData.url || undefined,
                itemId: formData.itemId || undefined,
                linkColor: formData.linkColor || undefined,
                target: formData.target,
                isExternal: formData.isExternal,
                isVisible: formData.isVisible
            };
            this.submit(this.#service.createEntry(request));
        } else {
            const request: UpdateEntryRequest = {
                itemType: formData.itemType,
                linkName: formData.linkName,
                url: formData.url || undefined,
                itemId: formData.itemId || undefined,
                linkColor: formData.linkColor || undefined,
                target: formData.target,
                isExternal: formData.isExternal,
                isVisible: formData.isVisible
            };
            this.submit(this.#service.updateEntry(this.data!.entry!.id, request));
        }
    }
}


